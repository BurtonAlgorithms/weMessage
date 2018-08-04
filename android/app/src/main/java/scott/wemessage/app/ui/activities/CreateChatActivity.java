package scott.wemessage.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import scott.wemessage.R;
import scott.wemessage.app.ui.CreateChatFragment;
import scott.wemessage.app.ui.activities.abstracts.SingleFragmentActivity;
import scott.wemessage.app.weMessage;

public class CreateChatActivity extends SingleFragmentActivity {

    public CreateChatActivity() {
        super(R.layout.activity_create_chat, R.id.createChatFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new CreateChatFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        String action = getIntent().getAction();

        if (action != null && (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SENDTO))){
            LocalBroadcastManager.getInstance(weMessage.get()).sendBroadcast(new Intent(weMessage.BROADCAST_COMPOSE_SMS_LAUNCH));
        }

        super.onCreate(savedInstance);
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
