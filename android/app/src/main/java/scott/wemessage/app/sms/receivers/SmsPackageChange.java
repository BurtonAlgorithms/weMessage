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

package scott.wemessage.app.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.weMessage;

public class SmsPackageChange extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 24) {
            if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.ACTION_DEFAULT_SMS_PACKAGE_CHANGED)) {
                if (MmsManager.isDefaultSmsApp()){
                    weMessage.get().enableSmsMode(true);
                }else {
                    weMessage.get().disableSmsMode();
                }
            }
        }
    }
}