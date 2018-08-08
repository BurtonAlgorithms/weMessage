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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.klinker.android.send_message.MmsSentReceiver;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.ReturnType;

public class MmsSent extends MmsSentReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (weMessage.get().getCurrentSession().getSmsHandle() == null){
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
            return;
        }

        try {
            String taskIdentifier = intent.getStringExtra(EXTRA_TASK_IDENTIFIER);
            Uri messageUri = Uri.parse(intent.getStringExtra(EXTRA_CONTENT_URI));
            MmsMessage mmsMessage = weMessage.get().getMmsDatabase().getMessageFromUri(taskIdentifier, messageUri, true);

            if (mmsMessage == null) throw new NullPointerException("Could not send MMS message because MMS built from URI was null");

            weMessage.get().getMmsManager().updateOrAddMessage(taskIdentifier, mmsMessage);
        }catch (Exception ex){
            AppLogger.error("An error occurred while adding an MMS message", ex);
            weMessage.get().getMessageManager().alertMessageSendFailure(null, ReturnType.UNKNOWN_ERROR);
        }
    }
}