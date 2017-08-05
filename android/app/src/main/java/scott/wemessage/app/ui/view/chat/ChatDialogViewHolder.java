package scott.wemessage.app.ui.view.chat;

import android.view.View;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

public class ChatDialogViewHolder extends DialogsListAdapter.DialogViewHolder<ChatDialogView> {

    private boolean isDialogUnreadButtonEnabled = true;

    public ChatDialogViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(ChatDialogView dialog) {
        super.onBind(dialog);

        tvBubble.setVisibility(isDialogUnreadButtonEnabled && dialog.getUnreadCount() > 0 ? View.VISIBLE : View.INVISIBLE);
    }
}
