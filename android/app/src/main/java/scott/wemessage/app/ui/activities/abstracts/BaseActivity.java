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

package scott.wemessage.app.ui.activities.abstracts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.mini.SetDefaultSmsActivity;
import scott.wemessage.app.weMessage;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        if (weMessage.get().isSmsModeEnabled.get() && !MmsManager.isDefaultSmsApp()){
            weMessage.get().disableSmsMode();
        }

        if (!weMessage.get().isSmsModeEnabled.get() && MmsManager.isDefaultSmsApp()){
            weMessage.get().enableSmsMode(true);
        }

        if (MmsManager.isDefaultSmsApp() && (!MmsManager.hasSmsPermissions() || SetDefaultSmsActivity.settingsNoAccess()) && getClass() != SetDefaultSmsActivity.class){
            Intent launcherIntent = new Intent(weMessage.get(), SetDefaultSmsActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_SET_SMS_PERMISSION_ERROR, true);

            startActivity(launcherIntent);
            finish();
        }
    }
}