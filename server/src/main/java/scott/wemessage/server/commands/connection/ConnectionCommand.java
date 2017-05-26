package scott.wemessage.server.commands.connection;

import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.DeviceManager;

public abstract class ConnectionCommand extends Command {

    public ConnectionCommand(CommandManager commandManager, String name, String description, String[] aliases){
        super(commandManager, name, description, aliases);
    }

    public DeviceManager getDeviceManager(){
        return getCommandManager().getMessageServer().getDeviceManager();
    }
}
