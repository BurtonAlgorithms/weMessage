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

package scott.wemessage.app.ui.view.messages;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;

import scott.wemessage.R;
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.commons.utils.DateUtils;

public class ActionMessageViewHolder extends MessageHolders.BaseMessageViewHolder<ActionMessageView> {

    private TextView actionMessageTextView;
    private TextView actionMessageTimeTextView;
    private TextView actionMessageDayTextView;

    public ActionMessageViewHolder(View itemView) {
        super(itemView);

        actionMessageTextView = itemView.findViewById(R.id.actionMessageText);
        actionMessageTimeTextView = itemView.findViewById(R.id.actionMessageTime);
        actionMessageDayTextView = itemView.findViewById(R.id.actionMessageDay);
    }

    @Override
    public void onBind(ActionMessageView message) {
        actionMessageTextView.setText(message.getText());

        Date date = message.getCreatedAt();

        if (DateFormatter.isToday(date)){
            actionMessageDayTextView.setText(itemView.getContext().getString(R.string.word_today));
        }else if (DateFormatter.isYesterday(date)){
            actionMessageDayTextView.setText(itemView.getContext().getString(R.string.word_yesterday));
        }else if (DateUtils.isSameWeek(date)){
            actionMessageDayTextView.setText(AndroidUtils.getDayFromDate(itemView.getContext(), date));
        }else {
            if (DateFormatter.isCurrentYear(date)){
                actionMessageDayTextView.setText(DateFormatter.format(date, "MMMM d"));
            }else {
                actionMessageDayTextView.setText(DateFormatter.format(date, "MMMM d, yyyy"));
            }
        }
        actionMessageTimeTextView.setText(" " + DateFormatter.format(date, "h:mm a"));
    }
}