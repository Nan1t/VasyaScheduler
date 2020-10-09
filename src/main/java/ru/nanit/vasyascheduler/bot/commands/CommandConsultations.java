package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.ScheduleTimer;
import ru.nanit.vasyascheduler.services.SubscribesManager;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;

public class CommandConsultations implements CommandHandler {

    private final Language lang;
    private final SubscribesManager subscribesManager;
    private final ScheduleManager scheduleManager;

    public CommandConsultations(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager){
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
            BufferedImage image = scheduleManager.getConsultationSchedule()
                    .getBuilder().build(lang, teacher);

            if(image != null){
                Message response = new Message(String.format(lang.of("command.consultations.send"), teacher.toString()));
                Media media = new Media(Media.Type.DOCUMENT);
                media.setStream(ImageUtil.createInputStream(image, XlsToImage.getFileName()));
                media.setFileName("consultations." + XlsToImage.getFileName());
                response.addMedia(media);
                return response;
            }

            return new Message(String.format(lang.of("command.consultations.send.error"), teacher));
        }

        return new Message(lang.of("command.consultations.not_subscribed"));
    }

}
