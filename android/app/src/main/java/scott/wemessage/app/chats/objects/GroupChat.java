package scott.wemessage.app.chats.objects;

import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.FileLocationContainer;

public class GroupChat extends Chat {

    private String displayName;
    private List<Contact> participants;

    public GroupChat(){

    }

    public GroupChat(UUID uuid, FileLocationContainer groupChatPictureFileLocation, String macGuid, String macGroupID, String macChatIdentifier,
                     boolean isInChat, boolean hasUnreadMessages, String displayName, List<Contact> participants) {

        super(uuid, groupChatPictureFileLocation, macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.displayName = displayName;
        this.participants = participants;
    }

    @Override
    public ChatType getChatType() {
        return ChatType.GROUP;
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

    public GroupChat addParticipant(Contact contact){
        participants.add(contact);
        return this;
    }

    public GroupChat removeParticipant(Contact contact){
        participants.remove(contact);
        return this;
    }
}