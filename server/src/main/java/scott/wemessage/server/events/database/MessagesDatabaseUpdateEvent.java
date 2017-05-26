package scott.wemessage.server.events.database;

import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.database.MessagesDatabase;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

import java.sql.Connection;

public class MessagesDatabaseUpdateEvent extends Event {

    private MessagesDatabase messagesDatabase;

    public MessagesDatabaseUpdateEvent(EventManager eventManager, MessagesDatabase messagesDatabase){
        super(eventManager);
        this.messagesDatabase = messagesDatabase;
    }

    public MessagesDatabase getMessagesDatabase(){
        return messagesDatabase;
    }

    public DatabaseManager getDatabaseManager(){
        return messagesDatabase.getDatabaseManager();
    }

    public Connection getChatDatabaseConnection(){
        return messagesDatabase.getDatabaseManager().getChatDatabaseConnection();
    }
}
