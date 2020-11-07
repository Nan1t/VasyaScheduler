package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;
import ru.nanit.vasyascheduler.services.SubscribesManager;

public class CommandTeacherDeny implements CommandHandler {

    private final Language lang;
    private final SubscribesManager subscribesManager;

    public CommandTeacherDeny(Language lang, SubscribesManager subscribesManager){
        this.lang = lang;
        this.subscribesManager = subscribesManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        SubscriberTeacher subscriber = subscribesManager.getTeacherSubscriber(sender.getBotType(), sender.getId());

        if(subscriber != null){
            subscribesManager.removeTeacherSubscriber(subscriber);
            return new Message(String.format(lang.of("command.teacher.deny.success"), subscriber.getTeacher().toString()));
        }

        return new Message(lang.of("command.teacher.deny.error"));
    }

}
