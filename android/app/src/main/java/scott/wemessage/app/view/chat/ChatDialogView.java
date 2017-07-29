package scott.wemessage.app.view.chat;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.app.WeApp;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.view.messages.ContactView;
import scott.wemessage.app.view.messages.MessageView;

public class ChatDialogView implements IDialog {

    private Chat chat;
    private List<ContactView> users = new ArrayList<>();
    private MessageView lastMessage;

    public ChatDialogView(MessageManager messageManager, Chat chat){
        this.chat = chat;
        this.lastMessage = new MessageView(messageManager, WeApp.get().getMessageDatabase().getLastMessageFromChat(chat));

        if (chat.getChatType() == Chat.ChatType.PEER){
            users.add(new ContactView(messageManager, ((PeerChat) chat).getContact()));
        }else {
            GroupChat groupChat = (GroupChat) chat;

            for (Contact c : groupChat.getParticipants()){
                users.add(new ContactView(messageManager, c));
            }
        }
    }

    @Override
    public String getId() {
        return chat.getUuid().toString();
    }

    @Override
    public String getDialogPhoto() {
        return AndroidIOUtils.getChatIconUri(chat);
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
        return booleanToInteger(WeApp.get().getMessageDatabase().getChatByUuid(getId()).hasUnreadMessages());
    }

    private int booleanToInteger(boolean bool){
        if (bool) return 1;
        else return 0;
    }
}