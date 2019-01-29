/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.messages.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import me.leolin.shortcutbadger.ShortcutBadger;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.security.CryptoType;
import scott.wemessage.app.security.DecryptionTask;
import scott.wemessage.app.security.KeyTextPair;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.message.JSONNotification;
import scott.wemessage.commons.utils.StringUtils;

public final class NotificationManager {

    private final int ERRORED_NOTIFICATION_TAG = 1000;
    
    private weMessage app;
    private NotificationCallbacks notificationCallbacks;
    private ConcurrentHashMap<String, Integer> unreadMessages = new ConcurrentHashMap<>();
    
    public NotificationManager(weMessage app){
        this.app = app;
        
        initialize();
    }

    public synchronized void setNotificationCallbacks(NotificationCallbacks notificationCallbacks){
        this.notificationCallbacks = notificationCallbacks;
    }

    public void addUnreadMessages(String chatId, int amount){
        int unread = 0;

        if (unreadMessages.containsKey(chatId)){
            unread = unreadMessages.get(chatId);
        }

        unreadMessages.put(chatId, unread + amount);

        try {
            ShortcutBadger.applyCount(app, getTotalUnread());
        }catch (Exception ex){
            AppLogger.error("An error occurred while trying to create a badge", ex);
        }
    }

    public void subtractUnreadMessages(String chatId){
        unreadMessages.remove(chatId);

        try {
            ShortcutBadger.applyCount(app, getTotalUnread());
        }catch (Exception ex){
            AppLogger.error("An error occurred while trying to create a badge", ex);
        }
    }

    public void refreshNotificationCount(final boolean firebaseOnly){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Chat c : app.getMessageManager().getChats()) {
                    if (firebaseOnly) {
                        if (!(c instanceof SmsChat)){
                            unreadMessages.put(c.getIdentifier(), app.getMessageDatabase().getUnreadMessagesCount(c));
                        }
                    }else {
                        unreadMessages.put(c.getIdentifier(), app.getMessageDatabase().getUnreadMessagesCount(c));
                    }
                }
            }
        }).start();
    }

    public void cancelAll(){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public synchronized void clearNotifications(String uuid){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            HashMap<Integer, String> toCancel = new HashMap<>();

            for (StatusBarNotification notification : notificationManager.getActiveNotifications()){
                if (StringUtils.isEmpty(notification.getTag())) continue;
                if (notification.getTag().equals(weMessage.NOTIFICATION_TAG + uuid) || notification.getTag().equals(weMessage.NOTIFICATION_TAG)){
                    toCancel.put(notification.getId(), notification.getTag());
                }
            }

            for (Integer i : toCancel.keySet()){
                notificationManager.cancel(toCancel.get(i), i);
            }
        }else {
            notificationManager.cancelAll();
        }
    }

    public void showFirebaseNotification(Context context, RemoteMessage remoteMessage){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        MessageDatabase database = app.getMessageDatabase();

        // Always show the notification when the device is sleeping
        PowerManager powerManager = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        boolean wake = !powerManager.isInteractive();

        try {
            int notificationVersion = Integer.parseInt(remoteMessage.getData().get("notificationVersion"));

            if (notificationVersion == weMessage.FIREBASE_NOTIFICATION_VERSION) {
                if (wake || performNotification(remoteMessage.getData().get("chatId"))) {
                    JSONNotification jsonNotification = new JSONNotification();

                    jsonNotification.setEncryptedText(remoteMessage.getData().get("encryptedText"));
                    jsonNotification.setKey(remoteMessage.getData().get("key"));
                    jsonNotification.setHandleId(remoteMessage.getData().get("handleId"));
                    jsonNotification.setChatId(remoteMessage.getData().get("chatId"));
                    jsonNotification.setChatName(remoteMessage.getData().get("chatName"));
                    jsonNotification.setAttachmentNumber(remoteMessage.getData().get("attachmentNumber"));
                    jsonNotification.setAccountLogin(remoteMessage.getData().get("accountLogin"));

                    if (!(app.getCurrentSession().getAccount() != null &&
                            Handle.parseHandleId(app.getCurrentSession().getAccount().getEmail()).equals(Handle.parseHandleId(jsonNotification.getAccountLogin())))) return;

                    Chat chat = database.getChatByMacGuid(jsonNotification.getChatId());
                    Handle handle = database.getHandleByHandleID(jsonNotification.getHandleId());

                    if (chat != null && chat instanceof GroupChat) {
                        if (((GroupChat) chat).isDoNotDisturb()) return;
                    }

                    if (handle != null) {
                        if (handle.isDoNotDisturb() || handle.isBlocked()) return;
                    }

                    DecryptionTask decryptionTask = new DecryptionTask(new KeyTextPair(jsonNotification.getEncryptedText(), jsonNotification.getKey()), CryptoType.AES);
                    decryptionTask.runDecryptTask();

                    String displayName = null;
                    String message = "";
                    Bitmap largeIcon = null;

                    if (!StringUtils.isEmpty(jsonNotification.getChatId())) {
                        if (chat != null && chat instanceof GroupChat) {
                            displayName = ((GroupChat) chat).getUIDisplayName();
                        }
                    } else if (!StringUtils.isEmpty(jsonNotification.getChatName())) {
                        displayName = jsonNotification.getChatName();
                    }

                    if (!StringUtils.isEmpty(displayName)) {
                        if (handle != null && database.getContactByHandle(handle) != null) {
                            message = database.getContactByHandle(handle).getDisplayName() + ": ";
                        } else {
                            message = jsonNotification.getHandleId() + ": ";
                        }
                    } else {
                        if (handle != null && database.getContactByHandle(handle) != null) {
                            displayName = database.getContactByHandle(handle).getDisplayName();
                        } else {
                            displayName = jsonNotification.getHandleId();
                        }
                    }

                    if (chat != null && chat instanceof GroupChat) {
                        if (chat.getChatPictureFileLocation() != null && !StringUtils.isEmpty(chat.getChatPictureFileLocation().getFileLocation())) {
                            largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeFile(chat.getChatPictureFileLocation().getFileLocation()));
                        } else {
                            largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default_group_chat));
                        }
                    } else {
                        if (handle != null) {
                            Contact c = database.getContactByHandle(handle);
                            if (c != null && c.getContactPictureFileLocation() != null && !StringUtils.isEmpty(c.getContactPictureFileLocation().getFileLocation())) {
                                Bitmap bitmap = BitmapFactory.decodeFile(c.getContactPictureFileLocation().getFileLocation());

                                if (bitmap != null) {
                                    largeIcon = DisplayUtils.createCircleBitmap(bitmap);
                                }else {
                                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default_contact));
                                }
                            } else {
                                largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default_contact));
                            }
                        }
                    }
                    String decryptedText = decryptionTask.getDecryptedText();

                    if (StringUtils.isEmpty(StringUtils.trimORC(decryptedText))) {
                        if (Integer.valueOf(jsonNotification.getAttachmentNumber()) > 0) {
                            message += context.getString(R.string.notification_attachments, jsonNotification.getAttachmentNumber());
                        }
                    } else {
                        message += decryptedText;
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
                        builder = new NotificationCompat.Builder(context, weMessage.NOTIFICATION_CHANNEL_NAME);
                    } else {
                        builder = new NotificationCompat.Builder(context);
                        builder.setVibrate(new long[]{1000, 1000})
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }

                    int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                    String tag = weMessage.NOTIFICATION_TAG;

                    if (chat != null) {
                        tag += chat.getIdentifier();
                    }

                    Notification notification = builder
                            .setContentTitle(displayName)
                            .setContentText(StringUtils.trimORC(message))
                            .setSmallIcon(R.drawable.ic_app_notification_white_small)
                            .setLargeIcon(largeIcon)
                            .setContentIntent(pendingIntent)
                            .setWhen(remoteMessage.getSentTime())
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify(tag, id, notification);

                    if (!isServiceRunning(ConnectionService.class)) addUnreadMessages(jsonNotification.getChatId(), 1);
                }
            }else {
                performErroredNotification(context, remoteMessage);
            }
        } catch (Exception ex){
            performErroredNotification(context, remoteMessage);
            AppLogger.error("An error occurred while trying to show a notification!", ex);
        }
    }

    public void showMmsNotification(MmsMessage message){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            if (!performNotification(message.getChat().getIdentifier())) return;

            Chat chat = message.getChat();
            Handle handle = message.getSender();

            if (chat instanceof GroupChat && ((GroupChat) chat).isDoNotDisturb()) return;
            if (handle.isDoNotDisturb() || handle.isBlocked()) return;

            String displayName = null;
            String messageText = "";
            Bitmap largeIcon;

            if (chat instanceof GroupChat) {
                displayName = ((GroupChat) chat).getUIDisplayName();
            }

            if (!StringUtils.isEmpty(displayName)) messageText = handle.getDisplayName() + ": ";
            else displayName = handle.getDisplayName();

            if (chat instanceof GroupChat) {
                if (chat.getChatPictureFileLocation() != null && !StringUtils.isEmpty(chat.getChatPictureFileLocation().getFileLocation())) {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeFile(chat.getChatPictureFileLocation().getFileLocation()));
                } else {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_group_chat_sms));
                }
            } else {
                Contact c = app.getMessageDatabase().getContactByHandle(handle);
                if (c != null && c.getContactPictureFileLocation() != null && !StringUtils.isEmpty(c.getContactPictureFileLocation().getFileLocation())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(c.getContactPictureFileLocation().getFileLocation());

                    if (bitmap != null) largeIcon = DisplayUtils.createCircleBitmap(bitmap);
                    else largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_contact_sms));
                } else {
                    largeIcon = DisplayUtils.createCircleBitmap(BitmapFactory.decodeResource(app.getResources(), R.drawable.ic_default_contact_sms));
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

    private synchronized boolean performNotification(String macGuid){
        if (notificationCallbacks == null) return true;

        return notificationCallbacks.onNotification(macGuid);
    }

    private void performErroredNotification(Context context, RemoteMessage remoteMessage){
        NotificationCompat.Builder builder;
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, weMessage.NOTIFICATION_CHANNEL_NAME);
        } else {
            builder = new NotificationCompat.Builder(context);
            builder.setVibrate(new long[]{1000, 1000})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        Notification notification = builder
                .setContentTitle(context.getString(R.string.notification_error))
                .setContentText(context.getString(R.string.notification_error_body))
                .setSmallIcon(R.drawable.ic_app_notification_white_small)
                .setWhen(remoteMessage.getSentTime())
                .setAutoCancel(true)
                .build();

        notificationManager.cancel(ERRORED_NOTIFICATION_TAG);
        notificationManager.notify(ERRORED_NOTIFICATION_TAG, notification);
    }

    private void initialize(){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(weMessage.NOTIFICATION_CHANNEL_NAME) == null) {
            NotificationChannel channel = new NotificationChannel(weMessage.NOTIFICATION_CHANNEL_NAME, app.getString(R.string.notification_channel_name), android.app.NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build();

            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{1000, 1000});
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getTotalUnread(){
        int value = 0;

        for (Integer i : unreadMessages.values()){
            value += i;
        }

        return value;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}