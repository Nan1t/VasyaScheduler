package ru.nanit.vasyascheduler.bot;

import ru.nanit.vasyascheduler.data.chat.Message;

public interface CommandHandler {

    Message execute(CommandSender sender, String command, String... args) throws Exception;

}
