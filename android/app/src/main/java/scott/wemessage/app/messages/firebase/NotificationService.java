package scott.wemessage.app.messages.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.chats.GroupChat;
import scott.wemessage.app.security.CryptoType;
import scott.wemessage.app.security.DecryptionTask;
import scott.wemessage.app.security.KeyTextPair;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.message.JSONNotification;
import scott.wemessage.commons.utils.StringUtils;

public class NotificationService extends FirebaseMessagingService {

    private final int ERRORED_NOTIFICATION_TAG = 1000;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if(!powerManager.isInteractive()) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WeMessageNotificationWakeLock");
            wakeLock.acquire(5 * 1000);
        }

        initChannel();
        showNotification(remoteMessage);
    }

    private void initChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(weMessage.NOTIFICATION_CHANNEL_NAME) == null) {
            NotificationChannel channel = new NotificationChannel(weMessage.NOTIFICATION_CHANNEL_NAME, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build();

            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{1000, 1000});
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(RemoteMessage remoteMessage){
        if (weMessage.get().isSignedIn()) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            MessageDatabase database = weMessage.get().getMessageDatabase();

            try {
                int notificationVersion = Integer.parseInt(remoteMessage.getData().get("notificationVersion"));

                if (notificationVersion == 1) {

                    if (weMessage.get().performNotification(remoteMessage.getData().get("chatId"))) {
                        JSONNotification jsonNotification = new JSONNotification();

                        jsonNotification.setEncryptedText(remoteMessage.getData().get("encryptedText"));
                        jsonNotification.setKey(remoteMessage.getData().get("key"));
                        jsonNotification.setHandleId(remoteMessage.getData().get("handleId"));
                        jsonNotification.setChatId(remoteMessage.getData().get("chatId"));
                        jsonNotification.setChatName(remoteMessage.getData().get("chatName"));
                        jsonNotification.setAttachmentNumber(remoteMessage.getData().get("attachmentNumber"));

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
                                displayName = ((GroupChat) chat).getUIDisplayName(false);
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
                                largeIcon = createCircleBitmap(BitmapFactory.decodeFile(chat.getChatPictureFileLocation().getFileLocation()));
                            } else {
                                largeIcon = createCircleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_group_chat));
                            }
                        } else {
                            if (handle != null) {
                                Contact c = database.getContactByHandle(handle);
                                if (c != null && c.getContactPictureFileLocation() != null && !StringUtils.isEmpty(c.getContactPictureFileLocation().getFileLocation())) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(c.getContactPictureFileLocation().getFileLocation());

                                    if (bitmap != null) {
                                        largeIcon = createCircleBitmap(bitmap);
                                    }else {
                                        largeIcon = createCircleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_contact));
                                    }
                                } else {
                                    largeIcon = createCircleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_contact));
                                }
                            }
                        }
                        String decryptedText = decryptionTask.getDecryptedText();

                        if (StringUtils.isEmpty(StringUtils.trimORC(decryptedText))) {
                            if (Integer.valueOf(jsonNotification.getAttachmentNumber()) > 0) {
                                message += getString(R.string.notification_attachments, jsonNotification.getAttachmentNumber());
                            }
                        } else {
                            message += decryptedText;
                        }

                        Intent intent = new Intent(this, LaunchActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        if (chat != null) {
                            intent.putExtra(weMessage.BUNDLE_LAUNCHER_GO_TO_CONVERSATION_UUID, chat.getUuid().toString());
                        }
                        intent.setAction(Long.toString(System.currentTimeMillis()));

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        NotificationCompat.Builder builder;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            builder = new NotificationCompat.Builder(this, weMessage.NOTIFICATION_CHANNEL_NAME);
                        } else {
                            builder = new NotificationCompat.Builder(this);
                            builder.setVibrate(new long[]{1000, 1000})
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        }

                        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                        String tag = weMessage.NOTIFICATION_TAG;

                        if (chat != null) {
                            tag += chat.getUuid().toString();
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
                    }
                }else if (notificationVersion == 2){

                    performErroredNotification(notificationManager, remoteMessage);

                }else {
                    performErroredNotification(notificationManager, remoteMessage);
                }
            } catch (Exception ex){
                performErroredNotification(notificationManager, remoteMessage);
                AppLogger.error("An error occurred while trying to show a notification!", ex);
            }
        }
    }

    private void performErroredNotification(NotificationManager notificationManager, RemoteMessage remoteMessage){
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, weMessage.NOTIFICATION_CHANNEL_NAME);
        } else {
            builder = new NotificationCompat.Builder(this);
            builder.setVibrate(new long[]{1000, 1000})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        Notification notification = builder
                .setContentTitle(getString(R.string.notification_error))
                .setContentText(getString(R.string.notification_error_body))
                .setSmallIcon(R.drawable.ic_app_notification_white_small)
                .setWhen(remoteMessage.getSentTime())
                .setAutoCancel(true)
                .build();

        notificationManager.cancel(ERRORED_NOTIFICATION_TAG);
        notificationManager.notify(ERRORED_NOTIFICATION_TAG, notification);
    }


    public Bitmap createCircleBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);
        float r;
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}