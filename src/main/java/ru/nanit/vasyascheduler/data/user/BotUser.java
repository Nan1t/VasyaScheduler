package ru.nanit.vasyascheduler.data.user;

import ru.nanit.vasyascheduler.bot.Bot;

public abstract class BotUser {

    private int id;
    private String messengerId;
    private Bot.Type botType;

    public BotUser(){ }

    public BotUser(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(String messengerId) {
        this.messengerId = messengerId;
    }

    public Bot.Type getBotType() {
        return botType;
    }

    public void setBotType(Bot.Type botType) {
        this.botType = botType;
    }

    @Override
    public String toString(){
        return "BotUser@" + getBotType() + ":" + getMessengerId();
    }
}
