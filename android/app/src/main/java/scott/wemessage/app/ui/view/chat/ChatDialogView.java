package scott.wemessage.app.ui.view.chat;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.ui.view.messages.ContactView;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.weMessage;

public class ChatDialogView implements IDialog {

    private Chat chat;
    private List<ContactView> users = new ArrayList<>();
    private MessageView lastMessage;

    public ChatDialogView(Chat chat){
        this.chat = chat;
        this.lastMessage = new MessageView(weMessage.get().getMessageDatabase().getLastMessageFromChat(chat));

        if (chat.getChatType() == Chat.ChatType.PEER){
            users.add(new ContactView(((PeerChat) chat).getContact()));
        }else {
            GroupChat groupChat = (GroupChat) chat;

            for (Contact c : groupChat.getParticipants()){
                users.add(new ContactView(c));
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
    public List<? extends ContactView> getUsers() {
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