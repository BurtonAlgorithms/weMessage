package scott.wemessage.server.messages.chat;

public abstract class ChatBase {

    private long rowID;
    private String guid;
    private String chatIdentifier;
    private String groupID;

    public ChatBase(String guid, long rowID, String groupID, String chatIdentifier){
        this.rowID = rowID;
        this.guid = guid;
        this.groupID = groupID;
        this.chatIdentifier = chatIdentifier;
    }

    public String getGuid() {
        return guid;
    }

    public long getRowID() {
        return rowID;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getChatIdentifier() {
        return chatIdentifier;
    }

    public ChatBase setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public ChatBase setRowID(long rowID) {
        this.rowID = rowID;
        return this;
    }

    public ChatBase setGroupID(String groupID) {
        this.groupID = groupID;
        return this;
    }

    public ChatBase setChatIdentifier(String chatIdentifier) {
        this.chatIdentifier = chatIdentifier;
        return this;
    }
}