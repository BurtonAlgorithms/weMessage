package scott.wemessage.app.messages;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.WeApp;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Handle;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.DateUtils;

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

    public static void dump(Context context){
        if (instance != null) {
            getInstance(context).contacts.clear();
            getInstance(context).chats.clear();
            getInstance(context).messages.clear();
            getInstance(context).callbacksList.clear();
            instance = null;
        }
    }

    private MessageManager(Context context){
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public ConcurrentHashMap<String, Chat> getChats(){
        return chats;
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

    public void updateHandle(final String uuid, final Handle newData, final boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateHandleTask(uuid, newData, threaded);
                }
            }).start();
        }else {
            updateHandleTask(uuid, newData, threaded);
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

    public void refreshMessages(boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    refreshMessagesTask();
                }
            }).start();
        }else {
            refreshMessagesTask();
        }
    }

    public void alertMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType){
        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageSendFailure(jsonMessage, returnType);
            }
        }
    }

    public void alertActionPerformFailure(JSONAction jsonAction, ReturnType returnType){
        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onActionPerformFailure(jsonAction, returnType);
            }
        }
    }

    private void init(){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Contact c : WeApp.get().getMessageDatabase().getContacts()){
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

        refreshChats(true);
    }

    private void addContactTask(Contact contact){
        contacts.put(contact.getUuid().toString(), contact);
        WeApp.get().getMessageDatabase().addContact(contact);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onContactCreate(contact);
            }
        }
    }

    private void updateHandleTask(String uuid, Handle newData, boolean threaded){
        WeApp.get().getMessageDatabase().updateHandle(uuid, newData);

        for (Contact c : contacts.values()){
            if (c.getHandle().getUuid().toString().equals(uuid)){
                c.setHandle(newData);
                updateContact(uuid, c, threaded);
            }
        }
    }

    private void updateContactTask(String uuid, Contact newData){
        Contact oldContact = WeApp.get().getMessageDatabase().getContactByUuid(uuid);

        contacts.put(uuid, newData);
        WeApp.get().getMessageDatabase().updateContact(uuid, newData);

        for (Chat chat : chats.values()) {
            if (chat instanceof PeerChat) {
                PeerChat peerChat = (PeerChat) chat;

                if (peerChat.getContact().getUuid().equals(oldContact.getUuid())) {
                    peerChat.setContact(newData);
                    updateChatTask(peerChat.getUuid().toString(), peerChat);
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
                updateChatTask(groupChat.getUuid().toString(), groupChat);
            }
        }

        for (Message message : messages.values()){
            if (message.getSender().getUuid().toString().equals(uuid)){
                message.setSender(newData);
                updateMessageTask(message.getUuid().toString(), message);
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
        WeApp.get().getMessageDatabase().addChat(chat);

        synchronized (callbacksList) {
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()) {
                Callbacks callbacks = i.next();
                callbacks.onChatAdd(chat);
            }
        }
    }

    private void updateChatTask(String uuid, Chat newData){
        Chat oldChat = WeApp.get().getMessageDatabase().getChatByUuid(uuid);

        chats.put(uuid, newData);
        WeApp.get().getMessageDatabase().updateChat(uuid, newData);

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
        WeApp.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

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
        WeApp.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);
        WeApp.get().getMessageDatabase().addActionMessage(new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_rename_group, newName), DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime())
        ));

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
        WeApp.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);
        WeApp.get().getMessageDatabase().addActionMessage(new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_add_participant, contact.getUIDisplayName()), DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime())
        ));

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
        WeApp.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);
        WeApp.get().getMessageDatabase().addActionMessage(new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_remove_participant, contact.getUIDisplayName()), DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime())
        ));

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
        WeApp.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);
        WeApp.get().getMessageDatabase().addActionMessage(new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_leave_group), DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime())
        ));

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
        WeApp.get().getMessageDatabase().deleteChatByUuid(chat.getUuid().toString());

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onChatDelete(chat);
            }
        }
    }

    private void refreshChatsTask(){
        chats.clear();

        for (Chat c : WeApp.get().getMessageDatabase().getChats()){
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
            if (WeApp.get().getMessageDatabase().getAttachmentByMacGuid(a.getMacGuid()) == null){
                WeApp.get().getMessageDatabase().addAttachment(a);
            }
        }
        WeApp.get().getMessageDatabase().addMessage(message);

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageAdd(message);
            }
        }
    }

    private void updateMessageTask(String uuid, Message newData){
        Message oldMessage = WeApp.get().getMessageDatabase().getMessageByUuid(uuid);

        messages.put(uuid, newData);
        WeApp.get().getMessageDatabase().updateMessage(uuid, newData);

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
        WeApp.get().getMessageDatabase().deleteMessageByUuid(message.getUuid().toString());

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessageDelete(message);
            }
        }
    }

    private void queueMessagesTask(Chat chat, int startIndex, int requestAmount){
        List<Message> messageList = WeApp.get().getMessageDatabase().getReversedMessages(chat, startIndex, requestAmount);

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

    private void refreshMessagesTask(){
        messages.clear();

        synchronized (callbacksList){
            Iterator<Callbacks> i = callbacksList.iterator();

            while (i.hasNext()){
                Callbacks callbacks = i.next();
                callbacks.onMessagesRefresh();
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

        void onMessagesRefresh();

        void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType);

        void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType);
    }
}