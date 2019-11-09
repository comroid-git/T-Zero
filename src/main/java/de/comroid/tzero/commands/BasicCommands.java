package de.comroid.tzero.commands;

import de.comroid.javacord.util.commands.Command;
import de.comroid.javacord.util.commands.CommandGroup;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

import static org.javacord.api.util.logging.ExceptionLogger.get;

@CommandGroup(name = "Basic Commands", description = "All commands for basic interaction with the bot")
public enum BasicCommands {
    INSTANCE;

    public static final String INVITE_LINK = "https://discordapp.com/oauth2/authorize?client_id=581831468381110273&scope=bot&permissions=85056";

    @Command(usage = "invite", description = "Sends an invite link for the bot to you via DM")
    public void invite(Message msg, User usr) {
        usr.sendMessage(DefaultEmbedFactory.create().addField("Invite Link", INVITE_LINK))
                .thenCompose(ign -> msg.delete())
                .exceptionally(get())
                .join();
    }
}
