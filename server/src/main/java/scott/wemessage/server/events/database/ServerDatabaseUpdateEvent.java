package scott.wemessage.server.events.database;

import java.sql.Connection;

import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class ServerDatabaseUpdateEvent extends Event {

    private DatabaseManager databaseManager;

    public ServerDatabaseUpdateEvent(EventManager eventManager, DatabaseManager databaseManager){
        super(eventManager);
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    public Connection getServerDatabaseConnection(){
        return databaseManager.getServerDatabaseConnection();
    }
}