package scott.wemessage.app.sms.services;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class SendMessageJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag){
            case SendMessageJob.TAG:
                return new SendMessageJob();
            default:
                return null;
        }
    }
}