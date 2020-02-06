package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.bot.Bot;

import java.util.HashMap;
import java.util.Map;

public final class BotManager {

    private static Map<Bot.Type, Bot> bots = new HashMap<>();

    public static Bot getBot(Bot.Type type){
        return bots.get(type);
    }

    public static void registerBot(Bot.Type type, Bot instance){
        bots.put(type, instance);
    }

    public static Map<Bot.Type, Bot> getBots(){
        return bots;
    }

}
