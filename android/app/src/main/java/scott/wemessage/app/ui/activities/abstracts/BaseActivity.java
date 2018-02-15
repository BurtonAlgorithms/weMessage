package scott.wemessage.app.ui.activities.abstracts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.mini.SetDefaultSmsActivity;
import scott.wemessage.app.weMessage;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        if (weMessage.get().isDefaultSmsApplication.get() && !MmsManager.isDefaultSmsApp()){
            weMessage.get().disableSmsMode();
        }

        if (!weMessage.get().isDefaultSmsApplication.get() && MmsManager.isDefaultSmsApp()){
            weMessage.get().enableSmsMode();
        }

        if (MmsManager.isDefaultSmsApp() && (!MmsManager.hasSmsPermissions() || SetDefaultSmsActivity.settingsNoAccess()) && getClass() != SetDefaultSmsActivity.class){
            Intent launcherIntent = new Intent(weMessage.get(), SetDefaultSmsActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_SET_SMS_PERMISSION_ERROR, true);

            startActivity(launcherIntent);
            finish();
        }
    }
}