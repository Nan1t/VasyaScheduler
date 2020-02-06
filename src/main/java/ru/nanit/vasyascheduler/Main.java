package ru.nanit.vasyascheduler;

import ru.nanit.vasyascheduler.api.util.LibLoader;
import ru.nanit.vasyascheduler.api.util.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger.info("Loading libraries...");
        Path root = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        //LibLoader.load(Paths.get(root.toString(), "libs"));
        new VasyaScheduler(root).start();
    }

}
