package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.commons.utils.StringUtils;

import java.util.Arrays;

public class CommandAliases extends CoreCommand {

    public CommandAliases(CommandManager manager){
        super(manager, "aliases", "Returns the aliases of a given command", new String[]{"commandnames", "alternatename"});
    }

    public void execute(String[] args){
        if (args.length == 0){
            ServerLogger.log("Please enter a command name to look up the aliases for.");
            return;
        }
        Command command = getCommandManager().getCommand(args[0]);

        if(command == null){
            ServerLogger.log("The command " + args[0] + " could not be found. Are you sure you typed it in correctly?");
            return;
        }

        ServerLogger.log("Aliases for Command " + command.getName() + ": " + StringUtils.join(Arrays.asList(command.getAliases()), ", ", 2));
    }
}