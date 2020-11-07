package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.services.SubscribesManager;

public class CommandStudentsDeny implements CommandHandler {

    private final Language lang;
    private final SubscribesManager subscribesManager;

    public CommandStudentsDeny(Language lang, SubscribesManager subscribesManager){
        this.lang = lang;
        this.subscribesManager = subscribesManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        SubscriberStudent subscriber = subscribesManager.getStudentSubscriber(sender.getBotType(), sender.getId());

        if(subscriber != null){
            subscribesManager.removeStudentSubscriber(subscriber);
            return new Message(lang.of("command.students.deny.success"));
        }

        return new Message(lang.of("command.students.deny.error"));
    }

}
