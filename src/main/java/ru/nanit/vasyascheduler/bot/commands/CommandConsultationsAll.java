package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.services.ScheduleManager;

import java.awt.image.BufferedImage;

public class CommandConsultationsAll implements CommandHandler {

    private Language lang;
    private ScheduleManager scheduleManager;

    public CommandConsultationsAll(Language lang, ScheduleManager scheduleManager){
        this.lang = lang;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        BufferedImage image = scheduleManager.getConsultationSchedule().toImage();

        if(image != null){
            Message message = new Message();
            Media media = new Media(Media.Type.DOCUMENT);
            media.setStream(ImageUtil.createInputStream(image, "png"));
            media.setFileName("consultations.png");
            message.addMedia(media);
            return message;
        }

        return new Message(lang.of("error.undefined"));
    }

}
