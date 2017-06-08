package scott.wemessage.app.messages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chat.Chat;

public class ChatListFragment extends Fragment implements MessageManager.Callbacks{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //TODO: Add callback hook
        //TODO: Listen for broadcast of message update error
        //TODO: Listen for broadcast of new message error; intent filter don't forget

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onContactCreate(Contact contact) {

    }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {

    }

    @Override
    public void onContactListRefresh(ConcurrentHashMap<String, Contact> contacts) {

    }

    @Override
    public void onChatAdd(Chat chat) {

    }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) {

    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {

    }

    @Override
    public void onChatRename(Chat chat, String displayName) {

    }

    @Override
    public void onParticipantAdd(Chat chat, Contact contact) {

    }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) {

    }

    @Override
    public void onLeaveGroup(Chat chat) {

    }

    @Override
    public void onChatDelete(Chat chat) {

    }

    @Override
    public void onChatListRefresh(ConcurrentHashMap<String, Chat> chats) {

    }

    @Override
    public void onMessageAdd(Message message) {

    }

    @Override
    public void onMessageUpdate(Message oldData, Message newData) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMessagesQueueFinish(ConcurrentHashMap<String, Message> messages) {

    }
}