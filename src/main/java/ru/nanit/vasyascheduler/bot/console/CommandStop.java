package ru.nanit.vasyascheduler.bot.console;

import ru.nanit.vasyascheduler.VasyaScheduler;
import ru.nanit.vasyascheduler.api.util.Logger;

public class CommandStop implements ConsoleCommand {

    private VasyaScheduler instance;

    public CommandStop(VasyaScheduler instance){
        this.instance = instance;
    }

    @Override
    public void onCommand(String command, String... args) {
        try{
            instance.exit();
        } catch (Exception e){
            Logger.error("Error while stopping bot: ", e);
        }
    }

}
