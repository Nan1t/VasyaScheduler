package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.ScheduleTimer;
import ru.nanit.vasyascheduler.services.SubscribesManager;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class CommandTeacher implements CommandHandler {

    private final Language lang;
    private final SubscribesManager subscribesManager;
    private final ScheduleManager scheduleManager;

    public CommandTeacher(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager){
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if (ScheduleTimer.isUpdating()){
            return new Message(lang.of("timer.updating"));
        }

        SubscriberTeacher subscriber = subscribesManager.getTeacherSubscriber(sender.getBotType(), sender.getId());

        if(subscriber != null){
            Person teacher = subscriber.getTeacher();

            if(scheduleManager.getTeacherSchedule().hasTeacher(teacher)){
                CompletableFuture.runAsync(()->{
                    try{
                        BufferedImage image = scheduleManager.getTeacherSchedule()
                                .getBuilder().build(lang, scheduleManager.getStudentSchedule(), teacher);

                        if(image != null){
                            Message response = new Message();
                            Media media = new Media(Media.Type.DOCUMENT);
                            media.setStream(ImageUtil.createInputStream(image, XlsToImage.getFileName()));
                            media.setFileName("schedule." + XlsToImage.getFileName());
                            response.addMedia(media);
                            response.setChatId(sender.getId());
                            BotManager.getBot(sender.getBotType()).sendMessage(response);
                            return;
                        }
                    } catch (Exception e){
                        Logger.error("Error while sending image: ", e);
                    }

                    BotManager.getBot(sender.getBotType())
                            .sendMessage(new Message(lang.of("error.undefined")));
                });

                return new Message(String.format(lang.of("command.teacher.send"), teacher.toString()));
            }

            return new Message(lang.of("command.teacher.send.error"));
        }

        return new Message(lang.of("command.teacher.not_subscribed"));
    }

}
