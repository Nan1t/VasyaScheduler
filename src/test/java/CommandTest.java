import ru.nanit.vasyascheduler.services.ConsoleManager;

import java.util.Scanner;

public class CommandTest {

    private ConsoleManager consoleManager;

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()){
            String line = scanner.nextLine();

            System.out.println("Resp " + line);
        }
    }

    private void registerConsoleCommands(){
        consoleManager = new ConsoleManager();
        consoleManager.startListening();
    }

}
