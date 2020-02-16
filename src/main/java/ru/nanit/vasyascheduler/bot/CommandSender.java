package ru.nanit.vasyascheduler.bot;

public class CommandSender {

    private String id;
    private Bot.Type botType;
    private String userName;

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

    public void setId(String id) {
        this.id = id;
    }

    public void setBotType(Bot.Type botType) {
        this.botType = botType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
