package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.CommandManager;

public class CommandClear extends CoreCommand {

    public CommandClear(CommandManager manager){
        super(manager, "clear", "Clears the terminal (console)", new String[]{ "clearconsole", "clearterminal" });
    }

    public void execute(String[] args){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}