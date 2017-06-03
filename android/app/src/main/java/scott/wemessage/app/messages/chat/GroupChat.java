package scott.wemessage.app.messages.chat;

import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.Contact;

public class GroupChat extends Conversation {

    private String displayName;
    private List<Contact> participants;

    public GroupChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, String displayName, List<Contact> participants) {
        super(uuid, macGuid, macGroupID, macChatIdentifier, isInChat);

        this.displayName = displayName;
        this.participants = participants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Contact> getParticipants() {
        return participants;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setParticipants(List<Contact> participants) {
        this.participants = participants;
    }
}