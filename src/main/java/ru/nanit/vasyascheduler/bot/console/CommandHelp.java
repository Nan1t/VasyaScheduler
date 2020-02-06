package ru.nanit.vasyascheduler.bot.console;

public class CommandHelp implements ConsoleCommand {

    private final String[] helpMsg = new String[]{
            "Console commands help:",
            "",
            "help, ?                  Shows this help message",
            "status, stats, info      Shows app status (MEM, CPU usage)",
            "stop, exit               Safe stopping bot",
            "restart, reload          Restart bot",
    };

    @Override
    public void onCommand(String command, String... args) {
        System.out.println();
        for (String s : helpMsg){
            System.out.println(s);
        }
        System.out.println();
    }
    
}
