package scott.wemessage.server.commands.connection;

import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;

public class CommandDisconnectAll extends ConnectionCommand {

    public CommandDisconnectAll(CommandManager manager) {
        super(manager, "disconnectall", "Disconnects all devices from the weServer", new String[]{"killall"});
    }

    public void execute(String[] args) {
        if (getDeviceManager().getDevices().size() == 0){
            ServerLogger.log("There are no devices connected.");
        }

        for (Device device : getDeviceManager().getDevices().values()) {
            getDeviceManager().removeDevice(device, DisconnectReason.FORCED, null);
        }
    }
}