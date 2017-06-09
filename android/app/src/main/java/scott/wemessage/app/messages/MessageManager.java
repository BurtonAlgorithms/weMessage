package scott.wemessage.app.messages;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.app.database.DatabaseManager;
import scott.wemessage.app.database.MessageDatabase;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chat.Chat;
import scott.wemessage.app.messages.objects.chat.GroupChat;
import scott.wemessage.app.messages.objects.chat.PeerChat;

public final class MessageManager {

    private static MessageManager instance;
    private Context context;
    private final List<Callbacks> callbacksList = Collections.synchronizedList(new ArrayList<Callbacks>());
    private ConcurrentHashMap<String, Contact> contacts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Message> messages = new ConcurrentHashMap<>();

    public static synchronized MessageManager getInstance(Context context){
        if (instance == null){
            MessageManager messageManager = new MessageManager(context.getApplicationContext());
            messageManager.init();
            instance = messageManager;
        }
        return instance;
    }

    private MessageManager(Context context){
        this.context = context;
    }

    public MessageDatabase getMessageDatabase(){
        return DatabaseManager.getInstance(context).getMessageDatabase();
    }

    public void hookCallbacks(Callbacks callbacks){
        callbacksList.add(callbacks);
    }

    public void unhookCallbacks(Callbacks callbacks){
        callbacksList.remove(callbacks);
    }

    public void addContact(final Contact contact, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addContactTask(contact);
                }
            }).start();
        }else {
            addContactTask(contact);
        }
    }

    public void updateContact(final String uuid, final Contact newData, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateContactTask(uuid, newData);
                }
            }).start();
        }else {
            updateContactTask(uuid, newData);
        }
    }

    public void addChat(final Chat chat, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addChatTask(chat);
                }
            }).start();
        }else {
            addChatTask(chat);
        }
    }

    public void updateChat(final String uuid, final Chat newData, boolean threaded) {
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateChatTask(uuid, newData);
                }
            }).start();
        }else {
            updateChatTask(uuid, newData);
        }
    }

    public void setHasUnreadMessages(final Chat chat, final boolean hasUnreadMessages, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    setHasUnreadMessagesTask(chat, hasUnreadMessages);
                }
            }).start();
        }else {
            setHasUnreadMessagesTask(chat, hasUnreadMessages);
        }
    }

    public void renameGroupChat(final GroupChat chat, final String newName, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    renameGroupChatTask(chat, newName);
                }
            }).start();
        }else {
            renameGroupChatTask(chat, newName);
        }
    }

    public void addParticipantToGroup(final GroupChat chat, final Contact contact, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addParticipantToGroupTask(chat, contact);
                }
            }).start();
        }else {
            addParticipantToGroupTask(chat, contact);
        }
    }

    public void removeParticipantFromGroup(final GroupChat chat, final Contact contact, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    removeParticipantFromGroupTask(chat, contact);
                }
            }).start();
        }else {
            removeParticipantFromGroupTask(chat, contact);
        }
    }

    public void leaveGroup(final GroupChat chat, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    leaveGroupTask(chat);
                }
            }).start();
        }else {
            leaveGroupTask(chat);
        }
    }

    public void deleteChat(final Chat chat, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    deleteChatTask(chat);
                }
            }).start();
        }else {
            deleteChatTask(chat);
        }
    }

    public void refreshChats(boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    refreshChatsTask();
                }
            }).start();
        }else {
            refreshChatsTask();
        }
    }

    public void addMessage(final Message message, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addMessageTask(message);
                }
            }).start();
        }else {
            addMessageTask(message);
        }
    }

    public void updateMessage(final String uuid, final Message newData, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateMessageTask(uuid, newData);
                }
            }).start();
        }else {
            updateMessageTask(uuid, newData);
        }
    }

    public void removeMessage(final Message message, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    removeMessageTask(message);
                }
            }).start();
        }else {
            removeMessageTask(message);
        }
    }

    public void queueMessages(final Chat chat, final int startIndex, final int requestAmount, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    queueMessagesTask(chat, startIndex, requestAmount);
                }
            }).start();
        }else {
            queueMessagesTask(chat, startIndex, requestAmount);
        }
    }

    private void init(){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Contact c : getMessageDatabase().getContacts()){
                    contacts.put(c.getUuid().toString(), c);
                }

                synchronized (callbacksList){
                    Iterator<Callbacks> i = callbacksList.iterator();

                    while (i.hasNext()){
                        Callbacks callbacks = i.next();
                        callbacks.onContactListRefresh(contacts);
                    }
                }
            }
        }).start();

        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Chat c : getMessageDatabase().getChats()){
                    chats.put(c.getUuid().toString(), c);
                }

                synchronized (callbacksList){
                    Iterator<Callbacks> i = callbacksList.iterator();

                    while (i.hasNext()){
                        Callbacks callbacks = i.next();
                        callbacks.onChatListRefresh(chats);
                    }
                }
            }
        }).start();
    }

    private void addContactTask(Contact contact){
        contacts.put(contact.getUuid().toString(), contact);
        getMessageDatabase().addContact(contact);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onContactCreate(contact);
            }
        }
    }

    private void updateContactTask(String uuid, Contact newData){
        Contact oldContact = getMessageDatabase().getContactByUuid(uuid);

        contacts.put(uuid, newData);
        getMessageDatabase().updateContact(uuid, newData);

        for (Chat chat : chats.values()) {
            if (chat instanceof PeerChat) {
                PeerChat peerChat = (PeerChat) chat;

                if (peerChat.getContact().getUuid().equals(oldContact.getUuid())) {
                    peerChat.setContact(newData);
                    chats.put(peerChat.getUuid().toString(), peerChat);
                    getMessageDatabase().updateChat(peerChat.getUuid().toString(), peerChat);
                }
            } else if (chat instanceof GroupChat) {
                GroupChat groupChat = (GroupChat) chat;
                ArrayList<Contact> newContactList = new ArrayList<>();

                for (Contact c : groupChat.getParticipants()) {
                    if (c.getUuid().equals(oldContact.getUuid())) {
                        newContactList.add(newData);
                    } else {
                        newContactList.add(c);
                    }
                }
                groupChat.setParticipants(newContactList);
                chats.put(groupChat.getUuid().toString(), groupChat);
                getMessageDatabase().updateChat(groupChat.getUuid().toString(), groupChat);
            }
        }

        synchronized (callbacksList) {
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()) {
                Callbacks callbacks = i.next();
                callbacks.onContactUpdate(oldContact, newData);
            }
        }
    }

    private void addChatTask(Chat chat){
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().addChat(chat);

        synchronized (callbacksList) {
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()) {
                Callbacks callbacks = i.next();
                callbacks.onChatAdd(chat);
            }
        }
    }

    private void updateChatTask(String uuid, Chat newData){
        Chat oldChat = getMessageDatabase().getChatByUuid(uuid);

        chats.put(uuid, newData);
        getMessageDatabase().updateChat(uuid, newData);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onChatUpdate(oldChat, newData);
            }
        }
    }

    private void setHasUnreadMessagesTask(Chat chat, boolean hasUnreadMessages){
        chat.setHasUnreadMessages(hasUnreadMessages);
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onUnreadMessagesUpdate(chat, hasUnreadMessages);
            }
        }
    }

    private void renameGroupChatTask(GroupChat chat, String newName){
        chat.setDisplayName(newName);
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onChatRename(chat, newName);
            }
        }
    }

    private void addParticipantToGroupTask(GroupChat chat, Contact contact){
        chat.addParticipant(contact);
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onParticipantAdd(chat, contact);
            }
        }
    }

    private void removeParticipantFromGroupTask(GroupChat chat, Contact contact){
        chat.removeParticipant(contact);
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onParticipantRemove(chat, contact);
            }
        }
    }

    private void leaveGroupTask(GroupChat chat){
        chat.setIsInChat(false);
        chats.put(chat.getUuid().toString(), chat);
        getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onLeaveGroup(chat);
            }
        }
    }

    private void deleteChatTask(Chat chat){
        chats.remove(chat.getUuid().toString());
        getMessageDatabase().deleteChatByUuid(chat.getUuid().toString());

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onChatDelete(chat);
            }
        }
    }

    private void refreshChatsTask(){
        for (Chat c : getMessageDatabase().getChats()){
            chats.put(c.getUuid().toString(), c);
        }

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onChatListRefresh(chats);
            }
        }
    }

    private void addMessageTask(Message message){
        messages.put(message.getUuid().toString(), message);
        for (Attachment a : message.getAttachments()){
            if (getMessageDatabase().getAttachmentByMacGuid(a.getMacGuid()) == null){
                getMessageDatabase().addAttachment(a);
            }
        }
        getMessageDatabase().addMessage(message);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageAdd(message);
            }
        }
    }

    private void updateMessageTask(String uuid, Message newData){
        Message oldMessage = getMessageDatabase().getMessageByUuid(uuid);

        messages.put(uuid, newData);
        getMessageDatabase().updateMessage(uuid, newData);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageUpdate(oldMessage, newData);
            }
        }
    }

    private void removeMessageTask(Message message){
        for (Attachment a : message.getAttachments()){
            a.getFileLocation().getFile().delete();
        }
        messages.remove(message.getUuid().toString());
        getMessageDatabase().deleteMessageByUuid(message.getUuid().toString());

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageDelete(message);
            }
        }
    }

    private void queueMessagesTask(Chat chat, int startIndex, int requestAmount){
        List<Message> messageList = getMessageDatabase().getReversedMessages(chat, startIndex, requestAmount);

        for (Message m : messageList){
            messages.put(m.getUuid().toString(), m);
        }

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessagesQueueFinish(messages);
            }
        }
    }

    private Thread createThreadedTask(Runnable runnable){
        return new Thread(runnable);
    }

    public interface Callbacks {

        void onContactCreate(Contact contact);

        void onContactUpdate(Contact oldData, Contact newData);

        void onContactListRefresh(ConcurrentHashMap<String, Contact> contacts);

        void onChatAdd(Chat chat);

        void onChatUpdate(Chat oldData, Chat newData);

        void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages);

        void onChatRename(Chat chat, String displayName);

        void onParticipantAdd(Chat chat, Contact contact);

        void onParticipantRemove(Chat chat, Contact contact);

        void onLeaveGroup(Chat chat);

        void onChatDelete(Chat chat);

        void onChatListRefresh(ConcurrentHashMap<String, Chat> chats);

        void onMessageAdd(Message message);

        void onMessageUpdate(Message oldData, Message newData);

        void onMessageDelete(Message message);

        void onMessagesQueueFinish(ConcurrentHashMap<String, Message> messages);
    }
}