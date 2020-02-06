package ru.nanit.vasyascheduler.bot;

public class CommandSender {

    private String id;
    private Bot.Type botType;

    public CommandSender(String id, Bot.Type botType){
        this.id = id;
        this.botType = botType;
    }

    public String getId() {
        return id;
    }

    public Bot.Type getBotType() {
        return botType;
    }
}
