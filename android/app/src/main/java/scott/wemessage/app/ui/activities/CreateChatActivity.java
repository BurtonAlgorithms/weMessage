package scott.wemessage.app.ui.activities;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.CreateChatFragment;
import scott.wemessage.app.utils.view.SingleFragmentActivity;

public class CreateChatActivity extends SingleFragmentActivity {

    public CreateChatActivity() {
        super(R.layout.activity_create_chat, R.id.createChatFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new CreateChatFragment();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.createChatFragmentContainer);
        if (fragment != null && fragment instanceof CreateChatFragment){
            ((CreateChatFragment) fragment).goToChatList();
        }else {
            super.onBackPressed();
        }
    }
}
