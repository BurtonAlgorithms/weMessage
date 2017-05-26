package scott.wemessage.server.messages.chat;

import scott.wemessage.server.messages.Handle;

import java.util.List;

public class GroupChat extends ChatBase {

    private List<Handle> participants;
    private String displayName;

    public GroupChat(){
        this(null, -1, null, null, null, null);
    }

    public GroupChat(String guid, int rowID, String groupID, String chatIdentifier, String displayName, List<Handle> participants){
        super(guid, rowID, groupID, chatIdentifier);
        this.participants = participants;
        this.displayName = displayName;
    }

    public List<Handle> getParticipants() {
        return participants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GroupChat setParticipants(List<Handle> participants) {
        this.participants = participants;
        return this;
    }

    public GroupChat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public GroupChat addParticipant(Handle participant){
        participants.add(participant);
        return this;
    }

    public GroupChat removeParticipant(Handle participant){
        participants.remove(participant);
        return this;
    }
}