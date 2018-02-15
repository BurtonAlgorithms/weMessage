package scott.wemessage.app.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import scott.wemessage.R;
import scott.wemessage.app.ui.ConversationFragment;
import scott.wemessage.app.ui.activities.abstracts.SingleFragmentActivity;

public class ConversationActivity extends SingleFragmentActivity {

    public ConversationActivity() {
        super(R.layout.activity_conversation, R.id.conversationFragmentContainer);
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstance);
    }

    @Override
    public Fragment createFragment() {
        return new ConversationFragment();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.conversationFragmentContainer);
        if (fragment != null && fragment instanceof ConversationFragment){
            ((ConversationFragment) fragment).goToChatList(null);
        }else {
            super.onBackPressed();
        }
    }
}