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