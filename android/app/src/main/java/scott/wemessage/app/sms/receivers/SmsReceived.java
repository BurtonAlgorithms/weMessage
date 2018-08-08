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