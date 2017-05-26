package scott.wemessage.server.commands.connection;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.utils.LoggingUtils;

public class CommandDevices extends ConnectionCommand {

    public CommandDevices(CommandManager manager){
        super(manager, "devices", "Lists all devices connected to the server", new String[]{ "connections" });
    }

    public void execute(String[] args){
        LoggingUtils.log("Connected weMessage Devices");

        int i = 0;
        if (getDeviceManager().getDevices().size() == 0){
            LoggingUtils.emptyLine();
            LoggingUtils.log("No connected devices.");
            return;
        }

        for(Device device : getDeviceManager().getDevices().values()){
            i++;
            LoggingUtils.emptyLine();
            LoggingUtils.log("Device " + i + " -  IP: " + device.getAddress() + "  Type: " + device.getDeviceType().getTypeName());
        }
    }
}
