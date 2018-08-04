package scott.wemessage.app.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.ReturnType;

public class MmsError extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
    }
}