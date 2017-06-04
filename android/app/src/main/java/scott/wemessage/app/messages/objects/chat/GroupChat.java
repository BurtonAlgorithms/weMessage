package scott.wemessage.app.messages.objects.chat;

import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.objects.Contact;

public class GroupChat extends Chat {

    private String displayName;
    private List<Contact> participants;

    public GroupChat(){

    }

    public GroupChat(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, String displayName, List<Contact> participants) {
        super(uuid, ChatType.GROUP, macGuid, macGroupID, macChatIdentifier, isInChat);

        this.displayName = displayName;
        this.participants = participants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Contact> getParticipants() {
        return participants;
    }

    public GroupChat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public GroupChat setParticipants(List<Contact> participants) {
        this.participants = participants;
        return this;
    }
}