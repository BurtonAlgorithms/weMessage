package scott.wemessage.app.database;

import android.content.Context;

import java.io.File;

import scott.wemessage.app.weMessage;

public final class DatabaseManager {

    private static DatabaseManager instance;
    private MessageDatabase messageDatabase;
    private File attachmentFolder;

    public static synchronized DatabaseManager getInstance(Context context){
        if (instance == null){
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseManager(Context context){
        File attachmentFolder = new File(context.getFilesDir(), weMessage.ATTACHMENT_FOLDER_NAME);
        attachmentFolder.mkdir();

        this.messageDatabase = new MessageDatabase(context);
        this.attachmentFolder = attachmentFolder;
    }

    public MessageDatabase getMessageDatabase(){
        return messageDatabase;
    }

    public File getAttachmentFolder(){
        return attachmentFolder;
    }
}