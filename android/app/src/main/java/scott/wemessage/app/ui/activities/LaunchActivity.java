package scott.wemessage.app.ui.activities;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.LaunchFragment;
import scott.wemessage.app.utils.view.SingleFragmentActivity;

public class LaunchActivity extends SingleFragmentActivity {

    public LaunchActivity(){
        super(R.layout.activity_launch, R.id.launchFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new LaunchFragment();
    }
}
