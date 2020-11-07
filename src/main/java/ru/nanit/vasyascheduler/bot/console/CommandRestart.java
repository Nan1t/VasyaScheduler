package ru.nanit.vasyascheduler.bot.console;

import ru.nanit.vasyascheduler.VasyaScheduler;
import ru.nanit.vasyascheduler.api.util.Logger;

public class CommandRestart implements ConsoleCommand {

    private VasyaScheduler instance;

    public CommandRestart(VasyaScheduler instance){
        this.instance = instance;
    }

    @Override
    public void onCommand(String command, String... args) {
        try {
            instance.stop();
            instance.start();
        } catch (Exception e){
            Logger.info("Restarting error: " + e.getMessage());
        }
    }
    
}
