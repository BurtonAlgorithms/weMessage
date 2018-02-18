package scott.wemessage.app.sms;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.jobs.SendMessageJob;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public final class MmsManager {

    private final int ERRORED_NOTIFICATION_TAG = 1000;

    private weMessage app;

    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, SmsChat> chats = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MmsMessage> messages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, SmsChat> syncingChats = new ConcurrentHashMap<>();

    public MmsManager(weMessage app){
        this.app = app;
    }

    public ConcurrentHashMap<String, SmsChat> getChats(){
        return chats;
    }

    public ConcurrentHashMap<String, MmsMessage> getLoadedMessages(){
        return messages;
    }

    public ConcurrentHashMap<String, SmsChat> getSyncingChats(){
        return syncingChats;
    }

    public SmsChat getSmsChat(String threadId){
        SmsChat chat = chats.get(threadId);

        if (chat == null){
            chat = app.getMessageDatabase().getSmsChatByThreadId(threadId);

            if (chat != null){
                chats.put(threadId, chat);
            }
        }

        return chat;
    }

    public MmsMessage getMmsMessage(String messageId){
        MmsMessage message = messages.get(messageId);

        if (message == null){
            message = app.getMessageDatabase().getMmsMessageByIdentifier(messageId);
        }

        return message;
    }

    public synchronized void addChat(SmsChat chat){
        chats.put(((Chat) chat).getIdentifier(), chat);
        app.getMessageDatabase().addSmsChat(chat);
    }

    public synchronized void updateChat(String threadId, SmsChat newData) {
        chats.put(threadId, newData);
        app.getMessageDatabase().updateSmsChatByThreadId(threadId, newData);
    }

    public synchronized void setHasUnreadMessages(SmsChat smsChat, boolean hasUnreadMessages){
        Chat chat = (Chat) smsChat;
        chat.setHasUnreadMessages(hasUnreadMessages);

        chats.put(chat.getIdentifier(), (SmsChat) chat);
        app.getMessageDatabase().updateSmsChatByThreadId(chat.getIdentifier(), (SmsChat) chat);

        if (!hasUnreadMessages) app.getMmsDatabase().markChatAsRead(chat.getIdentifier());
    }

    public synchronized void deleteChat(SmsChat smsChat){
        Chat chat = (Chat) smsChat;

        chats.remove(chat.getIdentifier());
        app.getMessageDatabase().deleteSmsChatByThreadId(chat.getIdentifier());
        app.getMmsDatabase().deleteChat(chat.getIdentifier());
        app.clearNotifications(chat.getIdentifier());
    }

    public synchronized void addMessage(MmsMessage message){
        for (Attachment a : message.getAttachments()) {
            if (app.getMessageDatabase().getAttachmentByUuid(a.getUuid().toString()) == null) {
                app.getMessageDatabase().addAttachment(a);
            }
        }

        messages.put(message.getIdentifier(), message);
        app.getMessageDatabase().addMmsMessage(message);
    }

    public synchronized MmsMessage addSmsMessage(final Object[] smsExtra){
        MmsMessage message = app.getMmsDatabase().getMessageFromUri(app.getMmsDatabase().addSmsMessage(smsExtra));

        if (message != null){
            app.getMessageManager().addMessage(message, false);
        }

        return message;
    }

    public synchronized void updateMessage(String identifier, MmsMessage message){
        messages.put(identifier, message);
        app.getMessageDatabase().updateMmsMessageByIdentifier(identifier, message);
    }

    public synchronized void removeMessage(MmsMessage message){
        messages.remove(message.getIdentifier());
        app.getMmsDatabase().deleteMessage(message);
        app.getMessageDatabase().deleteMmsMessageByIdentifier(message.getIdentifier());
    }

    public List<MmsMessage> queueMessages(SmsChat chat, long startIndex, long requestAmount){
        List<MmsMessage> messageList = app.getMessageDatabase().getReversedMmsMessagesByTime(chat, startIndex, requestAmount);

        for (MmsMessage m : messageList){
            messages.put(m.getIdentifier(), m);
        }

        Collections.sort(messageList, new Comparator<MmsMessage>() {
            @Override
            public int compare(MmsMessage o1, MmsMessage o2) {
                if (o1.getTimeIdentifier() > o2.getTimeIdentifier()) {
                    return -1;
                }
                if (o1.getTimeIdentifier() < o2.getTimeIdentifier()) {
                    return 1;
                }
                return 0;
            }
        });

        return messageList;
    }

    public synchronized void updateOrAddMessage(String taskIdentifier, MmsMessage message){
        if (messages.get(taskIdentifier) != null){
            app.getMessageManager().updateMessage(taskIdentifier, message, false);
        }else {
            if (messages.get(message.getIdentifier()) != null){
                app.getMessageManager().updateMessage(message.getIdentifier(), message, false);
            }else {
                app.getMessageManager().addMessage(message, false);
            }
        }
    }

    public void sendMessage(MmsMessage mmsMessage){
        String taskIdentifier = UUID.randomUUID().toString();

        mmsMessage.setIdentifier(taskIdentifier);
        weMessage.get().getMessageManager().addMessage(mmsMessage, false);

        SendMessageJob.performJob(taskIdentifier, mmsMessage.getIdentifier());
    }

    public static boolean isPhone(){
        return weMessage.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean isDefaultSmsApp(){
        return isPhone() && weMessage.get().getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(weMessage.get()));
    }

    public static String getPhoneNumber(){
        if (!isPhone()) return null;

        String sharedPrefNumber = weMessage.get().getSharedPreferences().getString(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER, "");
        if (!StringUtils.isEmpty(sharedPrefNumber)) return sharedPrefNumber;

        try {
            TelephonyManager telephonyManager = (TelephonyManager) weMessage.get().getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getLine1Number();
        }catch (SecurityException ex){
            return null;
        }
    }

    public static boolean hasSmsPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] permissions = new String[] {
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.RECEIVE_MMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE
            };

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(weMessage.get(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void initialize(){
        if (isInitialized.get()) return;
        isInitialized.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                chats.clear();

                for (SmsChat chat : app.getMessageDatabase().getSmsChats()){
                    chats.put(((Chat) chat).getIdentifier(), chat);
                }

                app.getMessageManager().refreshChats(false, true);
            }
        }).start();
    }

    public void dumpMessages(){
        isInitialized.set(false);

        chats.clear();
        messages.clear();
        app.getMessageManager().refreshChats(false, true);
    }

    public void showMmsNotification(MmsMessage message){
        NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            if (!app.performNotification(message.getChat().getIdentifier())) return;

            Chat chat = message.getChat();
            Handle handle = message.getSender();

            if (chat instanceof GroupChat && ((GroupChat) chat).isDoNotDisturb()) return;
            if (handle.isDoNotDisturb() || handle.isBlocked()) return;

            String displayName = null;
            String messageText = "";
            Bitmap largeIcon;

            if (chat instanceof GroupChat) {
                displayName = ((GroupChat) chat).getUIDisplayName(false);
            }

            if (!StringUtils.isEmpty(displayName)) messageText = handle.getDisplayName() + ": ";
            else displayName = handle.getDisplayName();

            if (chat instanceof GroupChat) {
                if (chat.getChatPictureFileLocation() != null && !StringUtils.isEmpty(chat.getChatPictureFileLocation().getFileLocation())) {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeFile(chat.getChatPictureFileLocation().getFileLocation()));
                } else {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_group_chat));
                }
            } else {
                Contact c = app.getMessageDatabase().getContactByHandle(handle);
                if (c != null && c.getContactPictureFileLocation() != null && !StringUtils.isEmpty(c.getContactPictureFileLocation().getFileLocation())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(c.getContactPictureFileLocation().getFileLocation());

                    if (bitmap != null) largeIcon = DisplayUtils.createCircleBitmap(bitmap);
                    else largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_contact));
                } else {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_contact));
                }
            }

            if (StringUtils.isEmpty(StringUtils.trimORC(message.getText()))) {
                if (message.getAttachments().size() > 0) {
                    messageText += app.getString(R.string.notification_attachments, String.valueOf(message.getAttachments().size()));
                }
            } else {
                messageText += message.getText();
            }

            Intent intent = new Intent(app, LaunchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            if (chat != null) {
                intent.putExtra(weMessage.BUNDLE_LAUNCHER_GO_TO_CONVERSATION_UUID, chat.getIdentifier());
            }
            intent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pendingIntent = PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new NotificationCompat.Builder(app, weMessage.NOTIFICATION_CHANNEL_NAME);
            } else {
                builder = new NotificationCompat.Builder(app);
                builder.setVibrate(new long[]{1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }

            int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            String tag = weMessage.NOTIFICATION_TAG;

            if (chat != null) {
                tag += chat.getIdentifier();
            }

            if (message.getModernDateSent() != null) builder.setWhen(message.getModernDateSent().getTime());
            else if (message.getModernDateDelivered() != null) builder.setWhen(message.getModernDateDelivered().getTime());
            else builder.setWhen(System.currentTimeMillis());

            Notification notification = builder
                    .setContentTitle(displayName)
                    .setContentText(StringUtils.trimORC(messageText))
                    .setSmallIcon(R.drawable.ic_app_notification_white_small)
                    .setLargeIcon(largeIcon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(tag, id, notification);


        } catch (Exception ex) {
            AppLogger.error("An error occurred while trying to show a notification!", ex);

            NotificationCompat.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new NotificationCompat.Builder(app, weMessage.NOTIFICATION_CHANNEL_NAME);
            } else {
                builder = new NotificationCompat.Builder(app);
                builder.setVibrate(new long[]{1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }

            Notification notification = builder
                    .setContentTitle(app.getString(R.string.notification_error))
                    .setContentText(app.getString(R.string.notification_error_body))
                    .setSmallIcon(R.drawable.ic_app_notification_white_small)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .build();

            notificationManager.cancel(ERRORED_NOTIFICATION_TAG);
            notificationManager.notify(ERRORED_NOTIFICATION_TAG, notification);
        }
    }
}