package scott.wemessage.app.messages;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Handle;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;

public final class MessageManager {

    private Context context;
    private ConcurrentHashMap<String, Callbacks> callbacksMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Contact> contacts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Message> messages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ActionMessage> actionMessages = new ConcurrentHashMap<>();

    public MessageManager(weMessage app){
        this.context = app;
        init();
    }

    public Context getContext(){
        return context;
    }

    public ConcurrentHashMap<String, Chat> getChats(){
        return chats;
    }

    public void hookCallbacks(String uuid, Callbacks callbacks){
        callbacksMap.put(uuid, callbacks);
    }

    public void unhookCallbacks(String uuid){
        callbacksMap.remove(uuid);
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

    public void renameGroupChat(final GroupChat chat, final String newName, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    renameGroupChatTask(chat, newName, executionTime);
                }
            }).start();
        }else {
            renameGroupChatTask(chat, newName, executionTime);
        }
    }

    public void addParticipantToGroup(final GroupChat chat, final Contact contact, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addParticipantToGroupTask(chat, contact, executionTime);
                }
            }).start();
        }else {
            addParticipantToGroupTask(chat, contact, executionTime);
        }
    }

    public void removeParticipantFromGroup(final GroupChat chat, final Contact contact, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    removeParticipantFromGroupTask(chat, contact, executionTime);
                }
            }).start();
        }else {
            removeParticipantFromGroupTask(chat, contact, executionTime);
        }
    }

    public void leaveGroup(final GroupChat chat, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    leaveGroupTask(chat, executionTime);
                }
            }).start();
        }else {
            leaveGroupTask(chat, executionTime);
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

    public void alertMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType){
        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessageSendFailure(jsonMessage, returnType);
        }
    }

    public void alertActionPerformFailure(JSONAction jsonAction, ReturnType returnType){
        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onActionPerformFailure(jsonAction, returnType);
        }
    }

    private void init(){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Contact c : weMessage.get().getMessageDatabase().getContacts()){
                    contacts.put(c.getUuid().toString(), c);
                }

                for (Callbacks callbacks : callbacksMap.values()){
                    callbacks.onContactListRefresh(new ArrayList<>(contacts.values()));
                }
            }
        }).start();

        refreshChats(true);
    }

    private void addContactTask(Contact contact){
        contacts.put(contact.getUuid().toString(), contact);
        weMessage.get().getMessageDatabase().addContact(contact);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onContactCreate(contact);
        }
    }

    private void updateHandleTask(String uuid, Handle newData, boolean threaded){
        weMessage.get().getMessageDatabase().updateHandle(uuid, newData);

        for (Contact c : contacts.values()){
            if (c.getHandle().getUuid().toString().equals(uuid)){
                c.setHandle(newData);
                updateContact(uuid, c, threaded);
            }
        }
    }

    private void updateContactTask(String uuid, Contact newData){
        Contact oldContact = weMessage.get().getMessageDatabase().getContactByUuid(uuid);

        contacts.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateContact(uuid, newData);

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

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onContactUpdate(oldContact, newData);
        }
    }

    private void addChatTask(Chat chat){
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().addChat(chat);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onChatAdd(chat);
        }
    }

    private void updateChatTask(String uuid, Chat newData){
        Chat oldChat = weMessage.get().getMessageDatabase().getChatByUuid(uuid);

        chats.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateChat(uuid, newData);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onChatUpdate(oldChat, newData);
        }
    }

    private void setHasUnreadMessagesTask(Chat chat, boolean hasUnreadMessages){
        chat.setHasUnreadMessages(hasUnreadMessages);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onUnreadMessagesUpdate(chat, hasUnreadMessages);
        }
    }

    private void renameGroupChatTask(GroupChat chat, String newName, Date executionTime){
        chat.setDisplayName(newName);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_rename_group, newName), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onChatRename(chat, newName);
        }
    }

    private void addParticipantToGroupTask(GroupChat chat, Contact contact, Date executionTime){
        chat.addParticipant(contact);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_add_participant, contact.getUIDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantAdd(chat, contact);
        }
    }

    private void removeParticipantFromGroupTask(GroupChat chat, Contact contact, Date executionTime){
        chat.removeParticipant(contact);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_remove_participant, contact.getUIDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantRemove(chat, contact);
        }
    }

    private void leaveGroupTask(GroupChat chat, Date executionTime){
        chat.setIsInChat(false);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_leave_group), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onLeaveGroup(chat);
        }
    }

    private void deleteChatTask(Chat chat){
        chats.remove(chat.getUuid().toString());
        weMessage.get().getMessageDatabase().deleteChatByUuid(chat.getUuid().toString());

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onChatDelete(chat);
        }
    }

    private void refreshChatsTask(){
        chats.clear();

        for (Chat c : weMessage.get().getMessageDatabase().getChats()){
            chats.put(c.getUuid().toString(), c);
        }

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onChatListRefresh(new ArrayList<>(chats.values()));
        }
    }

    private void addMessageTask(Message message){
        messages.put(message.getUuid().toString(), message);
        for (Attachment a : message.getAttachments()){
            if (StringUtils.isEmpty(a.getMacGuid()) || weMessage.get().getMessageDatabase().getAttachmentByMacGuid(a.getMacGuid()) == null){
                weMessage.get().getMessageDatabase().addAttachment(a);
            }
        }
        weMessage.get().getMessageDatabase().addMessage(message);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessageAdd(message);
        }
    }

    private void updateMessageTask(String uuid, Message newData){
        Message oldMessage = weMessage.get().getMessageDatabase().getMessageByUuid(uuid);

        messages.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateMessage(uuid, newData);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessageUpdate(oldMessage, newData);
        }
    }

    private void removeMessageTask(Message message){
        for (Attachment a : message.getAttachments()){
            a.getFileLocation().getFile().delete();
        }
        messages.remove(message.getUuid().toString());
        weMessage.get().getMessageDatabase().deleteMessageByUuid(message.getUuid().toString());

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessageDelete(message);
        }
    }

    private void queueMessagesTask(Chat chat, int startIndex, int requestAmount){
        List<ActionMessage> actionMessageList = weMessage.get().getMessageDatabase().getReversedActionMessagesByTime(chat, startIndex, requestAmount);
        List<Message> messageList = weMessage.get().getMessageDatabase().getReversedMessagesByTime(chat, startIndex, requestAmount);

        for (ActionMessage m : actionMessageList){
            actionMessages.put(m.getUuid().toString(), m);
        }

        for (Message m : messageList){
            messages.put(m.getUuid().toString(), m);
        }

        List<MessageBase> joinedList = new ArrayList<>();
        joinedList.addAll(actionMessageList);
        joinedList.addAll(messageList);

        Collections.sort(joinedList, new Comparator<MessageBase>() {
            @Override
            public int compare(MessageBase o1, MessageBase o2) {
                if (o1.getTimeIdentifier() > o2.getTimeIdentifier()) {
                    return -1;
                }
                if (o1.getTimeIdentifier() < o2.getTimeIdentifier()) {
                    return 1;
                }
                return 0;
            }
        });

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessagesQueueFinish(joinedList);
        }
    }

    private void refreshMessagesTask(){
        messages.clear();
        actionMessages.clear();

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onMessagesRefresh();
        }
    }

    private void addActionMessageTask(ActionMessage actionMessage){
        weMessage.get().getMessageDatabase().addActionMessage(actionMessage);
        actionMessages.put(actionMessage.getUuid().toString(), actionMessage);

        for (Callbacks callbacks : callbacksMap.values()){
            callbacks.onActionMessageAdd(actionMessage);
        }
    }

    public void dumpAll(weMessage app){
        contacts.clear();
        chats.clear();
        messages.clear();
        actionMessages.clear();
        callbacksMap.clear();
    }

    private Thread createThreadedTask(Runnable runnable){
        return new Thread(runnable);
    }

    public interface Callbacks {

        void onContactCreate(Contact contact);

        void onContactUpdate(Contact oldData, Contact newData);

        void onContactListRefresh(List<Contact> contacts);

        void onChatAdd(Chat chat);

        void onChatUpdate(Chat oldData, Chat newData);

        void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages);

        void onChatRename(Chat chat, String displayName);

        void onParticipantAdd(Chat chat, Contact contact);

        void onParticipantRemove(Chat chat, Contact contact);

        void onLeaveGroup(Chat chat);

        void onChatDelete(Chat chat);

        void onChatListRefresh(List<Chat> chats);

        void onMessageAdd(Message message);

        void onMessageUpdate(Message oldData, Message newData);

        void onMessageDelete(Message message);

        void onMessagesQueueFinish(List<MessageBase> messages);

        void onMessagesRefresh();

        void onActionMessageAdd(ActionMessage message);

        void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType);

        void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType);
    }
}