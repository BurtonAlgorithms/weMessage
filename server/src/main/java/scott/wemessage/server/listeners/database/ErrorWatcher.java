package scott.wemessage.server.listeners.database;

import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.database.ServerDatabaseUpdateEvent;
import scott.wemessage.server.utils.LoggingUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class ErrorWatcher extends Listener {

    private final String TAG = "Error Watcher";
    
    public ErrorWatcher() {
        super(ServerDatabaseUpdateEvent.class);
    }
    
    public void onEvent(Event e){
        try {
            ServerDatabaseUpdateEvent event = (ServerDatabaseUpdateEvent) e;

            String selectQuery = "SELECT * FROM " + event.getDatabaseManager().TABLE_ERRORS + " ORDER BY ROWID ASC LIMIT 1";
            Statement findStatement = event.getServerDatabaseConnection().createStatement();
            ResultSet resultSet = findStatement.executeQuery(selectQuery);

            if (resultSet.isClosed()) return;

            String errorMessage = resultSet.getString(event.getDatabaseManager().COLUMN_ERROR_MESSAGE);
            String scriptCaller = resultSet.getString(event.getDatabaseManager().COLUMN_ERRORED_SCRIPT);

            String clearStatementString = "DELETE FROM " + event.getDatabaseManager().TABLE_ERRORS + " WHERE " + event.getDatabaseManager().COLUMN_ERROR_MESSAGE + " = ?";
            PreparedStatement clearStatement = event.getServerDatabaseConnection().prepareStatement(clearStatementString);
            clearStatement.setString(1, errorMessage);

            LoggingUtils.log(LoggingUtils.Level.ERROR, "AppleScript Error", "An unexpected error occurred while executing AppleScript " + scriptCaller);
            LoggingUtils.emptyLine();
            LoggingUtils.log(errorMessage);
            LoggingUtils.emptyLine();
            clearStatement.execute();

            resultSet.close();
            findStatement.close();
            clearStatement.close();
        }catch(Exception ex){
            LoggingUtils.error(TAG, "An error occurred while watching the error database table. Shutting down!", ex);
            e.getMessageServer().shutdown(-1, false);
        }
    }
}