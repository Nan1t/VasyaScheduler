package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.properties.Properties;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Message;

public class CommandInvalidateHash implements CommandHandler {

    public final Properties hashes;

    public CommandInvalidateHash(Properties hashes){
        this.hashes = hashes;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        hashes.invalidate();
        return new Message("All schedule hash has been invalidated");
    }

}
