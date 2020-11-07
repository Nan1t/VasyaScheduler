package ru.nanit.vasyascheduler.services;

import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.console.ConsoleCommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class ConsoleManager {

    private Listener listener;
    private Map<String, ConsoleCommand> commands = new HashMap<>();

    public ConsoleManager(){
        listener = new Listener();
    }

    public void registerCommand(ConsoleCommand executor, String command, String... aliases){
        commands.put(command.toLowerCase(), executor);
        for (String alias : aliases){
            commands.put(alias.toLowerCase(), executor);
        }
    }

    public void startListening(){
        listener.start();
    }

    public void stopListening(){
        listener.stopListening();
    }

    private class Listener extends Thread {

        private Scanner scanner;

        void stopListening(){
            scanner.close();
        }

        @Override
        public void run(){
            scanner = new Scanner(System.in);

            while (scanner.hasNext()){
                String line = scanner.nextLine();
                String[] arr = line.split(" ");

                if(arr.length > 0){
                    try{
                        ConsoleCommand command = commands.get(arr[0].toLowerCase());

                        if(command != null){
                            command.onCommand(arr[0], (arr.length > 1) ? Arrays.copyOfRange(arr, 1, arr.length) : new String[0]);
                            continue;
                        }
                    } catch (Exception e){
                        Logger.error("Command '" + arr[0] + "' throws an exception: ", e);
                    }
                }

                System.out.println("Undefined command. Press help to get list of all commands");
            }
        }
    }
}
