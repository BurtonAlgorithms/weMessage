package scott.wemessage.app.messages.models.chats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class GroupChat extends Chat {

    private String displayName;
    private List<Handle> participants;
    private boolean isDoNotDisturb;

    public GroupChat(){

    }

    public GroupChat(UUID uuid, FileLocationContainer groupChatPictureFileLocation, String macGuid, String macGroupID, String macChatIdentifier,
                     boolean isInChat, boolean hasUnreadMessages, boolean isDoNotDisturb, String displayName, List<Handle> participants) {

        super(uuid, groupChatPictureFileLocation, macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.displayName = displayName;
        this.participants = participants;
        this.isDoNotDisturb = isDoNotDisturb;
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
        List<Contact> contacts = new ArrayList<>();

        for (Handle h : participants){
            contacts.add(weMessage.get().getMessageDatabase().getContactByHandle(h));
        }

        if (!StringUtils.isEmpty(getDisplayName())){
            fullString = getDisplayName();
        } else {
            ArrayList<String> dummyParticipantList = new ArrayList<>();

            if (!macUI) {
                for (Contact c : contacts) {
                    dummyParticipantList.add(c.getDisplayName());
                }
                dummyParticipantList.remove(dummyParticipantList.size() - 1);

                fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + contacts.get(contacts.size() - 1).getDisplayName();
            } else {
                for (Handle h : participants) {
                    dummyParticipantList.add(h.getHandleID());
                }
                dummyParticipantList.remove(dummyParticipantList.size() - 1);

                fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + getParticipants().get(getParticipants().size() - 1).getHandleID();
            }
        }
        return fullString;
    }

    public List<Handle> getParticipants() {
        return participants;
    }

    public boolean isDoNotDisturb() {
        return isDoNotDisturb;
    }

    public GroupChat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public GroupChat setParticipants(List<Handle> participants) {
        this.participants = participants;
        return this;
    }

    public GroupChat addParticipant(Handle handle){
        participants.add(handle);
        return this;
    }

    public GroupChat removeParticipant(Handle handle){
        for (Handle h : participants){
            if (h.getUuid().toString().equals(handle.getUuid().toString())){
                participants.remove(h);
                return this;
            }
        }
        return this;
    }

    public GroupChat setDoNotDisturb(boolean isDoNotDisturb) {
        this.isDoNotDisturb = isDoNotDisturb;
        return this;
    }
}