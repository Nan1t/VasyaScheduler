package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.schedule.TeacherSchedule;
import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.SubscribesManager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class CommandTeacher implements CommandHandler {

    private Language lang;
    private SubscribesManager subscribesManager;
    private ScheduleManager scheduleManager;
    private Message help;

    public CommandTeacher(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager){
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
        this.help = new Message(lang.of("command.teacher.help"));
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if(args.length == 0){
            SubscriberTeacher subscriber = subscribesManager.getTeacherSubscriber(sender.getBotType(), sender.getId());

            if(subscriber != null){
                Person teacher = subscriber.getTeacher();

                if(scheduleManager.getTeacherSchedule().hasTeacher(teacher)){
                    CompletableFuture.runAsync(()->{
                        try{
                            BufferedImage image = scheduleManager.getTeacherSchedule()
                                    .getBuilder().build(lang, scheduleManager.getStudentSchedule(), teacher);

                            if(image != null){
                                Message response = new Message("schedule.png");
                                Media media = new Media(Media.Type.DOCUMENT);
                                media.setStream(ImageUtil.createInputStream(image, "png"));
                                media.setFileName("schedule.png");
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

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("deny")){
                SubscriberTeacher subscriber = subscribesManager.getTeacherSubscriber(sender.getBotType(), sender.getId());

                if(subscriber != null){
                    subscribesManager.removeTeacherSubscriber(subscriber);
                    return new Message(String.format(lang.of("command.teacher.deny.success"), subscriber.getTeacher().toString()));
                }

                return new Message(lang.of("command.teacher.deny.error"));
            }

            return help;
        }

        if(args.length == 3){
            String firstName = args[1].toUpperCase();
            String lastName = args[0];
            String patronymic = args[2].toUpperCase();
            Person teacher = new Person(firstName, lastName, patronymic);

            if(scheduleManager.getTeacherSchedule().hasTeacher(teacher)){
                SubscriberTeacher subscriber = new SubscriberTeacher(teacher);
                subscriber.setBotType(sender.getBotType());
                subscriber.setMessengerId(sender.getId());

                subscribesManager.addTeacherSubscriber(subscriber);

                CompletableFuture.runAsync(()->{
                    try{
                        BufferedImage image = scheduleManager.getTeacherSchedule()
                                .getBuilder().build(lang, scheduleManager.getStudentSchedule(), teacher);

                        if(image != null){
                            Message scheduleMessage = new Message("schedule.png");
                            Media media = new Media(Media.Type.DOCUMENT);
                            media.setStream(ImageUtil.createInputStream(image, "png"));
                            media.setFileName("schedule.png");
                            scheduleMessage.addMedia(media);
                            scheduleMessage.setChatId(sender.getId());
                            BotManager.getBot(sender.getBotType()).sendMessage(scheduleMessage);
                            return;
                        }
                    } catch (IOException e){
                        Logger.error("Error while sending image: ", e);
                    }

                    BotManager.getBot(sender.getBotType())
                            .sendMessage(new Message(lang.of("error.undefined")));
                });

                return new Message(String.format(lang.of("command.teacher.subscribe.success"), teacher.toString()));
            }

            return new Message(lang.of("command.teacher.subscribe.error"));
        }

        return help;
    }

}
