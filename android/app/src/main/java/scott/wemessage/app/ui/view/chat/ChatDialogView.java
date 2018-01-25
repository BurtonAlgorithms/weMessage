package scott.wemessage.app.ui.view.chat;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.chats.GroupChat;
import scott.wemessage.app.messages.models.chats.PeerChat;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.ui.view.messages.UserView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.weMessage;

public class ChatDialogView implements IDialog {

    private Chat chat;
    private List<UserView> users = new ArrayList<>();
    private MessageView lastMessage;

    public ChatDialogView(Chat chat){
        this.chat = chat;
        this.lastMessage = new MessageView(weMessage.get().getMessageDatabase().getLastMessageFromChat(chat));

        if (chat.getChatType() == Chat.ChatType.PEER){
            users.add(new UserView(((PeerChat) chat).getHandle()));
        }else {
            GroupChat groupChat = (GroupChat) chat;

            for (Handle h : groupChat.getParticipants()){
                users.add(new UserView(h));
            }
        }
    }

    @Override
    public String getId() {
        return chat.getUuid().toString();
    }

    @Override
    public String getDialogPhoto() {
        return IOUtils.getChatIconUri(chat, IOUtils.IconSize.NORMAL);
    }

    @Override
    public String getDialogName() {
        if (chat.getChatType() == Chat.ChatType.PEER){
            return users.get(0).getName();
        } else {
            GroupChat groupChat = (GroupChat) chat;
            return groupChat.getUIDisplayName(false);
        }
    }

    @Override
    public List<? extends UserView> getUsers() {
        return users;
    }

    @Override
    public MessageView getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = (MessageView) message;
    }

    @Override
    public int getUnreadCount() {
        return booleanToInteger(weMessage.get().getMessageDatabase().getChatByUuid(getId()).hasUnreadMessages());
    }

    private int booleanToInteger(boolean bool){
        if (bool) return 1;
        else return 0;
    }
}