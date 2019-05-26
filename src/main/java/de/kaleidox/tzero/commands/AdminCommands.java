package de.kaleidox.tzero.commands;

import de.kaleidox.javacord.util.commands.Command;

import org.javacord.api.entity.user.User;

public enum AdminCommands {
    INSTANCE;

    @Command(
            usage = "shutdown",
            description = "Only the owner of the bot can use this",
            shownInHelpCommand = false
    )
    public void shutdown(User user) {
        if (user.isBotOwner()) System.exit(0);
    }
}
