package de.kaleidox.tzero.commands;

import java.util.TimeZone;

import de.kaleidox.javacord.util.commands.Command;
import de.kaleidox.javacord.util.commands.CommandGroup;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;
import de.kaleidox.tzero.TimezoneManager;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

@CommandGroup(name = "Timezone Commands", description = "Commands for setting up your T-Zero experience", ordinal = 0)
public enum TimezoneCommands {
    INSTANCE;

    @Command(
            aliases = {"zone", "set", "timezone"},
            usage = "zone <Timezone>",
            description = "Set your own timezone",
            requiredArguments = 1,
            ordinal = 0
    )
    public EmbedBuilder timezone(User user, String[] args) {
        TimeZone newZone = TimeZone.getTimeZone(args[0]);
        TimeZone prevZone = TimezoneManager.INSTANCE.setZone(user, newZone);

        return DefaultEmbedFactory.create()
                .addField("Timezone changed!", prevZone == null
                        ? "Your timezone was set to `" + newZone.getDisplayName() + "`!"
                        : "Your timezone was changed from `" + prevZone.getDisplayName() + "` to `"
                        + newZone.getDisplayName() + "`!");
    }
}
