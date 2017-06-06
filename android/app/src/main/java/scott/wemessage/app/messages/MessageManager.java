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

    private MessageDatabase getMessageDatabase(){
        return DatabaseManager.getInstance(context).getMessageDatabase();
    }

    public void hookCallbacks(Callbacks callbacks){
        callbacksList.add(callbacks);
    }

    public void unhookCallbacks(Callbacks callbacks){
        callbacksList.remove(callbacks);
    }

    public void addContact(final Contact contact){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void updateContact(final String uuid, final Contact newData){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                Contact oldContact = getMessageDatabase().getContactByUuid(uuid);

                contacts.put(uuid, newData);
                getMessageDatabase().updateContact(uuid, newData);

                for (Chat chat : chats.values()){
                    if (chat instanceof PeerChat){
                        PeerChat peerChat = (PeerChat) chat;

                        if (peerChat.getContact().getUuid().equals(oldContact.getUuid())){
                            peerChat.setContact(newData);
                            chats.put(peerChat.getUuid().toString(), peerChat);
                            getMessageDatabase().updateChat(peerChat.getUuid().toString(), peerChat);
                        }
                    }else if(chat instanceof GroupChat){
                        GroupChat groupChat = (GroupChat) chat;
                        ArrayList<Contact> newContactList = new ArrayList<>();

                        for (Contact c : groupChat.getParticipants()){
                            if (c.getUuid().equals(oldContact.getUuid())){
                                newContactList.add(newData);
                            }else {
                                newContactList.add(c);
                            }
                        }
                        groupChat.setParticipants(newContactList);
                        chats.put(groupChat.getUuid().toString(), groupChat);
                        getMessageDatabase().updateChat(groupChat.getUuid().toString(), groupChat);
                    }
                }

                synchronized (callbacksList){
                    Iterator<Callbacks> i = callbacksList.iterator();

                    while (i.hasNext()){
                        Callbacks callbacks = i.next();
                        callbacks.onContactUpdate(oldContact, newData);
                    }
                }
            }
        }).start();
    }

    public void addChat(final Chat chat){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                chats.put(chat.getUuid().toString(), chat);
                getMessageDatabase().addChat(chat);

                synchronized (callbacksList){
                    Iterator<Callbacks> i = callbacksList.iterator();

                    while (i.hasNext()){
                        Callbacks callbacks = i.next();
                        callbacks.onChatAdd(chat);
                    }
                }
            }
        }).start();
    }

    public void setHasUnreadMessages(final Chat chat, final boolean hasUnreadMessages){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void renameGroupChat(final GroupChat chat, final String newName){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void addParticipantToGroup(final GroupChat chat, final Contact contact){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void removeParticipantFromGroup(final GroupChat chat, final Contact contact){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void leaveGroup(final GroupChat chat){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void deleteChat(final Chat chat){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void refreshChats(){
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

    public void addMessage(final Message message){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void updateMessage(final String uuid, final Message newData){
        createThreadedTask(new Runnable(){
            @Override
            public void run() {
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
        }).start();
    }

    public void removeMessage(final Message message){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    public void queueMessages(final Chat chat, final int startIndex, final int requestAmount){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
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
        }).start();
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

    private Thread createThreadedTask(Runnable runnable){
        return new Thread(runnable);
    }

    public interface Callbacks {

        void onContactCreate(Contact contact);

        void onContactUpdate(Contact oldData, Contact newData);

        void onContactListRefresh(ConcurrentHashMap<String, Contact> contacts);

        void onChatAdd(Chat chat);

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