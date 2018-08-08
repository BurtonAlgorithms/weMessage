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

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import scott.wemessage.R;
import scott.wemessage.app.ui.LaunchFragment;
import scott.wemessage.app.ui.activities.abstracts.SingleFragmentActivity;
import scott.wemessage.app.weMessage;

public class LaunchActivity extends SingleFragmentActivity {

    public LaunchActivity(){
        super(R.layout.activity_launch, R.id.launchFragmentContainer);
    }

    @Override
    public Fragment createFragment() {
        return new LaunchFragment();
    }

    public static void launchActivity(Activity callingActivity, Fragment callingFragment, boolean reconnect){
        if ((callingFragment != null && callingFragment.isAdded()) || (callingActivity != null && !callingActivity.isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, !reconnect);

            callingActivity.startActivity(launcherIntent);
            callingActivity.finish();
        }
    }
}