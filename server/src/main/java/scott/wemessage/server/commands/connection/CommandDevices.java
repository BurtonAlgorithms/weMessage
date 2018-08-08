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

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;

public class CommandDevices extends ConnectionCommand {

    public CommandDevices(CommandManager manager){
        super(manager, "devices", "Lists all devices connected to the weServer", new String[]{ "connections" });
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
            ServerLogger.log("Device " + device.getDeviceName() + " -  IP: " + device.getAddress() + "  Type: " + device.getDeviceType().getTypeName());
        }
    }
}
