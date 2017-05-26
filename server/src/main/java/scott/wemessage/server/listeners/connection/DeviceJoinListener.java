package scott.wemessage.server.listeners.connection;

import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.DeviceJoinEvent;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.utils.LoggingUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class DeviceJoinListener extends Listener {

    public DeviceJoinListener(){
        super(DeviceJoinEvent.class);
    }

    public void onEvent(Event e){
        DeviceJoinEvent event = (DeviceJoinEvent) e;

        try {
            DatabaseManager db = event.getMessageServer().getDatabaseManager();
            String selectQuery = "SELECT * FROM " + db.TABLE_DEVICES + " WHERE " + db.COLUMN_DEVICE_ID + " = ?";
            PreparedStatement findStatement = db.getServerDatabaseConnection().prepareStatement(selectQuery);
            findStatement.setString(1, event.getDevice().getDeviceId());
            ResultSet resultSet = findStatement.executeQuery();

            if (!resultSet.isBeforeFirst()){
                String insertStatementString = "INSERT INTO " + db.TABLE_DEVICES + "(" + db.COLUMN_DEVICE_ID + ", " + db.COLUMN_DEVICE_ADDRESS + ") VALUES (?, ?)";
                PreparedStatement insertStatement = db.getServerDatabaseConnection().prepareStatement(insertStatementString);
                insertStatement.setString(1, event.getDevice().getDeviceId());
                insertStatement.setString(2, event.getDevice().getAddress());

                insertStatement.executeUpdate();
                insertStatement.close();
                db.getServerDatabaseConnection().commit();
            }else {
                String insertStatementString = "UPDATE " + db.TABLE_DEVICES + " SET " + db.COLUMN_DEVICE_ADDRESS + " = ? WHERE " + db.COLUMN_DEVICE_ID + " = ?";
                PreparedStatement insertStatement = db.getServerDatabaseConnection().prepareStatement(insertStatementString);
                insertStatement.setString(1, event.getDevice().getAddress());
                insertStatement.setString(2, event.getDevice().getDeviceId());

                insertStatement.executeUpdate();
                insertStatement.close();
                db.getServerDatabaseConnection().commit();
            }

            HashMap<String, Boolean>queuedMessages = db.getQueuedMessages(event.getDevice().getDeviceId());
            for (String guid : queuedMessages.keySet()){
                Message message = event.getMessageServer().getMessagesDatabase().getMessageByGuid(guid);

                if (!queuedMessages.get(guid)) {
                    event.getDevice().sendOutgoingMessage(message);
                }else {
                    event.getDevice().updateOutgoingMessage(message);
                }
                db.unQueueMessage(guid, event.getDevice().getDeviceId());
            }

            for (JSONAction jsonAction : db.getQueuedActions(event.getDevice().getDeviceId())){
                event.getDevice().sendOutgoingAction(jsonAction);
                db.unQueueAction(jsonAction, event.getDevice().getDeviceId());
            }

            resultSet.close();
            findStatement.close();
        }catch(Exception ex){
            LoggingUtils.error("An error occurred while connecting Device: " + event.getDevice().getAddress() + " to the server. \nPlease disconnect it or it will not work as expected.", ex);
        }
    }
}