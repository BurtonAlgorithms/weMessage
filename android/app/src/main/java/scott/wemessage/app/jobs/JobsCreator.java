package scott.wemessage.app.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class JobsCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag){
            case SendMessageJob.TAG:
                return new SendMessageJob();
            case SyncContactsJob.TAG:
                return new SyncContactsJob();
            case SyncMessagesJob.TAG:
                return new SyncMessagesJob();
            default:
                return null;
        }
    }
}