/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.server.commands.connection;

import java.util.List;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;

public class CommandExistingDevices extends ConnectionCommand {

    public CommandExistingDevices(CommandManager manager){
        super(manager, "existingdevices", "Lists all devices that have ever connected to the weServer", new String[]{ "alldevices", "allconnections", "previousdevices" });
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
