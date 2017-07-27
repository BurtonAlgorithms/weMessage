package scott.wemessage.app.activities;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.messages.ConversationFragment;
import scott.wemessage.app.utils.view.SingleFragmentActivity;

public class ConversationActivity extends SingleFragmentActivity {

    public ConversationActivity() {
        super(R.layout.activity_conversation, R.id.conversationFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new ConversationFragment();
    }
}
