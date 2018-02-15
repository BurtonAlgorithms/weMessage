package scott.wemessage.app.ui.activities;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.ChatViewFragment;
import scott.wemessage.app.ui.activities.abstracts.SingleFragmentActivity;

public class ChatViewActivity extends SingleFragmentActivity {

    public ChatViewActivity(){
        super(R.layout.activity_chat_view, R.id.chatViewFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new ChatViewFragment();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chatViewFragmentContainer);

        if (fragment != null && fragment instanceof ChatViewFragment){
            ((ChatViewFragment) fragment).returnToConversationScreen();
        }else {
            super.onBackPressed();
        }
    }
}