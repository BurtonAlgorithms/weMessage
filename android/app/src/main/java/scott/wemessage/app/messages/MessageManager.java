package scott.wemessage.app.messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.messages.ActionMessage;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.messages.MessageBase;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.chats.SmsGroupChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.ContactInfo;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.types.ActionType;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;

public final class MessageManager {

    private weMessage app;
    private ConcurrentHashMap<String, MessageCallbacks> callbacksMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Handle> handles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Message> messages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ActionMessage> actionMessages = new ConcurrentHashMap<>();

    public MessageManager(weMessage app){
        this.app = app;
        init();
    }

    public void hookCallbacks(String uuid, MessageCallbacks callbacks){
        callbacksMap.put(uuid, callbacks);
    }

    public void unhookCallbacks(String uuid){
        callbacksMap.remove(uuid);
    }

    public List<Chat> getChats(){
        List<Chat> chats = new ArrayList<>();

        if (MmsManager.isDefaultSmsApp()){
            for (SmsChat chat : app.getMmsManager().getChats().values()){
                chats.add((Chat) chat);
            }
        }
        chats.addAll(this.chats.values());

        return chats;
    }

    public HashMap<String, ContactInfo> getContacts(){
        HashMap<String, ContactInfo> contactInfoHashMap = new HashMap<>();

        for (Handle h : handles.values()){
            contactInfoHashMap.put(h.findRoot().getUuid().toString(), h.findRoot());
        }

        return contactInfoHashMap;
    }

    public synchronized void addContact(final Contact contact, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addContactTask(contact, true);
                }
            }).start();
        }else {
            addContactTask(contact, true);
        }
    }

    public synchronized void updateContact(final String uuid, final Contact newData, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateContactTask(uuid, newData, true);
                }
            }).start();
        }else {
            updateContactTask(uuid, newData, true);
        }
    }

    public synchronized void deleteContact(final String uuid, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    deleteContactTask(uuid, true);
                }
            }).start();
        }else {
            deleteContactTask(uuid, true);
        }
    }

    public synchronized void addContactNoCallback(final Contact contact, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addContactTask(contact, false);
                }
            }).start();
        }else {
            addContactTask(contact, false);
        }
    }

    public synchronized void updateContactNoCallback(final String uuid, final Contact newData, boolean threaded){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateContactTask(uuid, newData, false);
                }
            }).start();
        }else {
            updateContactTask(uuid, newData, false);
        }
    }

    public synchronized void deleteContactNoCallback(final String uuid, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    deleteContactTask(uuid, false);
                }
            }).start();
        }else {
            deleteContactTask(uuid, false);
        }
    }

    public synchronized void addHandle(final Handle handle, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addHandleTask(handle, true);
                }
            }).start();
        }else {
            addHandleTask(handle, true);
        }
    }

    public synchronized void updateHandle(final String uuid, final Handle newData, final boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    updateHandleTask(uuid, newData, true);
                }
            }).start();
        }else {
            updateHandleTask(uuid, newData, true);
        }
    }

    public synchronized void addHandleNoCallback(final Handle handle, boolean threaded){
        if (threaded){
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    addHandleTask(handle, false);
                }
            }).start();
        }else {
            addHandleTask(handle, false);
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

    public synchronized void refreshChats(boolean threaded, final boolean callbackOnly){
        if (threaded) {
            createThreadedTask(new Runnable() {
                @Override
                public void run() {
                    refreshChatsTask(callbackOnly);
                }
            }).start();
        }else {
            refreshChatsTask(callbackOnly);
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

    public void alertMessageSendFailure(String identifier, ReturnType returnType){
        if (!StringUtils.isEmpty(identifier) && MmsManager.isDefaultSmsApp() && (returnType == ReturnType.NOT_SENT || returnType == ReturnType.NUMBER_NOT_IMESSAGE || returnType == ReturnType.INVALID_NUMBER)){
            Message message = messages.get(identifier);

            if (message != null && isPossibleSmsChat(message.getChat())) {
                MmsMessage mmsMessage = new MmsMessage(null, message.getChat(), message.getSender(), message.getAttachments(), message.getText(),
                        message.getModernDateSent(), null, false, false, true, true);

                removeMessageTask(message);
                app.getMmsManager().sendMessage(mmsMessage);
                return;
            }
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageSendFailure(returnType);
        }
    }

    public void alertActionPerformFailure(JSONAction jsonAction, ReturnType returnType){
        if (ActionType.fromCode(jsonAction.getActionType()) == ActionType.CREATE_GROUP && MmsManager.isDefaultSmsApp() && returnType == ReturnType.NUMBER_NOT_IMESSAGE){
            SmsGroupChat groupChat;
            List<Handle> handles = new ArrayList<>();
            String[] participants = jsonAction.getArgs()[1].split(",");

            for (String s : participants){
                handles.add(new Handle().setHandleID(s).setHandleType(Handle.HandleType.SMS));
            }

            groupChat = new SmsGroupChat(null, handles, null, false, false);

            if (isPossibleSmsChat(groupChat)){
                MmsMessage mmsMessage = new MmsMessage(null, groupChat, app.getCurrentSession().getSmsHandle(), new ArrayList<Attachment>(), jsonAction.getArgs()[2],
                        Calendar.getInstance().getTime(), null, false, false, true, true);
                app.getMmsManager().sendMessage(mmsMessage);
                return;
            }
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onActionPerformFailure(jsonAction, returnType);
        }
    }

    public void alertAttachmentSendFailure(Attachment attachment, FailReason failReason){
        attachment.getFileLocation().getFile().delete();
        app.getMessageDatabase().deleteAttachmentByUuid(attachment.getUuid().toString());

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onAttachmentSendFailure(failReason);
        }
    }

    public void alertAttachmentReceiveFailure(FailReason failReason){
        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onAttachmentReceiveFailure(failReason);
        }
    }

    public void refreshContactList(){
        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
        }
    }

    public void initialize(){
        refreshChats(true, false);
    }

    public void dumpMessages(){
        chats.clear();
        messages.clear();
        actionMessages.clear();

        refreshChats(false, true);
    }

    private void addContactTask(Contact contact, boolean useCallbacks){
        app.getMessageDatabase().addContact(contact);

        if (useCallbacks) {
            for (MessageCallbacks callbacks : callbacksMap.values()) {
                callbacks.onContactCreate(contact);
            }
        }
    }

    private void updateContactTask(String uuid, Contact newData, boolean useCallbacks){
        Contact oldContact = app.getMessageDatabase().getContactByUuid(uuid);
        app.getMessageDatabase().updateContact(uuid, newData);

        if (useCallbacks) {
            for (MessageCallbacks callbacks : callbacksMap.values()) {
                callbacks.onContactUpdate(oldContact, newData);
            }
        }
    }

    private void deleteContactTask(String uuid, boolean useCallbacks){
        app.getMessageDatabase().deleteContactByUuid(uuid);

        if (useCallbacks) {
            for (MessageCallbacks callbacks : callbacksMap.values()) {
                callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
            }
        }
    }

    private void addHandleTask(Handle handle, boolean useCallbacks){
        if (app.getMessageDatabase().getHandleByHandleID(handle.getHandleID()) != null) return;

        handles.put(handle.getUuid().toString(), handle);
        app.getMessageDatabase().addHandle(handle);

        if (useCallbacks) {
            for (MessageCallbacks callbacks : callbacksMap.values()) {
                callbacks.onContactCreate(handle);
            }
        }
    }

    private void updateHandleTask(String uuid, Handle newData, boolean useCallbacks){
        Handle oldHandle = app.getMessageDatabase().getHandleByUuid(uuid);

        handles.put(uuid, newData);
        app.getMessageDatabase().updateHandle(uuid, newData);

        for (Chat chat : getChats()) {
            if (chat instanceof PeerChat) {
                PeerChat peerChat = (PeerChat) chat;

                if (peerChat.getHandle().getUuid().equals(oldHandle.getUuid())) {
                    peerChat.setHandle(newData);
                    updateChatTask(peerChat.getIdentifier(), peerChat);
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
                updateChatTask(groupChat.getIdentifier(), groupChat);
            }
        }

        for (Message message : messages.values()){
            if (message.getSender().getUuid().toString().equals(uuid)){
                message.setSender(newData);
                updateMessageTask(message.getIdentifier(), message);
            }
        }

        if (MmsManager.isDefaultSmsApp()){
            for (Message message : app.getMmsManager().getLoadedMessages().values()){
                if (message.getSender().getUuid().toString().equals(uuid)){
                    message.setSender(newData);
                    updateMessageTask(message.getIdentifier(), message);
                }
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
        Handle h = app.getMessageDatabase().getHandleByUuid(uuid);
        Contact c = app.getMessageDatabase().getContactByHandle(h);

        if (app.getMessageDatabase().getAccountByHandle(h) != null) return;

        for (Chat chat : getChats()){
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
            deleteContactTask(c.getUuid().toString(), false);
        }

        handles.remove(uuid);
        app.getMessageDatabase().deleteHandleByUuid(uuid);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
            callbacks.onChatListRefresh(getChats());
        }
    }

    private void addChatTask(Chat chat){
        if (chat instanceof SmsChat){
            app.getMmsManager().addChat((SmsChat) chat);
        }else {
            if (app.getMessageDatabase().getChatByMacGuid(chat.getMacGuid()) != null) return;
            chats.put(chat.getIdentifier(), chat);
            app.getMessageDatabase().addChat(chat);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatAdd(chat);
        }
    }

    private void updateChatTask(String uuid, Chat newData){
        Chat oldChat;

        if (newData instanceof SmsChat){
            oldChat = (Chat) app.getMmsManager().getSmsChat(uuid);
            app.getMmsManager().updateChat(uuid, (SmsChat) newData);
        }else {
            oldChat = app.getMessageDatabase().getChatByIdentifier(uuid);

            chats.put(uuid, newData);
            app.getMessageDatabase().updateChat(uuid, newData);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatUpdate(oldChat, newData);
        }
    }

    private void setHasUnreadMessagesTask(Chat chat, boolean hasUnreadMessages){
        if (chat instanceof SmsChat){
            app.getMmsManager().setHasUnreadMessages((SmsChat) chat, hasUnreadMessages);
        }else {
            chat.setHasUnreadMessages(hasUnreadMessages);
            chats.put(chat.getIdentifier(), chat);
            app.getMessageDatabase().updateChat(chat.getIdentifier(), chat);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onUnreadMessagesUpdate(chat, hasUnreadMessages);
        }
    }

    private void renameGroupChatTask(GroupChat chat, String newName, Date executionTime){
        chat.setDisplayName(newName);
        chats.put(chat.getIdentifier(), chat);
        app.getMessageDatabase().updateChat(chat.getIdentifier(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, app.getString(R.string.action_message_rename_group, newName), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatRename(chat, newName);
        }
    }

    private void addParticipantToGroupTask(GroupChat chat, Handle handle, Date executionTime){
        chat.addParticipant(handle);
        chats.put(chat.getIdentifier(), chat);
        app.getMessageDatabase().updateChat(chat.getIdentifier(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, app.getString(R.string.action_message_add_participant, handle.getDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantAdd(chat, handle);
        }
    }

    private void removeParticipantFromGroupTask(GroupChat chat, Handle handle, Date executionTime){
        chat.removeParticipant(handle);
        chats.put(chat.getIdentifier(), chat);
        app.getMessageDatabase().updateChat(chat.getIdentifier(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, app.getString(R.string.action_message_remove_participant, handle.getDisplayName()), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onParticipantRemove(chat, handle);
        }
    }

    private void leaveGroupTask(GroupChat chat, Date executionTime){
        chat.setIsInChat(false);
        chats.put(chat.getIdentifier(), chat);
        app.getMessageDatabase().updateChat(chat.getIdentifier(), chat);

        ActionMessage actionMessage = new ActionMessage(
                UUID.randomUUID(), chat, app.getString(R.string.action_message_leave_group), DateUtils.convertDateTo2001Time(executionTime));
        addActionMessageTask(actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onLeaveGroup(chat);
        }
    }

    private void deleteChatTask(Chat chat){
        if (chat instanceof SmsChat){
            app.getMmsManager().deleteChat((SmsChat) chat);
        }else {
            if (chat.getIdentifier() != null) {
                chats.remove(chat.getIdentifier());
                app.clearNotifications(chat.getIdentifier());
                app.getMessageDatabase().deleteChatByUuid(chat.getIdentifier());
            }
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatDelete(chat);
        }
    }

    private void refreshChatsTask(boolean callbackOnly){
        if (!callbackOnly) {
            chats.clear();

            for (Chat c : app.getMessageDatabase().getChats()) {
                chats.put(c.getIdentifier(), c);
            }
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onChatListRefresh(getChats());
        }
    }

    private void addMessageTask(Message message){
        if (message instanceof MmsMessage){
            app.getMmsManager().addMessage((MmsMessage) message);
        }else {
            for (Attachment a : message.getAttachments()) {
                if (StringUtils.isEmpty(a.getMacGuid()) || app.getMessageDatabase().getAttachmentByMacGuid(a.getMacGuid()) == null) {
                    app.getMessageDatabase().addAttachment(a);
                }
            }

            messages.put(message.getIdentifier(), message);
            app.getMessageDatabase().addMessage(message);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageAdd(message);
        }
    }

    private void updateMessageTask(String uuid, Message newData){
        Message oldMessage;

        if (newData instanceof MmsMessage){
            oldMessage = app.getMmsManager().getMmsMessage(uuid);
            app.getMmsManager().updateMessage(uuid, (MmsMessage) newData);
        }else {
            oldMessage = app.getMessageDatabase().getMessageByIdentifier(uuid);

            messages.put(uuid, newData);
            app.getMessageDatabase().updateMessage(uuid, newData);
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageUpdate(oldMessage, newData);
        }
    }

    private void removeMessageTask(Message message){
        if (message instanceof MmsMessage){
            if (!isUuid(message.getIdentifier())) app.getMmsManager().removeMessage((MmsMessage) message);
        }else {
            messages.remove(message.getIdentifier());
            app.getMessageDatabase().deleteMessageByUuid(message.getIdentifier());
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessageDelete(message);
        }
    }

    private void queueMessagesTask(Chat chat, long startIndex, long requestAmount){
        List<MessageBase> joinedList = new ArrayList<>();

        if (chat instanceof SmsChat){
            joinedList.addAll(app.getMmsManager().queueMessages((SmsChat) chat, startIndex, requestAmount));
        }else {
            List<ActionMessage> actionMessageList = app.getMessageDatabase().getReversedActionMessagesByTime(chat, startIndex, requestAmount);
            List<Message> messageList = app.getMessageDatabase().getReversedMessagesByTime(chat, startIndex, requestAmount);

            for (ActionMessage m : actionMessageList) {
                actionMessages.put(m.getUuid().toString(), m);
            }

            for (Message m : messageList) {
                messages.put(m.getIdentifier(), m);
            }

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
        }

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onMessagesQueueFinish(joinedList);
        }
    }

    private void addActionMessageTask(ActionMessage actionMessage){
        app.getMessageDatabase().addActionMessage(actionMessage);
        actionMessages.put(actionMessage.getUuid().toString(), actionMessage);

        for (MessageCallbacks callbacks : callbacksMap.values()){
            callbacks.onActionMessageAdd(actionMessage);
        }
    }

    private void init(){
        createThreadedTask(new Runnable() {
            @Override
            public void run() {
                for (Handle h : app.getMessageDatabase().getHandles()){
                    handles.put(h.getUuid().toString(), h);
                }

                for (MessageCallbacks callbacks : callbacksMap.values()){
                    callbacks.onContactListRefresh(new ArrayList<>(getContacts().values()));
                }
            }
        }).start();
    }

    private boolean isUuid(String identifier){
        try {
            UUID.fromString(identifier);
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    private boolean isPossibleSmsChat(Chat chat){
        if (chat instanceof PeerChat){
            return !AuthenticationUtils.isValidEmailFormat(((PeerChat) chat).getHandle().getHandleID());
        }else {
            GroupChat groupChat = (GroupChat) chat;

            for (Handle h : groupChat.getParticipants()){
                if (AuthenticationUtils.isValidEmailFormat(h.getHandleID())) return false;
            }

            return true;
        }
    }

    private Thread createThreadedTask(Runnable runnable){
        return new Thread(runnable);
    }
}