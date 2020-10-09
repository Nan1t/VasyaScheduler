package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;

import ru.nanit.vasyascheduler.data.chat.ChatButton;
import ru.nanit.vasyascheduler.data.chat.Keyboard;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.services.BotManager;
import ru.nanit.vasyascheduler.services.ScheduleManager;
import ru.nanit.vasyascheduler.services.ScheduleTimer;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandAud implements CommandHandler {

    private final ScheduleManager scheduleManager;
    private final Language lang;

    public CommandAud(ScheduleManager scheduleManager, Language lang){
        this.scheduleManager = scheduleManager;
        this.lang = lang;
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) {
        if (ScheduleTimer.isUpdating()){
            return new Message(lang.of("timer.updating"));
        }

        if(args.length == 2){
            if(args[0].equals("show")){
                String aud = args[1];

                Message response = new Message(String.format(lang.of("command.auds.send"), aud));

                CompletableFuture.runAsync(()->{
                    try{
                        Message imgMessage = new Message();
                        Media media = new Media(Media.Type.DOCUMENT);
                        BufferedImage image = scheduleManager.buildAudSchedule(aud);

                        if(image == null){
                            imgMessage.setMessage(lang.of("command.auds.not_found"));
                            BotManager.getBot(sender.getBotType()).sendMessage(imgMessage);
                            return;
                        }

                        media.setStream(ImageUtil.createInputStream(image, XlsToImage.getFileName()));
                        media.setFileName("audition." + XlsToImage.getFileName());
                        imgMessage.addMedia(media);
                        imgMessage.setChatId(sender.getId());
                        BotManager.getBot(sender.getBotType()).sendMessage(imgMessage);
                    } catch (IOException e){
                        Logger.error("Cannot send audition schedule: ", e);
                        BotManager.getBot(sender.getBotType()).sendMessage(new Message(lang.of("error.undefined")));
                    }
                });

                response.setEditMessage(true);
                response.setRemoveLastId(true);
                return response;
            }
        }

        // Show all auds
        if(args.length < 2){
            Collection<String> auds = scheduleManager.getAllAuds();

            if(!auds.isEmpty()){
                Message message = new Message();
                Keyboard keyboard = new Keyboard();
                int page = 1;

                if(args.length > 0){
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e){
                        message.setMessage("Wrong page number");
                        return message;
                    }
                }

                int pages = (int)Math.ceil((float)auds.size() / 10);
                int begin = 10 * (page-1);
                int end = begin + 10;
                int counter = 0;

                for (String aud : auds){
                    if(counter >= begin) {
                        if(counter > end) break;

                        ChatButton button = new ChatButton(aud, "/aud show " + aud);

                        if(counter % 2 == 0){
                            keyboard.addButtonToNewRow(button);
                        } else {
                            keyboard.addButton(button);
                        }
                    }

                    counter++;
                }

                ChatButton prevPage = new ChatButton(lang.of("list.page.prev"), "/aud " + (page - 1));
                ChatButton nextPage = new ChatButton(lang.of("list.page.next"), "/aud " + (page + 1));

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
                message.setMessage(String.format(lang.of("command.auds.list"), page));
                return message;
            }

            return new Message("error.undefined");
        }
        return null;
    }

}
