package scott.wemessage.app.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.weMessage;

public class SyncMessagesJob extends Job {

    public static final String TAG = "weMessageSyncMessagesJob";
    private static AtomicBoolean isSyncRunning = new AtomicBoolean(false);

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        if (weMessage.get().getCurrentSession().getSmsHandle() != null) {
            weMessage.get().getMmsDatabase().executeChatSync();

            for (SmsChat chat : weMessage.get().getMmsManager().getChats().values()) {
                weMessage.get().getMmsDatabase().executeMessageSync(((Chat) chat).getIdentifier());
            }
        }

        isSyncRunning.set(false);
        return Result.SUCCESS;
    }

    public static void performSync(){
        if (isSyncRunning.get()) return;
        isSyncRunning.set(true);

        new JobRequest.Builder(SyncMessagesJob.TAG)
                .startNow()
                .build()
                .schedule();
    }
}
