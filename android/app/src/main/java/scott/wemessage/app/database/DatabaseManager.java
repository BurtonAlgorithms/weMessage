package scott.wemessage.app.database;

import android.content.Context;

public final class DatabaseManager {

    private static DatabaseManager instance;
    private Context context;
    private MessageDatabase messageDatabase;

    public static synchronized DatabaseManager getInstance(Context context){
        if (instance == null){
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseManager(Context context){
        this.context = context;
        this.messageDatabase = new MessageDatabase(context);
    }

    public MessageDatabase getMessageDatabase(){
        return messageDatabase;
    }
}