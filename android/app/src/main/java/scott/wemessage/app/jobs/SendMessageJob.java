package scott.wemessage.app.jobs;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.commons.utils.StringUtils;

public class SendMessageJob extends Job {

    public static final String TAG = "weMessageSendMmsMessageJob";

    //todo compress image ; diff levels of compression
    //todo fix multiple recipients

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        MmsMessage mmsMessage = weMessage.get().getMmsManager().getMmsMessage(params.getExtras().getString("messageId", null));

        Settings settings = new Settings();
        settings.setUseSystemSending(true);
        settings.setDeliveryReports(true);

        final Transaction transaction = new Transaction(getContext(), settings);
        final String taskIdentifier = params.getExtras().getString("taskIdentifier", null);

        try {
            final Message message = new Message();
            Chat chat = mmsMessage.getChat();
            HashMap<Attachment, FailReason> failedAttachments = new HashMap<>();

            message.setText(mmsMessage.getText());

            if (chat instanceof PeerChat) {
                message.setAddress(((PeerChat) chat).getHandle().getHandleID());
            } else if (chat instanceof GroupChat) {
                List<String> addresses = new ArrayList<>();

                for (Handle handle : ((GroupChat) chat).getParticipants()) {
                    addresses.add(handle.getHandleID());
                }
                message.setAddresses(addresses.toArray(new String[addresses.size()]));
            }

            try {
                for (Attachment a : mmsMessage.getAttachments()) {
                    File attachmentFile = a.getFileLocation().getFile();

                    if (!AndroidUtils.hasMemoryForOperation(attachmentFile.length())) {
                        failedAttachments.put(a, FailReason.MEMORY);
                        continue;
                    }

                    try {
                        message.addMedia(FileUtils.readBytesFromFile(attachmentFile), AndroidUtils.getMimeTypeStringFromPath(attachmentFile.getAbsolutePath()), a.getTransferName());
                    } catch (IOException ex) {
                        AppLogger.error("An error occurred while trying to read bytes from an attachment file", ex);
                        failedAttachments.put(a, FailReason.UNKNOWN);
                    }
                }
            } catch (OutOfMemoryError error) {
                System.gc();
                message.getParts().clear();

                for (Attachment a : mmsMessage.getAttachments()) {
                    failedAttachments.put(a, FailReason.MEMORY);
                }
            }

            final String threadId = StringUtils.isEmpty(chat.getIdentifier()) ? null : mmsMessage.getChat().getIdentifier();

/*
            if (threadId == null && !isMeInChat(message.getAddresses())){
                message.addAddress(weMessage.get().getCurrentSession().getSmsHandle().getHandleID());
            }
*/
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    transaction.sendNewMessage(message, threadId == null ? Transaction.NO_THREAD_ID : Long.valueOf(threadId), taskIdentifier);
                }
            });

            for (Attachment a : failedAttachments.keySet()) {
                weMessage.get().getMessageManager().alertAttachmentSendFailure(a, failedAttachments.get(a));
            }
        }catch (Exception ex){
            if (weMessage.get().getMmsManager().getMmsMessage(taskIdentifier) != null) {
                weMessage.get().getMessageManager().removeMessage(weMessage.get().getMmsManager().getMmsMessage(taskIdentifier), false);
            }

            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.NOT_SENT);
            AppLogger.error("An error occurred while trying to send an SMS or MMS message", ex);
        }

        return Result.SUCCESS;
    }

    public static void performJob(String taskIdentifier, String messageId){
        PersistableBundleCompat persistableBundleCompat = new PersistableBundleCompat();
        persistableBundleCompat.putString("taskIdentifier", taskIdentifier);
        persistableBundleCompat.putString("messageId", messageId);

        new JobRequest.Builder(SendMessageJob.TAG)
                .startNow()
                .setExtras(persistableBundleCompat)
                .build()
                .schedule();
    }

    private boolean isMeInChat(String[] addresses){
        for (String address : addresses){
            String parsed = Handle.parseHandleId(address);

            if (weMessage.get().getCurrentSession().getSmsHandle().getHandleID().equals(parsed)) return true;
        }

        return false;
    }
}