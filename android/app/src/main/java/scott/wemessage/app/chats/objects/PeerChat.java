package scott.wemessage.app.chats.objects;

import java.util.UUID;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.FileLocationContainer;

public class PeerChat extends Chat {

    private Contact contact;

    public PeerChat(){

    }

    public PeerChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages, Contact contact) {
        super(uuid, ChatType.PEER, contact.getContactPictureFileLocation(), macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.contact = contact;
    }

    @Override
    public FileLocationContainer getChatPictureFileLocation() {
        return contact.getContactPictureFileLocation();
    }

    public Contact getContact() {
        return contact;
    }

    public PeerChat setContact(Contact contact) {
        this.contact = contact;
        return this;
    }
}