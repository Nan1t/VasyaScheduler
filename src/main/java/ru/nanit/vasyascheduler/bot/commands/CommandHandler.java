package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Message;

public interface CommandHandler {

    Message execute(CommandSender sender, String command, String... args) throws Exception;

}
