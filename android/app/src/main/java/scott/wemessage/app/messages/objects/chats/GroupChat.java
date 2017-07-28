package scott.wemessage.app.messages.objects.chats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.commons.utils.StringUtils;

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

    public String getUIDisplayName(boolean macUI){
        String fullString;

        if (!StringUtils.isEmpty(getDisplayName())){
            fullString = getDisplayName();
        } else {
            ArrayList<String> dummyParticipantList = new ArrayList<>();

            if (!macUI) {
                for (Contact c : participants) {
                    dummyParticipantList.add(c.getUIDisplayName());
                }
                dummyParticipantList.remove(dummyParticipantList.size() - 1);

                fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + getParticipants().get(getParticipants().size() - 1).getUIDisplayName();
            } else {
                for (Contact c : participants) {
                    dummyParticipantList.add(c.getHandle().getHandleID());
                }
                dummyParticipantList.remove(dummyParticipantList.size() - 1);

                fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + getParticipants().get(getParticipants().size() - 1).getHandle().getHandleID();
            }
        }
        return fullString;
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