package ru.nanit.vasyascheduler.bot.types;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.ChatButton;
import ru.nanit.vasyascheduler.services.CommandManager;
import ru.nanit.vasyascheduler.bot.Bot;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;

import java.util.*;
import java.util.concurrent.*;

public class BotTelegram implements Bot {

    private final String name;
    private final String token;

    private final CommandManager cmdManager;
    private MessageHandler handler;

    private final Map<String, Integer> lastIds = new HashMap<>();

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

        handler.startQueueTimer();
    }

    @Override
    public void disable() {
        handler.stopQueueTimer();
        handler.onClosing();
        Logger.info("Telegram bot disabled");
    }

    @Override
    public void sendMessage(Message message) {
        if(message.isEditMessage() && lastIds.containsKey(message.getChatId())){
            EditMessageText text = new EditMessageText();

            text.setChatId(message.getChatId());
            text.setMessageId(lastIds.get(message.getChatId()));
            text.setText(message.getMessage());

            if(message.getKeyboard() != null){
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

                for (List<ChatButton> row : message.getKeyboard().getButtons()){
                    List<InlineKeyboardButton> line = new ArrayList<>();
                    for (ChatButton btn : row){
                        line.add(new InlineKeyboardButton().setText(btn.getText()).setCallbackData(btn.getCommand()));
                    }
                    buttons.add(line);
                }

                markup.setKeyboard(buttons);
                text.setReplyMarkup(markup);
            }

            handler.executeWithQueue(text);
            return;
        }

        if(message.getMessage() != null){
            SendMessage sendMessage = new SendMessage().setChatId(message.getChatId());
            sendMessage.setText(message.getMessage());

            if(message.getKeyboard() != null){
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

                for (List<ChatButton> row : message.getKeyboard().getButtons()){
                    List<InlineKeyboardButton> line = new ArrayList<>();
                    for (ChatButton btn : row){
                        line.add(new InlineKeyboardButton().setText(btn.getText()).setCallbackData(btn.getCommand()));
                    }
                    buttons.add(line);
                }

                markup.setKeyboard(buttons);
                sendMessage.setReplyMarkup(markup);
            }

            handler.executeWithQueue(sendMessage).thenAccept((resp)->{
                if (resp != null){
                    lastIds.put(resp.getChatId().toString(), resp.getMessageId());
                }
            });
        }

        if(message.getMedia() != null){
            lastIds.remove(message.getChatId());

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
                handler.executeWithQueue(group);
                return;
            }

            for (Media media : message.getMedia()){
                PartialBotApiMethod method = getSendMethod(media);

                if(method instanceof SendPhoto){
                    ((SendPhoto) method).setChatId(message.getChatId());
                    handler.executeWithQueue((SendPhoto)method);
                }

                if(method instanceof SendVideo){
                    ((SendVideo) method).setChatId(message.getChatId());
                    handler.executeWithQueue((SendVideo)method);
                }

                if(method instanceof SendAudio){
                    ((SendAudio) method).setChatId(message.getChatId());
                    handler.executeWithQueue((SendAudio)method);
                }

                if(method instanceof SendDocument){
                    ((SendDocument) method).setChatId(message.getChatId());
                    handler.executeWithQueue((SendDocument)method);
                }
            }
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

    private class MessageHandler extends TelegramLongPollingBot {

        private final int MESSAGES_IN_BULK = 30;

        private final String name;
        private final String token;
        private final Map<String, MessageHash> lastMessages = new HashMap<>();
        private final Queue<QueueMethod> queue = new LinkedBlockingQueue<>();
        private final ScheduledExecutorService timer;
        private ScheduledFuture<?> task;

        public MessageHandler(String name, String token){
            this.name = name;
            this.token = token;
            this.timer = Executors.newScheduledThreadPool(1);
        }

        public void startQueueTimer(){
            task = timer.scheduleWithFixedDelay(()->{
                int count = 0;

                for (int i = 0; i < MESSAGES_IN_BULK; i++){
                    QueueMethod method = queue.poll();

                    if (method != null){
                        method.execute(handler);
                        count++;
                        continue;
                    }

                    break;
                }

                if (count > 0){
                    Logger.info("Sent " + count + " messages in second");
                }
            }, 0, 1, TimeUnit.SECONDS);
        }

        public void stopQueueTimer(){
            timer.shutdown();
            task.cancel(true);
        }

        @Override
        public void onUpdateReceived(Update update) {
            if(update.hasMessage() && update.getMessage().hasText()) {
                lastIds.remove(update.getMessage().getChatId().toString());

                String text = update.getMessage().getText();
                MessageHash hash = lastMessages.computeIfAbsent(update.getMessage().getChatId().toString(),
                        (h)->new MessageHash(text));

                if (hash.checkMessage(text)){
                    executeCommand(
                            text,
                            update.getMessage().getChatId().toString(),
                            buildUser(update.getMessage().getFrom()));
                }
            }

            if(update.hasCallbackQuery()){
                String text = update.getCallbackQuery().getData();
                MessageHash hash = lastMessages.computeIfAbsent(update.getCallbackQuery().getMessage().getChatId().toString(),
                        (h)->new MessageHash(text));

                if (hash.checkMessage(text)){
                    executeCommand(
                            text,
                            update.getCallbackQuery().getMessage().getChatId().toString(),
                            buildUser(update.getCallbackQuery().getFrom()));
                }
            }
        }

        private void executeCommand(String message, String chatid, String username){
            String[] arr = message.split("@");
            String command = arr[0];

            if(arr.length > 1){
                if(!arr[1].equalsIgnoreCase(this.name)){
                    return;
                }
            }

            if(command.startsWith("/")) {
                CommandSender sender = new CommandSender(chatid, Type.TELEGRAM);
                sender.setUserName(username);
                CompletableFuture<Message> future = CompletableFuture.supplyAsync(()->
                        cmdManager.executeCommand(sender, message.substring(1)));

                future.thenAcceptAsync((response)->{
                    if(response != null){
                        response.setChatId(chatid);
                        sendMessage(response);
                    }
                });
            }
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(BotApiMethod<?> method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueueApiMethod(method, future));
            return future;
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(SendMediaGroup method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueueMediaGroup(method, future));
            return future;
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(SendPhoto method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueuePhoto(method, future));
            return future;
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(SendVideo method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueueVideo(method, future));
            return future;
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(SendAudio method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueueAudio(method, future));
            return future;
        }

        public CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> executeWithQueue(SendDocument method){
            CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> future = new CompletableFuture<>();
            queue.offer(new QueueDocument(method, future));
            return future;
        }

        private String buildUser(User user){
            if(user != null) return "[" + user.getId() + "@" + user.getUserName() + "](" + user.getFirstName() + " " + user.getLastName() + ")";
            return "NULL";
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

    private static class MessageHash {

        private String message;
        private long time;

        public MessageHash(String message) {
            this.message = message;
            this.time = 0;
        }

        public String getMessage() {
            return message;
        }

        public boolean checkMessage(String message){
            if (!this.message.equals(message) || isTimeExpired()){
                this.message = message;
                this.time = System.currentTimeMillis();
                return true;
            }

            return false;
        }

        public boolean isTimeExpired(){
            return System.currentTimeMillis() - time > 1000;
        }
    }

    private interface QueueMethod {
        void execute(MessageHandler handler);
    }

    private static class QueueApiMethod implements QueueMethod {

        private final BotApiMethod<?> method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueueApiMethod(BotApiMethod<?> method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete((org.telegram.telegrambots.meta.api.objects.Message) handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }

    private static class QueueMediaGroup implements QueueMethod {

        private final SendMediaGroup method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueueMediaGroup(SendMediaGroup method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete((org.telegram.telegrambots.meta.api.objects.Message) handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }

    private static class QueuePhoto implements QueueMethod {

        private final SendPhoto method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueuePhoto(SendPhoto method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete(handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }

    private static class QueueVideo implements QueueMethod {

        private final SendVideo method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueueVideo(SendVideo method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete(handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }

    private static class QueueAudio implements QueueMethod {

        private final SendAudio method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueueAudio(SendAudio method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete(handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }

    private static class QueueDocument implements QueueMethod {

        private final SendDocument method;
        private final CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response;

        public QueueDocument(SendDocument method, CompletableFuture<org.telegram.telegrambots.meta.api.objects.Message> response) {
            this.method = method;
            this.response = response;
        }

        @Override
        public void execute(MessageHandler handler) {
            try {
                response.complete(handler.execute(method));
            } catch (TelegramApiException e) {
                Logger.error("Cannot execute API method: " + e.getMessage());
                response.complete(null);
            }
        }
    }
}
