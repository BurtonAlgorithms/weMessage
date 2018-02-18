package scott.wemessage.app.jobs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.commons.utils.StringUtils;

public class SendMessageJob extends Job {

    public static final String TAG = "weMessageSendMmsMessageJob";

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
                long finalSize = 0L;

                for (Attachment a : mmsMessage.getAttachments()){
                    finalSize += a.getFileLocation().getFile().length();
                }

                for (Attachment a : mmsMessage.getAttachments()) {
                    File attachmentFile = a.getFileLocation().getFile();

                    if (!AndroidUtils.hasMemoryForOperation(attachmentFile.length())) {
                        failedAttachments.put(a, FailReason.MEMORY);
                        continue;
                    }

                    try {
                        String type = a.getFileType();

                        if ((MimeType.getTypeFromString(type) == MimeType.IMAGE || type.equals("image/jpg") || type.equals("image/bmp")) && !type.equals("image/gif")) {
                            message.addMedia(compressImage(attachmentFile, finalSize), "image/jpeg", a.getTransferName());
                        }else {
                            message.addMedia(FileUtils.readBytesFromFile(attachmentFile), AndroidUtils.getMimeTypeStringFromPath(attachmentFile.getAbsolutePath()), a.getTransferName());
                        }

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

            final String threadId = StringUtils.isEmpty(chat.getIdentifier()) || !isIdentifierSms(chat.getIdentifier()) ? null : mmsMessage.getChat().getIdentifier();

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

    private byte[] compressImage(File file, long totalSize) throws IOException {
        double ratio = totalSize / weMessage.MAX_MMS_ATTACHMENT_SIZE;
        int quality;

        if (ratio < 0.5) quality = 100;
        else if (isInRange(0.5, ratio, 1.0)) quality = 90;
        else if (isInRange(1, ratio, 1.25)) quality = 85;
        else if (isInRange(1.25, ratio, 1.5)) quality = 80;
        else if (isInRange(1.75, ratio, 2.0)) quality = 75;
        else if (isInRange(2.0, ratio, 2.5)) quality = 70;
        else if (isInRange(2.5, ratio, 3.0)) quality = 65;
        else if (isInRange(3.0, ratio, 3.5)) quality = 58;
        else if (isInRange(3.5, ratio, 4.0)) quality = 50;
        else if (isInRange(4.0, ratio, 4.5)) quality = 42;
        else if (isInRange(4.5, ratio, 5.0)) quality = 34;
        else if (isInRange(5.0, ratio, 5.5)) quality = 26;
        else if (isInRange(5.5, ratio, 6.0)) quality = 20;
        else if (isInRange(6.0, ratio, 6.5)) quality = 15;
        else if (isInRange(6.5, ratio, 7.0)) quality = 10;
        else quality = 1;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] byteArray = baos.toByteArray();
        baos.close();

        return byteArray;
    }

    private boolean isInRange(double min, double number, double max){
        return min <= number && number <= max;
    }

    private boolean isIdentifierSms(String identifier){
        try {
            UUID.fromString(identifier);
        }catch (Exception ex){
            try {
                Long.parseLong(identifier);
                return true;
            }catch (Exception exc){ }
        }
        return false;
    }
}