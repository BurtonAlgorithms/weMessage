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

package scott.wemessage.server.listeners.connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.DeviceJoinEvent;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.weMessage;

public class DeviceJoinListener extends Listener {

    public DeviceJoinListener(){
        super(DeviceJoinEvent.class);
    }

    public void onEvent(Event e){
        DeviceJoinEvent event = (DeviceJoinEvent) e;

        try {
            List<String> accounts = e.getMessageServer().getMessagesDatabase().getAccounts();
            String account = e.getMessageServer().getConfiguration().getAccountEmail().toLowerCase();

            if (!accounts.contains(account) && !event.getDeviceManager().getPastDeviceConnections().containsKey(event.getDevice().getDeviceId())){
                event.getDevice().sendOutgoingMessage(weMessage.JSON_NO_ACCOUNTS_FOUND_SERVER, "", String.class);
            }

            DatabaseManager db = event.getMessageServer().getDatabaseManager();
            String selectQuery = "SELECT * FROM " + db.TABLE_DEVICES + " WHERE " + db.COLUMN_DEVICE_ID + " = ?";
            PreparedStatement findStatement = db.getServerDatabaseConnection().prepareStatement(selectQuery);
            findStatement.setString(1, event.getDevice().getDeviceId());
            ResultSet resultSet = findStatement.executeQuery();

            if (!resultSet.isBeforeFirst()){
                String insertStatementString = "INSERT INTO " + db.TABLE_DEVICES + "(" + db.COLUMN_DEVICE_ID + ", "
                        + db.COLUMN_DEVICE_ADDRESS + ", " + db.COLUMN_DEVICE_LAST_EMAIL + ", " + db.COLUMN_DEVICE_NAME + ") VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = db.getServerDatabaseConnection().prepareStatement(insertStatementString);
                insertStatement.setString(1, event.getDevice().getDeviceId());
                insertStatement.setString(2, event.getDevice().getAddress());
                insertStatement.setString(3, event.getMessageServer().getConfiguration().getAccountEmail());
                insertStatement.setString(4, event.getDevice().getDeviceName());

                insertStatement.executeUpdate();
                insertStatement.close();
            }else {
                String insertStatementString = "UPDATE " + db.TABLE_DEVICES + " SET " + db.COLUMN_DEVICE_ADDRESS + " = ?, " + db.COLUMN_DEVICE_LAST_EMAIL + " = ?, "
                        + db.COLUMN_DEVICE_NAME + " = ? WHERE " + db.COLUMN_DEVICE_ID + " = ?";
                PreparedStatement insertStatement = db.getServerDatabaseConnection().prepareStatement(insertStatementString);
                insertStatement.setString(1, event.getDevice().getAddress());
                insertStatement.setString(2, event.getMessageServer().getConfiguration().getAccountEmail());
                insertStatement.setString(3, event.getDevice().getDeviceName());
                insertStatement.setString(4, event.getDevice().getDeviceId());

                insertStatement.executeUpdate();
                insertStatement.close();
            }

            db.setRegistrationToken(event.getDevice().getDeviceId(), event.getDevice().getRegistrationToken());

            HashMap<String, Boolean>queuedMessages = db.getQueuedMessages(event.getDevice().getDeviceId());
            for (String guid : queuedMessages.keySet()){
                Message message = event.getMessageServer().getMessagesDatabase().getMessageByGuid(guid);

                if (message != null) {
                    if (!queuedMessages.get(guid)) {
                        event.getDevice().sendOutgoingMessage(message);
                    } else {
                        event.getDevice().updateOutgoingMessage(message, false);
                    }
                    db.unQueueMessage(guid, event.getDevice().getDeviceId());
                }
            }

            for (JSONAction jsonAction : db.getQueuedActions(event.getDevice().getDeviceId())){
                event.getDevice().sendOutgoingAction(jsonAction);
                db.unQueueAction(jsonAction, event.getDevice().getDeviceId());
            }

            resultSet.close();
            findStatement.close();

            event.getDeviceManager().getPastDeviceConnections().put(event.getDevice().getDeviceId(), event.getDevice().getAddress());
        }catch(Exception ex){
            ServerLogger.error("An error occurred while connecting Device: " + event.getDevice().getAddress() + " to the weServer. \nPlease disconnect it or it will not work as expected.", ex);
        }
    }
}