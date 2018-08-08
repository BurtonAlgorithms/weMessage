/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
