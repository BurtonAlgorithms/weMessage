package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.utils.LoggingUtils;

public class CommandInfo extends CoreCommand {

    public CommandInfo(CommandManager manager){
        super(manager, "info", "Displays version info about the weMessage Server", new String[]{ "version", "?" });
    }

    public void execute(String[] args){
        ServerConfiguration configuration = getCommandManager().getMessageServer().getConfiguration();

        LoggingUtils.log("weServer Info");
        LoggingUtils.emptyLine();
        LoggingUtils.log("Version:  " + configuration.getVersion());
        LoggingUtils.log("Release Number:  " + configuration.getBuildVersion());
        LoggingUtils.log("Port:  " + configuration.getPort());
    }
}
