package ru.nanit.vasyascheduler.bot.types;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.services.CommandManager;
import ru.nanit.vasyascheduler.bot.Bot;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BotTelegram implements Bot {

    private String name;
    private String token;

    private CommandManager cmdManager;
    private MessageHandler handler;

    public BotTelegram(String name, String token, CommandManager cmdManager){
        this.name = name;
        this.token = token;
        this.cmdManager = cmdManager;
    }

    @Override
    public void enable() {
        ApiContextInitializer.init();
        handler = new MessageHandler(name, token);
        TelegramBotsApi botsApi = new TelegramBotsApi();

        try{
            botsApi.registerBot(handler);
            Logger.info("[TELEGRAM] Bot successfully started");
        } catch (Exception e){
            Logger.error("Error while starting Telegram bot: ", e);
        }
    }

    @Override
    public void disable() {
        handler.onClosing();
        Logger.info("Telegram bot disabled");
    }

    @Override
    public void sendMessage(Message message) {
        try {
            if(message.getMessage() != null){
                SendMessage sendMessage = new SendMessage().setChatId(message.getChatId());
                sendMessage.setText(message.getMessage());
                handler.execute(sendMessage);
            }

            if(message.getMedia() != null){
                if(message.getMedia().size() > 1){
                    SendMediaGroup group = new SendMediaGroup().setChatId(message.getChatId());
                    List<InputMedia> list = new ArrayList<>();

                    for (Media media : message.getMedia()){
                        InputMedia input = getInputMedia(media);

                        if(input != null){
                            list.add(input);
                        }
                    }

                    group.setMedia(list);
                    group.setChatId(message.getChatId());
                    handler.execute(group);
                    return;
                }

                for (Media media : message.getMedia()){
                    PartialBotApiMethod method = getSendMethod(media);

                    if(method instanceof SendPhoto){
                        ((SendPhoto) method).setChatId(message.getChatId());
                        handler.execute((SendPhoto)method);
                    }

                    if(method instanceof SendVideo){
                        ((SendVideo) method).setChatId(message.getChatId());
                        handler.execute((SendVideo)method);
                    }

                    if(method instanceof SendAudio){
                        ((SendAudio) method).setChatId(message.getChatId());
                        handler.execute((SendAudio)method);
                    }

                    if(method instanceof SendDocument){
                        ((SendDocument) method).setChatId(message.getChatId());
                        handler.execute((SendDocument)method);
                    }
                }
            }
        } catch (TelegramApiException e){
            Logger.error("Sending message to telegram finished with error: ", e);
        }
    }

    private InputMedia getInputMedia(Media media){
        switch (media.getType()){
            default:
                return null;
            case IMAGE:
                return new InputMediaPhoto().setMedia(media.getStream(), media.getFileName());
            case VIDEO:
                return new InputMediaVideo().setMedia(media.getStream(), media.getFileName());
            case SOUND:
                return new InputMediaAudio().setMedia(media.getStream(), media.getFileName());
            case DOCUMENT:
                return new InputMediaDocument().setMedia(media.getStream(), media.getFileName());
        }
    }

    private PartialBotApiMethod getSendMethod(Media media){
        switch (media.getType()){
            default:
                return null;
            case IMAGE:
                return new SendPhoto().setPhoto(media.getFileName(), media.getStream());
            case VIDEO:
                return new SendVideo().setVideo(media.getFileName(), media.getStream());
            case SOUND:
                return new SendAudio().setAudio(media.getFileName(), media.getStream());
            case DOCUMENT:
                return new SendDocument().setDocument(media.getFileName(), media.getStream());
        }
    }

    private class MessageHandler extends TelegramLongPollingBot{

        private String name;
        private String token;

        public MessageHandler(String name, String token){
            this.name = name;
            this.token = token;
        }

        @Override
        public void onUpdateReceived(Update update) {
            if(update.hasMessage() && update.getMessage().hasText()) {
                String[] arr = update.getMessage().getText().split("@");
                String command = arr[0];

                if(arr.length > 1){
                    if(!arr[1].equalsIgnoreCase(this.name)){
                        return;
                    }
                }

                if(command.startsWith("/")) {
                    CommandSender sender = new CommandSender(update.getMessage().getChatId().toString(), Type.TELEGRAM);
                    CompletableFuture<Message> future = CompletableFuture.supplyAsync(()->
                            cmdManager.executeCommand(sender, update.getMessage().getText().substring(1)));

                    future.thenAcceptAsync((response)->{
                        if(response != null){
                            response.setChatId(update.getMessage().getChatId().toString());
                            sendMessage(response);
                        }
                    });
                }
            }
        }

        @Override
        public String getBotUsername() {
            return this.name;
        }

        @Override
        public String getBotToken() {
            return this.token;
        }
    }
}
