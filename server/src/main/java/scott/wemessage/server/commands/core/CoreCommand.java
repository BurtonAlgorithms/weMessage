package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;

public abstract class CoreCommand extends Command {

    public CoreCommand(CommandManager commandManager, String name, String description, String[] aliases){
        super(commandManager, name, description, aliases);
    }
}
