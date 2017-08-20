package scott.wemessage.app.ui.activities;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.ContactViewFragment;
import scott.wemessage.app.utils.view.SingleFragmentActivity;

public class ContactViewActivity extends SingleFragmentActivity {

    public ContactViewActivity(){
        super(R.layout.activity_contact_view, R.id.contactViewFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new ContactViewFragment();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contactViewFragmentContainer);

        if (fragment != null && fragment instanceof ContactViewFragment){
            ((ContactViewFragment) fragment).returnToConversationScreen();
        }else {
            super.onBackPressed();
        }
    }
}