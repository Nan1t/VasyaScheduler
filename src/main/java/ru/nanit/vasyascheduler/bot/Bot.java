package ru.nanit.vasyascheduler.bot;

import ru.nanit.vasyascheduler.bot.types.BotTelegram;
import ru.nanit.vasyascheduler.data.chat.Message;

public interface Bot {

    void enable();

    void disable();

    void sendMessage(final Message message);

    enum Type{
        TELEGRAM(BotTelegram.class, (byte) 0),
        FACEBOOK(BotTelegram.class, (byte) 2);

        Class<? extends Bot> type;
        byte id;

        Type(Class<? extends Bot> type, byte id){
            this.type = type;
            this.id = id;
        }

        public Class<? extends Bot> getType(){
            return type;
        }

        public byte getId(){
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
