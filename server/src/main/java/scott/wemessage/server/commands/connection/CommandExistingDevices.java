package scott.wemessage.server.commands.connection;

import java.util.List;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;

public class CommandExistingDevices extends ConnectionCommand {

    public CommandExistingDevices(CommandManager manager){
        super(manager, "existingdevices", "Lists all devices that have ever connected to the server", new String[]{ "alldevices", "allconnections", "previousdevices" });
    }

    public void execute(String[] args) {
        try {
            int i = 0;
            List<String> existingDevices = getDeviceManager().getMessageServer().getDatabaseManager().getAllExistingDevices();

            ServerLogger.log("All Existing weMessage Devices");
            if (existingDevices.isEmpty()) {
                ServerLogger.emptyLine();
                ServerLogger.log("No devices have ever connected to this weServer instance.");
                return;
            }

            for (String s : existingDevices) {
                i++;
                ServerLogger.emptyLine();

                Device device = getDeviceManager().getDeviceById(s);
                if (device != null){
                    ServerLogger.log("Device " + device.getDeviceName() + " -  IP: " + device.getAddress() + "  Connected: True  Type: " + device.getDeviceType().getTypeName());
                }else {
                    String ip = getDeviceManager().getMessageServer().getDatabaseManager().getAddressByDeviceId(s);
                    String name = getDeviceManager().getMessageServer().getDatabaseManager().getNameByDeviceId(s);
                    ServerLogger.log("Device " + name + " -  IP: " + ip + "  Connected: False");
                }
            }
        }catch (Exception ex){
            ServerLogger.error("An error occurred while fetching all existing weServer devices", ex);
        }
    }
}
