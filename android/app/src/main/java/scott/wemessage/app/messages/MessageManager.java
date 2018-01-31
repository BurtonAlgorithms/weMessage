package scott.wemessage.app.messages;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.messages.models.ActionMessage;
import scott.wemessage.app.messages.models.Attachment;
import scott.wemessage.app.messages.models.Message;
import scott.wemessage.app.messages.models.MessageBase;
import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.chats.GroupChat;
import scott.wemessage.app.messages.models.chats.PeerChat;
import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.ContactInfo;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;

public final class MessageManager {

    private Context context;
    private ConcurrentHashMap<String, MessageCallbacks> callbacksMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Handle> handles = new ConcurrentHashMap<>();
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

    public HashMap<String, ContactInfo> getContacts(){
        HashMap<String, ContactInfo> contactInfoHashMap = new HashMap<>();

        for (Handle h : handles.values()){
            contactInfoHashMap.put(h.findRoot().getUuid().toString(), h.findRoot());
        }

        return contactInfoHashMap;
    }

    public void hookCallbacks(String uuid, MessageCallbacks callbacks){
        callbacksMap.put(uuid, callbacks);
    }

    public void unhookCallbacks(String uuid){
        callbacksMap.remove(uuid);
    }

    public synchronized void addContact(final Contact contact, boolean threaded){
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

    public synchronized void updateContact(final String uuid, final Contact newData, boolean threaded){
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

    public synchronized void deleteContact(final String uuid, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    deleteContactTask(uuid);
                }
            }).start();
        }else {
            deleteContactTask(uuid);
        }
    }

    public synchronized void addHandle(final Handle handle, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addHandleTask(handle);
                }
            }).start();
        }else {
            addHandleTask(handle);
        }
    }

    public synchronized void updateHandle(final String uuid, final Handle newData, final boolean threaded, final boolean useCallbacks){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateHandleTask(uuid, newData, useCallbacks);
                }
            }).start();
        }else {
            updateHandleTask(uuid, newData, useCallbacks);
        }
    }

    public synchronized void deleteHandle(final String uuid, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    deleteHandleTask(uuid);
                }
            }).start();
        }else {
            deleteHandleTask(uuid);
        }
    }

    public synchronized void addChat(final Chat chat, boolean threaded){
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

    public synchronized void updateChat(final String uuid, final Chat newData, boolean threaded) {
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

    public synchronized void setHasUnreadMessages(final Chat chat, final boolean hasUnreadMessages, boolean threaded){
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

    public synchronized void renameGroupChat(final GroupChat chat, final String newName, final Date executionTime, boolean threaded){
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

    public synchronized void addParticipantToGroup(final GroupChat chat, final Handle handle, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addParticipantToGroupTask(chat, handle, executionTime);
                }
            }).start();
        }else {
            addParticipantToGroupTask(chat, handle, executionTime);
        }
    }

    public synchronized void removeParticipantFromGroup(final GroupChat chat, final Handle handle, final Date executionTime, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    removeParticipantFromGroupTask(chat, handle, executionTime);
                }
            }).start();
        }else {
            removeParticipantFromGroupTask(chat, handle, executionTime);
        }
    }

    public synchronized void leaveGroup(final GroupChat chat, final Date executionTime, boolean threaded){
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

    public synchronized void deleteChat(final Chat chat, boolean threaded){
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

    public synchronized void refreshChats(boolean threaded){
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

    public synchronized void addMessage(final Message message, boolean threaded){
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

    public synchronized void updateMessage(final String uuid, final Message newData, boolean threaded){
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

    public synchronized void removeMessage(final Message message, boolean threaded){
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

    public void queueMessages(final Chat chat, final long startIndex, final long requestAmount, boolean threaded){
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
        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageSendFailure(jsonMessage, returnType);
        }
    }

    public void alertActionPerformFailure(JSONAction jsonAction, ReturnType returnType){
        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onActionPerformFailure(jsonAction, returnType);
        }
    }

    public void alertAttachmentSendFailure(Attachment attachment, FailReason failReason){
        attachment.getFileLocation().getFile().delete();
        weMessage.get().getMessageDatabase().deleteAttachmentByUuid(attachment.getUuid().toString());

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onAttachmentSendFailure(failReason);
        }
    }

    public void alertAttachmentReceiveFailure(FailReason failReason){
        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onAttachmentReceiveFailure(failReason);
        }
    }

    private void init(){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Handle h : weMessage.get().getMessageDatabase().getHandles()){
                    handles.put(h.getUuid().toString(), h);
                }

                for (MessageCallbacks callbacks : callbacksMap.values()){
                    callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
                }
            }
        }).start();

        refreshChats(true);
    }

    private void addContactTask(Contact contact){
        weMessage.get().getMessageDatabase().addContact(contact);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactCreate(contact);
        }
    }

    private void updateContactTask(String uuid, Contact newData){
        Contact oldContact = weMessage.get().getMessageDatabase().getContactByUuid(uuid);
        weMessage.get().getMessageDatabase().updateContact(uuid, newData);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactUpdate(oldContact, newData);
        }
    }

    private void deleteContactTask(String uuid){
        weMessage.get().getMessageDatabase().deleteContactByUuid(uuid);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
        }
    }

    private void addHandleTask(Handle handle){
        if (weMessage.get().getMessageDatabase().getHandleByHandleID(handle.getHandleID()) != null) return;

        handles.put(handle.getUuid().toString(), handle);
        weMessage.get().getMessageDatabase().addHandle(handle);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactCreate(handle);
        }
    }

    private void updateHandleTask(String uuid, Handle newData, boolean useCallbacks){
        Handle oldHandle = weMessage.get().getMessageDatabase().getHandleByUuid(uuid);

        handles.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateHandle(uuid, newData);

        for (Chat chat : chats.values()) {
            if (chat instanceof PeerChat) {
                PeerChat peerChat = (PeerChat) chat;

                if (peerChat.getHandle().getUuid().equals(oldHandle.getUuid())) {
                    peerChat.setHandle(newData);
                    updateChatTask(peerChat.getUuid().toString(), peerChat);
                }
            } else if (chat instanceof GroupChat) {
                GroupChat groupChat = (GroupChat) chat;
                ArrayList<Handle> newParticipantList = new ArrayList<>();

                for (Handle h : groupChat.getParticipants()) {
                    if (h.getUuid().equals(oldHandle.getUuid())) {
                        newParticipantList.add(newData);
                    } else {
                        newParticipantList.add(h);
                    }
                }
                groupChat.setParticipants(newParticipantList);
                updateChatTask(groupChat.getUuid().toString(), groupChat);
            }
        }

        for (Message message : messages.values()){
            if (message.getSender().getUuid().toString().equals(uuid)){
                message.setSender(newData);
                updateMessageTask(message.getUuid().toString(), message);
            }
        }

        if (useCallbacks) {
            for (MessageCallbacks callbacks : callbacksMap.values()) {
                callbacks.onContactUpdate(oldHandle, newData);
            }
        }
    }

    private void deleteHandleTask(String uuid){
        List<Chat> chatsToDelete = new ArrayList<>();
        Handle h = weMessage.get().getMessageDatabase().getHandleByUuid(uuid);
        Contact c = weMessage.get().getMessageDatabase().getContactByHandle(h);

        if (weMessage.get().getMessageDatabase().getAccountByHandle(h) != null) return;

        for (Chat chat : chats.values()){
            if (chat instanceof PeerChat){
                if (((PeerChat) chat).getHandle().equals(h)) chatsToDelete.add(chat);
            }else if (chat instanceof GroupChat){
                if (((GroupChat) chat).getParticipants().contains(h)) chatsToDelete.add(chat);
            }
        }

        for (Chat chat : chatsToDelete){
            deleteChatTask(chat);
        }

        if (c != null){
            deleteContactTask(c.getUuid().toString());
        }

        handles.remove(uuid);
        weMessage.get().getMessageDatabase().deleteHandleByUuid(uuid);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
            callbacks.onChatListRefresh(new ArrayList<>(getChats().values()));
        }
    }

    private void addChatTask(Chat chat){
        if (weMessage.get().getMessageDatabase().getChatByMacGuid(chat.getMacGuid()) != null) return;
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().addChat(chat);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatAdd(chat);
        }
    }

    private void updateChatTask(String uuid, Chat newData){
        Chat oldChat = weMessage.get().getMessageDatabase().getChatByUuid(uuid);

        chats.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateChat(uuid, newData);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatUpdate(oldChat, newData);
        }
    }

    private void setHasUnreadMessagesTask(Chat chat, boolean hasUnreadMessages){
        chat.setHasUnreadMessages(hasUnreadMessages);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        for (MessageCallbacks callbacks : callbacksMap.values()){
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

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatRename(chat, newName);
        }
    }

    private void addParticipantToGroupTask(GroupChat chat, Handle handle, Date executionTime){
        chat.addParticipant(handle);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_add_participant, handle.getDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantAdd(chat, handle);
        }
    }

    private void removeParticipantFromGroupTask(GroupChat chat, Handle handle, Date executionTime){
        chat.removeParticipant(handle);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_remove_participant, handle.getDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantRemove(chat, handle);
        }
    }

    private void leaveGroupTask(GroupChat chat, Date executionTime){
        chat.setIsInChat(false);
        chats.put(chat.getUuid().toString(), chat);
        weMessage.get().getMessageDatabase().updateChat(chat.getUuid().toString(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, getContext().getString(R.string.action_message_leave_group), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onLeaveGroup(chat);
        }
    }

    private void deleteChatTask(Chat chat){
        chats.remove(chat.getUuid().toString());
        weMessage.get().clearNotifications(chat.getUuid().toString());
        weMessage.get().getMessageDatabase().deleteChatByUuid(chat.getUuid().toString());

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatDelete(chat);
        }
    }

    private void refreshChatsTask(){
        chats.clear();

        for (Chat c : weMessage.get().getMessageDatabase().getChats()){
            chats.put(c.getUuid().toString(), c);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
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

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageAdd(message);
        }
    }

    private void updateMessageTask(String uuid, Message newData){
        Message oldMessage = weMessage.get().getMessageDatabase().getMessageByUuid(uuid);

        messages.put(uuid, newData);
        weMessage.get().getMessageDatabase().updateMessage(uuid, newData);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageUpdate(oldMessage, newData);
        }
    }

    private void removeMessageTask(Message message){
        messages.remove(message.getUuid().toString());
        weMessage.get().getMessageDatabase().deleteMessageByUuid(message.getUuid().toString());

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageDelete(message);
        }
    }

    private void queueMessagesTask(Chat chat, long startIndex, long requestAmount){
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

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessagesQueueFinish(joinedList);
        }
    }

    private void refreshMessagesTask(){
        messages.clear();
        actionMessages.clear();

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessagesRefresh();
        }
    }

    private void addActionMessageTask(ActionMessage actionMessage){
        weMessage.get().getMessageDatabase().addActionMessage(actionMessage);
        actionMessages.put(actionMessage.getUuid().toString(), actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onActionMessageAdd(actionMessage);
        }
    }

    public void dumpAll(weMessage app){
        handles.clear();
        chats.clear();
        messages.clear();
        actionMessages.clear();
        callbacksMap.clear();
    }

    private Thread createThreadedTask(Runnable runnable){
        return new Thread(runnable);
    }
}