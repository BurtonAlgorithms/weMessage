package scott.wemessage.app.messages.text.chat;

import java.util.UUID;

import scott.wemessage.app.messages.text.Contact;

public class PeerChat extends Chat {

    private Contact contact;

    public PeerChat(){

    }

    public PeerChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, Contact contact) {
        super(uuid, ChatType.PEER, macGuid, macGroupID, macChatIdentifier, isInChat);

        this.contact = contact;
    }

    public Contact getContact() {
        return contact;
    }

    public PeerChat setContact(Contact contact) {
        this.contact = contact;
        return this;
    }
}