package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.data.chat.ChatButton;
import ru.nanit.vasyascheduler.data.chat.Keyboard;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;

import ru.nanit.vasyascheduler.data.user.SubscriberTeacher;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.SubscribesManager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandTeacherSubscribe implements CommandHandler {

    private static final int TEACHERS_ON_PAGE = 15;

    private Language lang;
    private ScheduleManager scheduleManager;
    private SubscribesManager subscribesManager;

    public CommandTeacherSubscribe(Language lang, SubscribesManager subscribesManager, ScheduleManager scheduleManager){
        this.lang = lang;
        this.subscribesManager = subscribesManager;
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        if(args.length < 3){
            List<Person> teachers = scheduleManager.getTeacherSchedule().getTeachers();

            if(!teachers.isEmpty()){
                Message message = new Message();
                Keyboard keyboard = new Keyboard();
                int page = 1;

                if(args.length > 0){
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e){
                        message.setMessage(lang.of("Wrong page number"));
                        return message;
                    }
                }

                int pages = (int)Math.ceil((float)teachers.size() / TEACHERS_ON_PAGE);
                int begin = TEACHERS_ON_PAGE * (page-1);
                int end = begin + TEACHERS_ON_PAGE;
                int counter = 0;

                for (Person teacher : teachers){
                    if(counter >= begin) {
                        if(counter > end) break;

                        ChatButton button = new ChatButton(teacher.toString(), "/teachersubscribe " + teacher.getLastName() + " " + teacher.getFirstNameLetter() + " " + teacher.getPatronymicLetter());

                        if(counter % 2 == 0){
                            keyboard.addButtonToNewRow(button);
                        } else {
                            keyboard.addButton(button);
                        }
                    }

                    counter++;
                }

                ChatButton prevPage = new ChatButton(lang.of("list.page.prev"), "/teachersubscribe " + (page - 1));
                ChatButton nextPage = new ChatButton(lang.of("list.page.next"), "/teachersubscribe " + (page + 1));

                if(page <= 1){
                    keyboard.addButtonToNewRow(nextPage);
                } else if(page < pages){
                    keyboard.addButtonToNewRow(prevPage);
                    keyboard.addButton(nextPage);
                } else {
                    keyboard.addButtonToNewRow(prevPage);
                }

                message.setKeyboard(keyboard);
                message.setEditMessage(true);
                message.setMessage(String.format(lang.of("command.teacher.subscribe.list"), page));
                return message;
            }

            return new Message(lang.of("error.undefined"));
        }

        if(args.length == 3){
            String firstName = args[1].toUpperCase();
            String lastName = lastNameToUppercase(args[0]);
            String patronymic = args[2].toUpperCase();
            Person teacher = new Person(firstName, lastName, patronymic);

            if(scheduleManager.getTeacherSchedule().hasTeacher(teacher)){
                SubscriberTeacher subscriber = new SubscriberTeacher(teacher);
                subscriber.setBotType(sender.getBotType());
                subscriber.setMessengerId(sender.getId());

                subscribesManager.addTeacherSubscriber(subscriber);

                CompletableFuture.runAsync(()->{
                    try{
                        System.out.println("Run async");
                        BufferedImage image = scheduleManager.getTeacherSchedule()
                                .getBuilder().build(lang, scheduleManager.getStudentSchedule(), teacher);

                        System.out.println("Image is " + image);

                        if(image != null){
                            Message scheduleMessage = new Message();
                            Media media = new Media(Media.Type.DOCUMENT);
                            media.setStream(ImageUtil.createInputStream(image, "png"));
                            media.setFileName("schedule.png");
                            scheduleMessage.addMedia(media);
                            scheduleMessage.setChatId(sender.getId());
                            System.out.println("Send to bot");
                            BotManager.getBot(sender.getBotType()).sendMessage(scheduleMessage);
                            return;
                        }
                    } catch (IOException e){
                        Logger.error("Error while sending image: ", e);
                    }

                    BotManager.getBot(sender.getBotType())
                            .sendMessage(new Message(lang.of("error.undefined")));
                });

                Message response = new Message(String.format(lang.of("command.teacher.subscribe.success"), teacher.toString()));
                response.setEditMessage(true);
                response.setRemoveLastId(true);
                return response;
            }

            return new Message(lang.of("command.teacher.subscribe.error"));
        }

        return new Message("Nothing to show");
    }

    private String lastNameToUppercase(String str){
        char[] arr = str.toCharArray();
        char firstChar = Character.toUpperCase(arr[0]);
        return firstChar + String.valueOf(Arrays.copyOfRange(arr, 1, str.length()));
    }

}
