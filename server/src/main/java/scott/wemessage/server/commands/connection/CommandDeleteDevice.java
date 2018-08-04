package scott.wemessage.server.commands.connection;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.database.DatabaseManager;

public class CommandDeleteDevice extends ConnectionCommand {

    public CommandDeleteDevice(CommandManager manager){
        super(manager, "deletedevice", "Deletes device information from the weServer's database", new String[]{ "delete", "cleardevice", "cleardeviceinfo" });
    }

    public void execute(String[] args) {
        if (args.length == 0) {
            ServerLogger.log("You need to provide the name of the device to delete from the weServer's database.");
            return;
        }
        try {
            DatabaseManager databaseManager = getCommandManager().getMessageServer().getDatabaseManager();
            String deviceId = databaseManager.getDeviceIdByName(args[0]);

            if (deviceId == null) {
                ServerLogger.log("Device with name: " + args[0] + " was not found. Make sure your arguments are surrounded in \"quotation marks.\"");
                ServerLogger.log("Example Usage: deletedevice \"Google Pixel XL\"");
                return;
            }

            if (getDeviceManager().getDeviceById(deviceId) != null){
                ServerLogger.log("This device is currently connected. Please disconnect the device to remove the weServer stored data for it!");
                return;
            }

            databaseManager.deleteDevice(deviceId);
            ServerLogger.log("Device successfully deleted from weServer's storage database.");
        }catch (Exception ex){
            ServerLogger.error("An error occurred while deleting this device", ex);
        }
    }
}