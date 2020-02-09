package ru.nanit.vasyascheduler.data.chat;

import java.util.Collection;
import java.util.HashSet;

public class Message {

    private static final String NULL_VALUE = "NULL";

    private String chatId = NULL_VALUE;
    private String author = NULL_VALUE;
    private String message;
    private Collection<Media> media;
    private Keyboard keyboard;
    private boolean editMessage;
    private boolean removeLastId;

    public Message(){ }

    public Message(String message){
        this.message = message;
    }

    public boolean isEditMessage() {
        return editMessage;
    }

    public boolean isRemoveLastId() {
        return removeLastId;
    }

    public void setRemoveLastId(boolean removeLastId) {
        this.removeLastId = removeLastId;
    }

    public void setEditMessage(boolean editMessage) {
        this.editMessage = editMessage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId){
        this.chatId = chatId;
    }

    public String getAuthor(){
        return author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Collection<Media> getMedia(){
        return media;
    }

    public void addMedia(Media media){
        if(this.media == null){
            this.media = new HashSet<>();
        }

        this.media.add(media);
    }

    public Keyboard getKeyboard(){
        return keyboard;
    }

    public void setKeyboard(Keyboard keyboard){
        this.keyboard = keyboard;
    }
}
