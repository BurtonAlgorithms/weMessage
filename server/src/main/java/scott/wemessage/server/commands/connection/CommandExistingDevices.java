package scott.wemessage.server.commands.connection;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.utils.LoggingUtils;

import java.util.List;

public class CommandExistingDevices extends ConnectionCommand {

    public CommandExistingDevices(CommandManager manager){
        super(manager, "existingdevices", "Lists all devices that have ever connected to the server", new String[]{ "alldevices", "allconnections", "previousdevices" });
    }

    public void execute(String[] args) {
        try {
            int i = 0;
            List<String> existingDevices = getDeviceManager().getMessageServer().getDatabaseManager().getAllExistingDevices();

            LoggingUtils.log("All Existing weMessage Devices");
            if (existingDevices.isEmpty()) {
                LoggingUtils.emptyLine();
                LoggingUtils.log("No devices have ever connected to this weServer instance.");
                return;
            }

            for (String s : existingDevices) {
                i++;
                LoggingUtils.emptyLine();

                Device device = getDeviceManager().getDeviceById(s);
                if (device != null){
                    LoggingUtils.log("Device " + i + " -  IP: " + device.getAddress() + "  Connected: True  Type: " + device.getDeviceType().getTypeName());
                }else {
                    String ip = getDeviceManager().getMessageServer().getDatabaseManager().getAddressByDeviceId(s);
                    LoggingUtils.log("Device " + i + " -  IP: " + ip + "  Connected: False");
                }
            }
        }catch (Exception ex){
            LoggingUtils.error("An error occurred while fetching all existing weServer devices", ex);
        }
    }
}
