package scott.wemessage.app;

import android.app.Application;

import java.io.File;

import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.objects.Account;

public class WeApp extends Application {

    private Account currentAccount;
    private File attachmentFolder;
    private MessageDatabase messageDatabase;
    private static WeApp instance;

    public static WeApp get(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        File attachmentFolder = new File(getFilesDir(), weMessage.ATTACHMENT_FOLDER_NAME);
        attachmentFolder.mkdir();

        this.attachmentFolder = attachmentFolder;
        this.messageDatabase = new MessageDatabase(this);

        instance = this;
    }

    public synchronized Account getCurrentAccount(){
        if (currentAccount == null) throw new MessageDatabase.AccountNotLoggedInException();

        return currentAccount;
    }

    public synchronized void setCurrentAccount(Account account){
        this.currentAccount = account;
    }

    public synchronized File getAttachmentFolder(){
        return attachmentFolder;
    }

    public synchronized MessageDatabase getMessageDatabase(){
        return messageDatabase;
    }
}