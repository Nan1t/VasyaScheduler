package ru.nanit.vasyascheduler.bot.console;

import java.text.NumberFormat;

public class CommandStats implements ConsoleCommand {

    private Runtime runtime = Runtime.getRuntime();

    @Override
    public void onCommand(String command, String... args) {
        System.out.println();

        NumberFormat format = NumberFormat.getInstance();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        System.out.println("Free memory: " + format.format(freeMemory / 1024) + " bytes");
        System.out.println("Allocated memory: " + format.format(allocatedMemory / 1024) + " bytes");
        System.out.println("Max memory: " + format.format(maxMemory / 1024) + " bytes");
        System.out.println("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + " bytes");
        System.out.println("Active threads: " + Thread.activeCount());
        System.out.println();
    }
    
}
