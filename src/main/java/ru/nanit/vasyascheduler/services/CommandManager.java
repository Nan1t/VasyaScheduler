package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.bot.commands.CommandHandler;
import ru.nanit.vasyascheduler.data.chat.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class CommandManager {

    private Map<String, CommandHandler> handlers = new HashMap<>();

    public void registerCommand(CommandHandler handler, String command, String... aliases){
        handlers.put(command.toLowerCase(), handler);

        if(aliases != null){
            for(String cmd : aliases){
                registerCommand(handler, cmd);
            }
        }
    }

    public Message executeCommand(CommandSender sender, String line){
        Logger.info("[" + sender.getBotType() + "] User " + sender.getId() + " entered command '" + line + "'");

        String[] arr = line.split(" ");

        if(arr.length > 0){
            String command = arr[0];
            String[] args = (arr.length > 1) ? Arrays.copyOfRange(arr, 1, arr.length) : new String[0];

            CommandHandler handler = handlers.get(command.toLowerCase());

            if(handler != null){
                try{
                    return handler.execute(sender, command, args);
                } catch (Exception e){
                    Logger.error("Error while executing bot command: ", e);
                }
            }
        }

        return null;
    }

}
