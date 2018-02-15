package scott.wemessage.app.sms.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.klinker.android.send_message.MmsSentReceiver;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.ReturnType;

public class MmsSent extends MmsSentReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        try {
            Uri uri = Uri.parse(intent.getStringExtra(EXTRA_CONTENT_URI));
            MmsMessage mmsMessage = weMessage.get().getMmsManager().getMmsMessage(weMessage.get().getMmsDatabase().getMessageId(uri), false);

            if (mmsMessage != null) {
                weMessage.get().getMmsManager().updateOrAddMessage(intent.getStringExtra(EXTRA_TASK_IDENTIFIER), mmsMessage);
            }
        }catch (Exception ex){
            AppLogger.error("An error occurred while adding an MMS message", ex);
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
        }
    }
}