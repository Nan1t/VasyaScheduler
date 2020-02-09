package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.ChatButton;
import ru.nanit.vasyascheduler.data.chat.Keyboard;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.SubscribesManager;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandStudentsSubscribe implements CommandHandler {

    private Language lang;
    private SubscribesManager subscribesManager;
    private ScheduleManager scheduleManager;

    public CommandStudentsSubscribe(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager) {
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if(args.length == 0){
            Map<String, StudentSchedule> scheduleMap = scheduleManager.getStudentSchedule();
            Message message = new Message(lang.of("command.students.subscribe.list"));
            Keyboard keyboard = new Keyboard();

            for (Map.Entry<String, StudentSchedule> entry : scheduleMap.entrySet()){
                keyboard.addButtonToNewRow(new ChatButton(entry.getValue().getDisplayName(),
                        "/studentsubscribe " + entry.getKey()));
            }

            message.setKeyboard(keyboard);
            message.setEditMessage(true);
            return message;
        }

        StudentSchedule schedule = scheduleManager.getStudentSchedule(args[0]);

        if(schedule != null){
            SubscriberStudent student = new SubscriberStudent(args[0]);
            student.setBotType(sender.getBotType());
            student.setMessengerId(sender.getId());

            subscribesManager.addStudentSubscriber(student);

            CompletableFuture.runAsync(()->{
                try{
                    Message scheduleMessage = new Message();
                    Media media = new Media(Media.Type.DOCUMENT);

                    media.setStream(ImageUtil.createInputStream(schedule.toImage(), "png"));
                    media.setFileName("schedule.png");
                    scheduleMessage.addMedia(media);
                    scheduleMessage.setChatId(sender.getId());

                    BotManager.getBot(sender.getBotType()).sendMessage(scheduleMessage);
                } catch (Exception e){
                    Logger.error("Error while sending student schedule image: ", e);
                    BotManager.getBot(sender.getBotType())
                            .sendMessage(new Message(lang.of("error.undefined")));
                }
            });

            return new Message(String.format(lang.of("command.students.subscribe.success"), schedule.getDisplayName()));
        }

        return new Message(lang.of("command.students.not_exists"));
    }

}
