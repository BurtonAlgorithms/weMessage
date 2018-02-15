package scott.wemessage.app.models.chats;

import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;

public class PeerChat extends Chat {

    private Handle handle;

    public PeerChat(){

    }

    public PeerChat(String identifier, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages, Handle handle) {
        super(identifier, null, macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.handle = handle;

        if (handle != null && weMessage.get().getMessageDatabase().getContactByHandle(handle) != null){
            this.chatPictureFileLocation = weMessage.get().getMessageDatabase().getContactByHandle(handle).getContactPictureFileLocation();
        }
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PEER;
    }

    @Override
    public FileLocationContainer getChatPictureFileLocation() {
        if (getHandle() != null && weMessage.get().getMessageDatabase().getContactByHandle(getHandle()) != null){
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