package scott.wemessage.app.view.chat;

import android.net.Uri;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.R;
import scott.wemessage.app.chats.objects.Chat;
import scott.wemessage.app.chats.objects.GroupChat;
import scott.wemessage.app.chats.objects.PeerChat;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.view.messages.ContactView;
import scott.wemessage.app.view.messages.MessageView;
import scott.wemessage.app.WeApp;
import scott.wemessage.commons.utils.StringUtils;

public class ChatDialogView implements IDialog {

    private MessageManager messageManager;
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
        if (chat.getChatType() == Chat.ChatType.PEER) {
            return users.get(0).getAvatar();
        } else {
            if (chat.getChatPictureFileLocation() == null){
                return AndroidIOUtils.getUriFromResource(WeApp.get(), R.drawable.ic_group_chat_icon_v2).toString();
            }else {
                return Uri.fromFile(WeApp.get().getMessageDatabase().getChatByUuid(getId()).getChatPictureFileLocation().getFile()).toString();
            }
        }
    }

    @Override
    public String getDialogName() {
        if (chat.getChatType() == Chat.ChatType.PEER){
            return users.get(0).getName();
        } else {
            GroupChat groupChat = (GroupChat) chat;
            String fullString;

            if (!StringUtils.isEmpty(groupChat.getDisplayName())){
                fullString = groupChat.getDisplayName();
            } else {
                ArrayList<String> dummyParticipantList = new ArrayList<>();

                for (ContactView c : users){
                    dummyParticipantList.add(c.getName());
                }
                dummyParticipantList.remove(dummyParticipantList.size() - 1);

                fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + groupChat.getParticipants().get(groupChat.getParticipants().size() - 1).getHandle().getHandleID();
            }
            return fullString;
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