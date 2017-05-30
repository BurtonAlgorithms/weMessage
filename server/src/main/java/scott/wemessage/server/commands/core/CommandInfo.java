package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.ServerLogger;

public class CommandInfo extends CoreCommand {

    public CommandInfo(CommandManager manager){
        super(manager, "info", "Displays version info about the weServer", new String[]{ "version", "?" });
    }

    public void execute(String[] args){
        ServerConfiguration configuration = getCommandManager().getMessageServer().getConfiguration();

        ServerLogger.log("weServer Info");
        ServerLogger.emptyLine();
        ServerLogger.log("Version:  " + configuration.getVersion());
        ServerLogger.log("Release Number:  " + configuration.getBuildVersion());
        ServerLogger.log("Port:  " + configuration.getPort());
    }
}
