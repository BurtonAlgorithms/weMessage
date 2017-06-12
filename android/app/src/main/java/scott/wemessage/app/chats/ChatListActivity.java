package scott.wemessage.app.chats;

import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.utils.view.SingleFragmentActivity;

public class ChatListActivity extends SingleFragmentActivity {

    public ChatListActivity() {
        super(R.layout.activity_chat_list, R.id.chatListFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new ChatListFragment();
    }
}