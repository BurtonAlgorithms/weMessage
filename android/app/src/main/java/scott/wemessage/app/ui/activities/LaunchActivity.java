package scott.wemessage.app.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.LaunchFragment;
import scott.wemessage.app.ui.activities.abstracts.SingleFragmentActivity;
import scott.wemessage.app.weMessage;

public class LaunchActivity extends SingleFragmentActivity {

    public LaunchActivity(){
        super(R.layout.activity_launch, R.id.launchFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new LaunchFragment();
    }

    public static void launchActivity(Activity callingActivity, Fragment callingFragment, boolean reconnect){
        if ((callingFragment != null && callingFragment.isAdded()) || (callingActivity != null && !callingActivity.isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, !reconnect);

            callingActivity.startActivity(launcherIntent);
            callingActivity.finish();
        }
    }
}