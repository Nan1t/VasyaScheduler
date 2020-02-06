package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.user.SubscriberStudent;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.SubscribesManager;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandStudents implements CommandHandler {

    private Language lang;

    private SubscribesManager subscribesManager;
    private ScheduleManager scheduleManager;
    private Message help;

    public CommandStudents(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager) {
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
        this.help = new Message(lang.of("command.students.help"));
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if(args.length == 0){
            SubscriberStudent subscriber = subscribesManager.getStudentSubscriber(sender.getBotType(), sender.getId());

            if(subscriber != null){
                StudentSchedule schedule = scheduleManager.getStudentSchedule(subscriber.getSchedule());

                CompletableFuture.runAsync(()->{
                    try{
                        BufferedImage image = schedule.toImage();

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
                        Logger.error("Error while sending student schedule image: ", e);
                    }

                    BotManager.getBot(sender.getBotType())
                            .sendMessage(new Message(lang.of("command.students.image.error")));
                });

                return new Message(String.format(lang.of("command.students.send"), schedule.getDisplayName()));
            }

            return new Message(lang.of("command.students.not_subscribed"));
        }

        if(args.length == 1){
            Collection<StudentSchedule> schedules = scheduleManager.getStudentSchedule().values();

            if(args[0].equals("list")){
                StringBuilder msgBuilder = new StringBuilder();

                int i = 0;
                for (StudentSchedule schedule : schedules){
                    msgBuilder.append(i);
                    msgBuilder.append(" - ");
                    msgBuilder.append(schedule.getDisplayName());
                    msgBuilder.append("\n");
                    i++;
                }

                return new Message(msgBuilder.toString());
            }

            // Try to get schedule by numeric id

            try{
                int index = Integer.parseInt(args[0]);
                int i = 0;

                for (StudentSchedule schedule : schedules){
                    if(i == index){
                        SubscriberStudent student = new SubscriberStudent(schedule.getName());
                        student.setBotType(sender.getBotType());
                        student.setMessengerId(sender.getId());

                        subscribesManager.addStudentSubscriber(student);

                        CompletableFuture.runAsync(()->{
                            try{
                                Message scheduleMessage = new Message("schedule.png");
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
                    i++;
                }

                return new Message(lang.of("command.students.id.not_exists"));
            } catch (NumberFormatException e){
                return new Message(lang.of("command.students.id.format_error"));
            }
        }

        return help;
    }

}
