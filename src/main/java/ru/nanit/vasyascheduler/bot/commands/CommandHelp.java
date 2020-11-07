package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Message;

public class CommandHelp implements CommandHandler {

    private Language lang;

    public CommandHelp(Language lang){
        this.lang = lang;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        return new Message(String.join("\n", lang.ofList("commands.help")));
    }

}
