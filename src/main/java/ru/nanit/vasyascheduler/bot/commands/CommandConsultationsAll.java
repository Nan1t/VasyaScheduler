package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.ScheduleTimer;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;

public class CommandConsultationsAll implements CommandHandler {

    private final Language lang;
    private final ScheduleManager scheduleManager;

    public CommandConsultationsAll(Language lang, ScheduleManager scheduleManager){
        this.lang = lang;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if (ScheduleTimer.isUpdating()){
            return new Message(lang.of("timer.updating"));
        }

        BufferedImage image = scheduleManager.getConsultationSchedule().toImage();

        if(image != null){
            Message message = new Message();
            Media media = new Media(Media.Type.DOCUMENT);
            media.setStream(ImageUtil.createInputStream(image, XlsToImage.getFileName()));
            media.setFileName("consultations." + XlsToImage.getFileName());
            message.addMedia(media);
            return message;
        }

        return new Message(lang.of("error.undefined"));
    }

}
