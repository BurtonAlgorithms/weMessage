package scott.wemessage.app.sms.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.klinker.android.send_message.DeliveredReceiver;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;

public class SmsDelivered extends DeliveredReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (weMessage.get().getCurrentSession().getSmsHandle() == null){
            LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR));
            return;
        }

        try {
            String taskIdentifier = intent.getStringExtra("task_identifier");
            Uri messageUri = Uri.parse(intent.getStringExtra("message_uri"));
            MmsMessage message = weMessage.get().getMmsDatabase().getMessageFromUri(taskIdentifier, messageUri, true);

            if (message == null) throw new NullPointerException("Could not deliver message because SMS built from URI was null");

            weMessage.get().getMmsManager().updateOrAddMessage(taskIdentifier, message);
        }catch (Exception ex){
            AppLogger.error("An error occurred while updating an SMS message", ex);
            LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR));
        }
    }
}