package scott.wemessage.app.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;

public class SmsReceived extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_DELIVER_ACTION)) {
                Bundle bundle = intent.getExtras();

                if (weMessage.get().getCurrentSession().getSmsHandle() == null){
                    LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_NEW_MESSAGE_ERROR));
                    return;
                }

                if (bundle != null) {
                    Object[] smsExtra = (Object[]) bundle.get("pdus");
                    MmsMessage message = weMessage.get().getMmsManager().addSmsMessage(smsExtra);

                    if (message == null) throw new NullPointerException("Could not receive message because SMS built from URI was null");

                    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                    if (!powerManager.isInteractive()) {
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WeMessageNotificationWakeLock");
                        wakeLock.acquire(5 * 1000);
                    }

                    weMessage.get().getMessageManager().setHasUnreadMessages(message.getChat(), true, false);
                    weMessage.get().getNotificationManager().showMmsNotification(message);
                }
            }
        }catch (Exception ex){
            AppLogger.error("An error occurred while receiving an SMS message", ex);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(weMessage.BROADCAST_NEW_MESSAGE_ERROR));
        }
    }
}