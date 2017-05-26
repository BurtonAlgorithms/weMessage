package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.CommandManager;

public class CommandStop extends CoreCommand {

    public CommandStop(CommandManager manager){
        super(manager, "stop", "Stops the weMessage Server", new String[]{ "shutdown", "end" });
    }

    public void execute(String[] args){
        getCommandManager().getMessageServer().shutdown(0, true);
    }
}
