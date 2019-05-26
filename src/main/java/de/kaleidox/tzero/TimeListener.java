package de.kaleidox.tzero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jetbrains.annotations.Nullable;

public enum TimeListener implements MessageCreateListener {
    INSTANCE;

    public static final String CLOCK_EMOJI = "⏰";
    public static final Pattern TIME_PATTERN = Pattern.compile(".*?(\\s(?<hour>([012]?\\d)))([:.\\s](?<minute>\\d{2}))?(\\s??(?<ampm>[ap]m))?.*?");
    public static final DateTimeFormatter FORMATTER_DAY = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR)
            .appendLiteral(' ')
            .appendZoneOrOffsetId()
            .toFormatter();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) return;
        Optional<User> optionalUser = event.getMessageAuthor().asUser();
        if (!optionalUser.isPresent()) return;
        try {
            User user = optionalUser.get();
            TimeZone targetZone = TimezoneManager.INSTANCE.getZone(optionalUser.get());
            Matcher matcher = TIME_PATTERN.matcher(event.getReadableMessageContent());
            String hourEx, ampmEx;
            if (!matcher.matches() || (hourEx = group(matcher, "hour", null)) == null) return;
            int hour = Integer.parseInt(hourEx);
            int minute = Integer.parseInt(group(matcher, "minute", "00"));
            if (group(matcher, "ampm", "am").equals("pm")) hour += 12;

            LocalDate targetDate = guessDateFromContent(event.getReadableMessageContent());

            long offsettingHours = TimeUnit.MILLISECONDS.toHours(targetZone.getRawOffset());
            long offsettingMinutes = TimeUnit.MILLISECONDS.toMinutes(targetZone.getRawOffset()) - (offsettingHours * 60);

            long targetMinute = (minute - offsettingMinutes >= 60
                    ? (minute - offsettingMinutes - 60)
                    : (minute - offsettingMinutes));
            long targetHour = hour - offsettingHours + (targetMinute >= 60 ? 1 : 0);

            TemporalAccessor parse = FORMATTER_DAY.parse(String.format("%d-%d-%d %d:%d %s",
                    targetDate.getYear(),
                    targetDate.getMonthValue(),
                    targetDate.getDayOfMonth(),
                    targetHour,
                    targetMinute,
                    targetZone.getID()));

            ZonedDateTime targetTime = LocalDateTime.from(parse).atZone(targetZone.toZoneId());

            Message message = event.getMessage();
            TimeConverter converter = new TimeConverter(targetTime, optionalUser.get());
            message.addReaction(CLOCK_EMOJI);
            message.addReactionAddListener(converter);
        } catch (DateTimeParseException ignored) {
        }
    }

    private static LocalDate guessDateFromContent(String content) {
        String x;
        if (content.contains("tomorrow"))
            return LocalDate.now().plusDays(1);
        else if (content.contains("yesterday"))
            return LocalDate.now().minusDays(1);
        else return LocalDate.now();
    }

    @SuppressWarnings("ConstantConditions")
    private static String group(Matcher matcher, String group, @Nullable String def) {
        try {
            String yield = matcher.group(group);
            return yield == null ? def : yield;
        } catch (IllegalStateException ignored) {
            return def;
        }
    }

    private static String formatZonedTime(ZonedDateTime time) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        int[] timeAsInts = new int[]{time.getHour(), time.getMinute()};
        int[] offsetAsInts = offsetAsInts(time);

        for (int i = 0; i < offsetAsInts.length; i++) {
            timeAsInts[i] += offsetAsInts[i];
            if (i == 1) if (timeAsInts[1] >= 60) {
                timeAsInts[0] += 1;
                timeAsInts[1] = timeAsInts[1] - 60;
            }
        }

        return String.format("%s %d:%2d", dateFormatter.format(time), timeAsInts[0], timeAsInts[1]);
    }

    private static int[] offsetAsInts(ZonedDateTime time) {
        int[] yields = new int[2];
        DateTimeFormatter offsetFormatter = new DateTimeFormatterBuilder().appendOffsetId().toFormatter();

        String format = time.format(offsetFormatter);
        String[] split = format.split("[+:]");

        if (split.length != 3)
            return new int[]{0, 0};

        int yI = 0;
        for (String s : split)
            if (s != null && !s.isEmpty())
                yields[yI++] = Integer.parseInt(s) * (yI == 0 && format.charAt(0) == '-' ? -1 : 1);

        return yields;
    }

    private class TimeConverter implements ReactionAddListener {
        private final ZonedDateTime originTime;
        private final User originUser;
        private final ZoneId originZone;

        public TimeConverter(ZonedDateTime originTime, User originUser) {
            this.originTime = originTime;
            this.originUser = originUser;
            this.originZone = originTime.getZone();
        }

        @Override
        public void onReactionAdd(ReactionAddEvent event) {
            handleReaction(event);
        }

        private synchronized void handleReaction(SingleReactionEvent event) {
            User targetUser = event.getUser();
            if (targetUser.isBot()) return;
            if (originUser.equals(event.getUser())) return;
            if (!event.getEmoji().asUnicodeEmoji().map(CLOCK_EMOJI::equals).orElse(false)) return;

            TimeZone targetZone = TimezoneManager.INSTANCE.getZone(targetUser);
            EmbedBuilder embed = DefaultEmbedFactory.create();

            embed.addField(originUser.getName() + " [" + originZone.getId() + "] mentioned the time:", formatZonedTime(originTime))
                    .addField("For " + targetUser.getName() + " [" + targetZone.getID() + "] this is at:",
                            formatZonedTime(TimezoneManager.INSTANCE.translate(originTime, originUser, targetUser)))
                    .setFooter("This message will self-delete in 30 seconds.");

            event.requestMessage()
                    .thenApply(Message::getChannel)
                    .thenCompose(tc -> tc.sendMessage(embed))
                    .thenAccept(msg -> event.requestMessage()
                            .thenAccept(prv -> prv.addReactionRemoveListener(new Deleter(prv, msg, targetUser))
                                    .removeAfter(30, TimeUnit.SECONDS)
                                    .addRemoveHandler(() -> {
                                        msg.delete();
                                        prv.removeReactionsByEmoji(targetUser, CLOCK_EMOJI);
                                    })))
                    .exceptionally(ExceptionLogger.get());
        }

        private class Deleter implements ReactionRemoveListener {
            private final Message attached;
            private final Message target;
            private final User targetUser;

            public Deleter(Message attached, Message target, User targetUser) {
                this.attached = attached;
                this.target = target;
                this.targetUser = targetUser;
            }

            @Override
            public void onReactionRemove(ReactionRemoveEvent event) {
                if (!event.getUser().equals(targetUser)) return;
                if (!event.getEmoji().asUnicodeEmoji().map(CLOCK_EMOJI::equals).orElse(false)) return;

                target.delete().exceptionally(ExceptionLogger.get());
                attached.removeReactionsByEmoji(targetUser, CLOCK_EMOJI);
                attached.removeMessageAttachableListener(this);
            }
        }
    }
}
