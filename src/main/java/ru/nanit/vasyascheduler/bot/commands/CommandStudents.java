package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.ScheduleTimer;
import ru.nanit.vasyascheduler.services.SubscribesManager;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class CommandStudents implements CommandHandler {

    private final Language lang;
    private final SubscribesManager subscribesManager;
    private final ScheduleManager scheduleManager;

    public CommandStudents(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager) {
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if (ScheduleTimer.isUpdating()){
            return new Message(lang.of("timer.updating"));
        }

        SubscriberStudent subscriber = subscribesManager.getStudentSubscriber(sender.getBotType(), sender.getId());

        if(subscriber != null){
            StudentSchedule schedule = scheduleManager.getStudentSchedule(subscriber.getSchedule());

            CompletableFuture.runAsync(()->{
                try{
                    BufferedImage image = schedule.toImage();

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
                    Logger.error("Error while sending student schedule image: ", e);
                }

                BotManager.getBot(sender.getBotType())
                        .sendMessage(new Message(lang.of("command.students.image.error")));
            });

            return new Message(String.format(lang.of("command.students.send"), schedule.getDisplayName()));
        }

        return new Message(lang.of("command.students.not_subscribed"));
    }

}
