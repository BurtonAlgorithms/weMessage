package scott.wemessage.app.sms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;

import com.android.mms.MmsConfig;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.jobs.SendMessageJob;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public final class MmsManager {

    private weMessage app;

    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private ConcurrentHashMap<String, SmsChat> chats = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MmsMessage> messages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, SmsChat> syncingChats = new ConcurrentHashMap<>();

    public MmsManager(weMessage app){
        this.app = app;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CarrierConfigManager carrierConfigManager = (CarrierConfigManager) app.getSystemService(Context.CARRIER_CONFIG_SERVICE);
                weMessage.MAX_MMS_ATTACHMENT_SIZE = carrierConfigManager.getConfig().getInt(CarrierConfigManager.KEY_MMS_MAX_MESSAGE_SIZE_INT);
            } else {
                MmsConfig.init(app);
                weMessage.MAX_MMS_ATTACHMENT_SIZE = MmsConfig.getMaxMessageSize();
            }
        }catch (Exception ex){
            AppLogger.error("Could not fetch maximum SMS size, defaulting to 10MB", ex);
        }
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
        app.getNotificationManager().clearNotifications(chat.getIdentifier());
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

    public MmsMessage addSmsMessage(final Object[] smsExtra){
        Uri uri = app.getMmsDatabase().addSmsMessage(smsExtra);
        MmsMessage message = app.getMmsDatabase().getMessageFromUri(uri);

        if (message != null){
            message.setUnread(true);
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
}