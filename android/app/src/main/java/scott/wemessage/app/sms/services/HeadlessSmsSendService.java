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

package scott.wemessage.app.sms.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.chats.SmsGroupChat;
import scott.wemessage.app.models.sms.chats.SmsPeerChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class HeadlessSmsSendService extends IntentService {

    public HeadlessSmsSendService() {
        super("weMessageHeadlessSmsSendService");

        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Uri intentUri = intent.getData();

        if (action == null || !action.equals(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE) || extras == null) return;

        String message = extras.getString(Intent.EXTRA_TEXT);
        String recipients = getRecipients(intentUri);

        if (StringUtils.isEmpty(recipients) || StringUtils.isEmpty(message)) return;

        String[] addresses = TextUtils.split(recipients, ";");
        Chat chat;

        if (addresses.length == 1){
            chat = new SmsPeerChat(null, new Handle().setHandleID(addresses[0]).setHandleType(Handle.HandleType.SMS), false);
        }else {
            List<Handle> handles = new ArrayList<>();

            for (String s : addresses){
                handles.add(new Handle().setHandleID(s).setHandleType(Handle.HandleType.SMS));
            }

            chat = new SmsGroupChat(null, handles, null, false, false);
        }

        MmsMessage mmsMessage = new MmsMessage(null, chat, weMessage.get().getCurrentSession().getSmsHandle(), new ArrayList<Attachment>(), message,
                Calendar.getInstance().getTime(), null, false, false, true, false, chat instanceof SmsGroupChat);
        weMessage.get().getMmsManager().sendMessage(mmsMessage);
    }

    private String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        int pos = base.indexOf('?');
        return (pos == -1) ? base : base.substring(0, pos);
    }
}