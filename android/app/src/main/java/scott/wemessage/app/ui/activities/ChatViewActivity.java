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