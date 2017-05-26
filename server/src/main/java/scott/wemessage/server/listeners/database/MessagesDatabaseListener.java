package scott.wemessage.server.listeners.database;

import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.database.DatabaseSnapshot;
import scott.wemessage.server.database.MessagesDatabase;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.database.MessagesDatabaseUpdateEvent;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.ServerLogger;

public class MessagesDatabaseListener extends Listener {

    public MessagesDatabaseListener() {
        super(MessagesDatabaseUpdateEvent.class);
    }

    public void onEvent(Event e){
        MessagesDatabaseUpdateEvent event = (MessagesDatabaseUpdateEvent) e;
        MessagesDatabase messagesDb = event.getMessagesDatabase();
        DeviceManager deviceManager = event.getMessageServer().getDeviceManager();
        DatabaseManager databaseManager  = event.getDatabaseManager();

        try {
            DatabaseSnapshot oldSnapshot = messagesDb.getLastDatabaseSnapshot();
            DatabaseSnapshot newSnapshot = new DatabaseSnapshot(messagesDb.getMessagesByAmount(messagesDb.MESSAGE_COUNT_LIMIT));

            for (Message message : newSnapshot.getMessages()){
                if (message == null) return;
                if ((message.getText() == null || message.getText().equals("")) && (message.getAttachments() == null || message.getAttachments().isEmpty())) return;

                if (oldSnapshot.getMessage(message.getGuid()) == null){
                    if (message.isFromMe()){
                        databaseManager.queueMessage(message.getGuid(), true);

                        for (Device device : deviceManager.getDevices().values()){
                            device.updateOutgoingMessage(message);
                        }
                    }else {
                        databaseManager.queueMessage(message.getGuid(), false);

                        for (Device device : deviceManager.getDevices().values()){
                            device.sendOutgoingMessage(message);
                        }
                    }
                }else {
                    Message oldMessage = oldSnapshot.getMessage(message.getGuid());

                    boolean comparison = isMessageSame(oldMessage, message);

                    if (!comparison){
                        for (Device device : deviceManager.getDevices().values()){
                            device.updateOutgoingMessage(message);
                        }
                    }
                }
            }
            messagesDb.setLastDatabaseSnapshot(newSnapshot);
        }catch(Exception ex){
            ServerLogger.error("An error occurred while checking the Messages database for updates.", ex);
        }
    }

    private boolean isMessageSame(Message one, Message two){
        if(one.getDateSent() != two.getDateSent()) return false;
        if(one.getDateDelivered() != two.getDateSent()) return false;
        if(one.getDateRead() != two.getDateRead()) return false;
        if(one.hasErrored() != two.hasErrored()) return false;
        if(one.isFinished() != two.isFinished()) return false;

        return true;
    }
}