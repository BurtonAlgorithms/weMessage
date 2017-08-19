package scott.wemessage.app.ui.view.messages;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;

import scott.wemessage.R;

public class ActionMessageViewHolder extends MessageHolders.BaseMessageViewHolder<ActionMessageView> {

    private TextView actionMessageTextView;
    private TextView actionMessageTimeTextView;
    private TextView actionMessageDayTextView;

    public ActionMessageViewHolder(View itemView) {
        super(itemView);

        actionMessageTextView = (TextView) itemView.findViewById(R.id.actionMessageText);
        actionMessageTimeTextView = (TextView) itemView.findViewById(R.id.actionMessageTime);
        actionMessageDayTextView = (TextView) itemView.findViewById(R.id.actionMessageDay);
    }

    @Override
    public void onBind(ActionMessageView message) {
        actionMessageTextView.setText(message.getText());

        Date date = message.getCreatedAt();

        if (DateFormatter.isToday(date)){
            actionMessageDayTextView.setText(itemView.getContext().getString(R.string.word_today));
        }else if (DateFormatter.isYesterday(date)){
            actionMessageDayTextView.setText(itemView.getContext().getString(R.string.word_yesterday));
        }else {
            if (DateFormatter.isCurrentYear(date)){
                actionMessageDayTextView.setText(DateFormatter.format(date, "MMMM d"));
            }else {
                actionMessageDayTextView.setText(DateFormatter.format(date, "MMMM d yyyy"));
            }
        }
        actionMessageTimeTextView.setText(" " + DateFormatter.format(date, "h:mm a"));
    }
}