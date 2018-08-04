package scott.wemessage.app.sms.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.klinker.android.send_message.MmsReceivedReceiver;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;

public class MmsReceived extends MmsReceivedReceiver {

    @Override
    protected void onMessageReceived(Uri messageUri) {
        try {
            PowerManager powerManager = (PowerManager) weMessage.get().getSystemService(Context.POWER_SERVICE);
            MmsMessage message = weMessage.get().getMmsDatabase().getMessageFromUri(messageUri, true);

            if (message == null) throw new NullPointerException("Could not receive MMS message because MMS built from URI was null");

            if (weMessage.get().getCurrentSession().getSmsHandle() == null){
                LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_NEW_MESSAGE_ERROR));
                return;
            }

            if (!powerManager.isInteractive()) {
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WeMessageNotificationWakeLock");
                wakeLock.acquire(5 * 1000);
            }

            weMessage.get().getMessageManager().addMessage(message.setUnread(true), false);
            weMessage.get().getMessageManager().setHasUnreadMessages(message.getChat(), true, false);
            weMessage.get().getNotificationManager().showMmsNotification(message);
        }catch (Exception ex){
            AppLogger.error("An error occurred while receiving an MMS message", ex);
            LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_NEW_MESSAGE_ERROR));
        }
    }

    @Override
    protected void onError(String error) {
        LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_NEW_MESSAGE_ERROR));
    }
}