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

        if (weMessage.get().getCurrentSession().getSmsHandle() == null){
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
            return;
        }

        try {
            String taskIdentifier = intent.getStringExtra("task_identifier");
            Uri messageUri = Uri.parse(intent.getStringExtra("message_uri"));
            MmsMessage message = weMessage.get().getMmsDatabase().getMessageFromUri(taskIdentifier, messageUri);

            if (message == null) throw new NullPointerException("Message from constructed URI was null.");

            weMessage.get().getMmsManager().updateOrAddMessage(taskIdentifier, message);
        }catch (Exception ex){
            AppLogger.error("An error occurred while sending an SMS message", ex);
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
        }
    }
}