package de.comroid;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.comroid.javacord.util.commands.CommandHandler;
import de.comroid.javacord.util.server.properties.ServerPropertiesManager;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import de.comroid.tzero.TimeListener;
import de.comroid.tzero.TimezoneManager;
import de.comroid.tzero.commands.AdminCommands;
import de.comroid.tzero.commands.BasicCommands;
import de.comroid.tzero.commands.TimezoneCommands;
import de.comroid.util.files.FileProvider;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class TZero {
    public static final DiscordApi API;
    public static final CommandHandler CMD;
    public static final ServerPropertiesManager PROP;

    static {
        try {
            File file = FileProvider.getFile("login/token.cred");
            System.out.println("Looking for token file at " + file.getAbsolutePath());
            API = new DiscordApiBuilder()
                    .setToken(new BufferedReader(new FileReader(file)).readLine())
                    .login()
                    .exceptionally(ExceptionLogger.get())
                    .join();

            API.updateStatus(UserStatus.DO_NOT_DISTURB);
            API.updateActivity("Booting up...");

            DefaultEmbedFactory.setEmbedSupplier(() -> new EmbedBuilder().setColor(new Color(0x7289DA)));

            CMD = new CommandHandler(API);
            CMD.prefixes = new String[]{"tzero!", "time!", "timezone!"};
            CMD.useDefaultHelp(null);
            CMD.registerCommands(BasicCommands.INSTANCE);
            CMD.registerCommands(TimezoneCommands.INSTANCE);
            CMD.registerCommands(AdminCommands.INSTANCE);

            PROP = new ServerPropertiesManager(FileProvider.getFile("data/properties.json"));
            PROP.usePropertyCommand(null, CMD);
            PROP.register("bot.prefix", CMD.prefixes[0])
                    .withDisplayName("Custom Command Prefix")
                    .withDescription("A custom prefix to call bot commands with");

            CMD.withCustomPrefixProvider(PROP.getProperty("bot.prefix"));

            API.getThreadPool()
                    .getScheduler()
                    .scheduleAtFixedRate(() -> {
                        try {
                            PROP.storeData();
                            TimezoneManager.INSTANCE.tick();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, 5, 5, TimeUnit.MINUTES);
            Runtime.getRuntime().addShutdownHook(new Thread(TZero::shutdown));

            API.updateStatus(UserStatus.ONLINE);
            API.updateActivity(ActivityType.LISTENING, "tzero!help");
        } catch (Exception e) {
            throw new RuntimeException("Error in initializer", e);
        }
    }

    public static void main(String[] args) {
        API.addListener(TimeListener.INSTANCE);
    }

    private static void shutdown() {
        try {
            TimezoneManager.INSTANCE.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
