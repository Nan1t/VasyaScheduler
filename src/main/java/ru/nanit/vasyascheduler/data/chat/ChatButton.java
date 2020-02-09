package ru.nanit.vasyascheduler.data.chat;

public class ChatButton {

    private String text;
    private String command;

    public ChatButton(String text, String command){
        this.text = text;
        this.command = command;
    }

    public String getText() {
        return text;
    }

    public String getCommand() {
        return command;
    }
}
