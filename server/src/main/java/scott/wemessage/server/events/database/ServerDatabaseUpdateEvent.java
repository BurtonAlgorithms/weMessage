package scott.wemessage.server.events.database;

import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

import java.sql.Connection;

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