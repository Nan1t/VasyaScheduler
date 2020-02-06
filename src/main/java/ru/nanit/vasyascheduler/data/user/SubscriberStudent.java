package ru.nanit.vasyascheduler.data.user;

public class SubscriberStudent extends BotUser {

    private String schedule;

    public SubscriberStudent(String schedule){
        this.schedule = schedule;
    }

    public String getSchedule(){
        return schedule;
    }
}
