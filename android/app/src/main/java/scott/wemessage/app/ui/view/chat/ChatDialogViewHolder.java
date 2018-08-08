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

package scott.wemessage.app.ui.view.chat;

import android.support.text.emoji.EmojiCompat;
import android.view.View;

import com.daimajia.swipe.SwipeLayout;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import scott.wemessage.R;
import scott.wemessage.app.weMessage;

public class ChatDialogViewHolder extends DialogsListAdapter.DialogViewHolder<ChatDialogView> {

    private boolean isDialogUnreadButtonEnabled = true;
    private boolean isDeleteButtonShowing = false;
    private SwipeLayout swipeLayout;

    public ChatDialogViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(ChatDialogView dialog) {
        super.onBind(dialog);

        final String dialogId = dialog.getId();
        swipeLayout = (SwipeLayout) root;

        if (tvBubble != null) {
            tvBubble.setVisibility(isDialogUnreadButtonEnabled && dialog.getUnreadCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        if (tvLastMessage != null && weMessage.get().isEmojiCompatInitialized()){
            tvLastMessage.setText(EmojiCompat.get().process(dialog.getLastMessage().getText()));
        }

        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.chatDeleteButtonLayout));
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                isDeleteButtonShowing = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {
                isDeleteButtonShowing = false;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        itemView.findViewById(R.id.chatDeleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weMessage.get().getMessageManager().deleteChat(weMessage.get().getMessageDatabase().getChatByIdentifier(dialogId), true);
            }
        });
    }

    public void closeView(){
        if (isDeleteButtonShowing) {
            isDeleteButtonShowing = false;
            swipeLayout.close();
        }
    }
}