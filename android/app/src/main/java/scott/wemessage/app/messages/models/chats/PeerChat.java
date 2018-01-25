package scott.wemessage.app.messages.models.chats;

import java.util.UUID;

import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;

public class PeerChat extends Chat {

    private Handle handle;

    public PeerChat(){

    }

    public PeerChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages, Handle handle) {
        super(uuid, null, macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.handle = handle;

        if (weMessage.get().getMessageDatabase().getContactByHandle(handle) != null){
            this.chatPictureFileLocation = weMessage.get().getMessageDatabase().getContactByHandle(handle).getContactPictureFileLocation();
        }
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PEER;
    }

    @Override
    public FileLocationContainer getChatPictureFileLocation() {
        if (weMessage.get().getMessageDatabase().getContactByHandle(getHandle()) != null){
            return weMessage.get().getMessageDatabase().getContactByHandle(getHandle()).getContactPictureFileLocation();
        }

        return null;
    }

    public Handle getHandle() {
        return handle;
    }

    public PeerChat setHandle(Handle handle) {
        this.handle = handle;
        return this;
    }
}