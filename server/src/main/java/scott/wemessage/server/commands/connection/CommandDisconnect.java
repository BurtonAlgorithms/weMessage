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

import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.connection.Device;

public class CommandDisconnect extends ConnectionCommand {

    public CommandDisconnect(CommandManager manager){
        super(manager, "disconnect", "Disconnects a device from the weServer", new String[]{ "killdevice" });
    }

    public void execute(String[] args){
        if (args.length == 0){
            ServerLogger.log("Please provide an IP of a device to disconnect.");
            return;
        }
        Device device = getDeviceManager().getDeviceByAddress(args[0]);

        if(device == null){
            ServerLogger.log("Device with IP Address: " + args[0] + " is not connected.");
            return;
        }
        getDeviceManager().removeDevice(device, DisconnectReason.FORCED, null);
    }
}
