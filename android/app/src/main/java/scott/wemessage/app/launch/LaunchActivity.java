package scott.wemessage.app.launch;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.utils.SingleFragmentActivity;

public class LaunchActivity extends SingleFragmentActivity {

    public LaunchActivity(){
        super(R.layout.activity_launch, R.id.launchFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new LaunchFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }
}
