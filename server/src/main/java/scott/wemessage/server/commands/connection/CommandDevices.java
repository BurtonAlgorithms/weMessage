package scott.wemessage.server.commands.connection;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.ServerLogger;

public class CommandDevices extends ConnectionCommand {

    public CommandDevices(CommandManager manager){
        super(manager, "devices", "Lists all devices connected to the server", new String[]{ "connections" });
    }

    public void execute(String[] args){
        ServerLogger.log("Connected weMessage Devices");

        int i = 0;
        if (getDeviceManager().getDevices().size() == 0){
            ServerLogger.emptyLine();
            ServerLogger.log("No connected devices.");
            return;
        }

        for(Device device : getDeviceManager().getDevices().values()){
            i++;
            ServerLogger.emptyLine();
            ServerLogger.log("Device " + i + " -  IP: " + device.getAddress() + "  Type: " + device.getDeviceType().getTypeName());
        }
    }
}
