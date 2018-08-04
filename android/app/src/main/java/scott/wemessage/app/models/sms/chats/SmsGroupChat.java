package scott.wemessage.app.models.sms.chats;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.commons.utils.StringUtils;

public class SmsGroupChat extends GroupChat implements SmsChat {

    public SmsGroupChat(String identifier, List<Handle> participants, FileLocationContainer groupChatPictureFileLocation, boolean hasUnreadMessages, boolean isDoNotDisturb){
        super(identifier, groupChatPictureFileLocation, "", "", "", true, hasUnreadMessages, isDoNotDisturb, "", participants);
    }

    @Override
    public String getDisplayName() {
        String fullString;

        ArrayList<String> dummyParticipantList = new ArrayList<>();

        for (Handle h : getParticipants()) {
            dummyParticipantList.add(h.getDisplayName());
        }
        dummyParticipantList.remove(dummyParticipantList.size() - 1);

        fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + getParticipants().get(getParticipants().size() - 1).getDisplayName();

        return fullString;
    }

    @Override
    public String getUIDisplayName() {
        return getDisplayName();
    }

    @Override
    public String getMacGuid() {
        return "";
    }

    @Override
    public String getMacGroupID() {
        return "";
    }

    @Override
    public String getMacChatIdentifier() {
        return "";
    }

    @Override
    public boolean isInChat() {
        return true;
    }

    @Override
    public SmsGroupChat setMacGuid(String macGuid) {
        return this;
    }

    @Override
    public SmsGroupChat setMacGroupID(String macGroupID) {
        return this;
    }

    @Override
    public SmsGroupChat setMacChatIdentifier(String macChatIdentifier) {
        return this;
    }

    @Override
    public SmsGroupChat setIsInChat(boolean isInChat) {
        return this;
    }

    @Override
    public SmsGroupChat setDisplayName(String displayName) {
        return this;
    }

    @Override
    public SmsGroupChat addParticipant(Handle handle) {
        return this;
    }

    @Override
    public SmsGroupChat removeParticipant(Handle handle) {
        return this;
    }
}