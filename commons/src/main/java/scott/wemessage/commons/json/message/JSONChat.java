package scott.wemessage.commons.json.message;

import java.util.List;

public class JSONChat {

    private String macGuid;
    private String macGroupID;
    private String macChatIdentifier;
    private String displayName;
    private List<String> participants;
    
    public JSONChat(String macGuid, String macGroupID, String macChatIdentifier, String displayName, List<String> participants){
        this.macGuid = macGuid;
        this.macGroupID =  macGroupID;
        this.macChatIdentifier = macChatIdentifier;
        this.displayName = displayName;
        this.participants = participants;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public String getMacGroupID() {
        return macGroupID;
    }

    public String getMacChatIdentifier() {
        return macChatIdentifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setMacGroupID(String macGroupID) {
        this.macGroupID = macGroupID;
    }

    public void setMacChatIdentifier(String macChatIdentifier) {
        this.macChatIdentifier = macChatIdentifier;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}