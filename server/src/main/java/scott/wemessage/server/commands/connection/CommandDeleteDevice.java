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