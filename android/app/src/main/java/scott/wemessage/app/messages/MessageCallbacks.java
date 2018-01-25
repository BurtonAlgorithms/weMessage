package scott.wemessage.app.messages;

import java.util.List;

import scott.wemessage.app.messages.models.ActionMessage;
import scott.wemessage.app.messages.models.Message;
import scott.wemessage.app.messages.models.MessageBase;
import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.users.ContactInfo;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;

public interface MessageCallbacks {

    void onContactCreate(ContactInfo contact);

    void onContactUpdate(ContactInfo oldData, ContactInfo newData);

    void onContactListRefresh(List<? extends ContactInfo> contacts);

    void onChatAdd(Chat chat);

    void onChatUpdate(Chat oldData, Chat newData);

    void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages);

    void onChatRename(Chat chat, String displayName);

    void onParticipantAdd(Chat chat, Handle handle);

    void onParticipantRemove(Chat chat, Handle handle);

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

    void onAttachmentSendFailure(FailReason failReason);

    void onAttachmentReceiveFailure(FailReason failReason);
}