package ru.nanit.vasyascheduler.bot;

import ru.nanit.vasyascheduler.bot.types.BotTelegram;
import ru.nanit.vasyascheduler.data.chat.Message;

public interface Bot {

    void enable();

    void disable();

    void sendMessage(final Message message);

    enum Type{
        TELEGRAM(BotTelegram.class, 0),
        FACEBOOK(BotTelegram.class, 2);

        Class<? extends Bot> type;
        int id;

        Type(Class<? extends Bot> type, int id){
            this.type = type;
            this.id = id;
        }

        public Class<? extends Bot> getType(){
            return type;
        }

        public int getId(){
            return id;
        }

        public static Type fromId(int id){
            switch (id){
                default:
                    return null;
                case 0:
                    return Type.TELEGRAM;
                case 2:
                    return Type.FACEBOOK;
            }
        }
    }

}
