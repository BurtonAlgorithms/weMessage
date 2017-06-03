package scott.wemessage.app.messages.chat;

import java.util.UUID;

import scott.wemessage.app.messages.Contact;

public class PeerChat extends Conversation {

    private Contact contact;

    public PeerChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, Contact contact) {
        super(uuid, macGuid, macGroupID, macChatIdentifier, isInChat);

        this.contact = contact;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}