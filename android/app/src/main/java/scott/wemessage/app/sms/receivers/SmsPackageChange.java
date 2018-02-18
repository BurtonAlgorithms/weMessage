package scott.wemessage.app.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.weMessage;

public class SmsPackageChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 24) {
            if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED)) {
                if (MmsManager.isDefaultSmsApp()){
                    weMessage.get().enableSmsMode(true);
                }else {
                    weMessage.get().disableSmsMode();
                }
            }
        }
    }
}