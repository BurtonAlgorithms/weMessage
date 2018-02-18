package scott.wemessage.app.sms.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.klinker.android.send_message.SentReceiver;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.ReturnType;

public class SmsSent extends SentReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        try {
            Uri messageUri = Uri.parse(intent.getStringExtra("message_uri"));
            MmsMessage message = weMessage.get().getMmsDatabase().getMessageFromUri(messageUri);

            if (message == null) return;
            weMessage.get().getMmsManager().updateOrAddMessage(intent.getStringExtra("task_identifier"), message);
        }catch (Exception ex){
            AppLogger.error("An error occurred while sending an SMS message", ex);
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
        }
    }
}