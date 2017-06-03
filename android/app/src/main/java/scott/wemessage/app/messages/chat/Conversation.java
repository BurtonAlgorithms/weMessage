package scott.wemessage.app.messages.chat;

import java.util.UUID;

public abstract class Conversation {

    private UUID uuid;
    private String macGuid;
    private String macGroupID;
    private String macChatIdentifier;
    private boolean isInChat;

    public Conversation(UUID uuid, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.macGroupID = macGroupID;
        this.macChatIdentifier = macChatIdentifier;
        this.isInChat = isInChat;
    }

    public UUID getUuid() {
        return uuid;
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

    public boolean isInChat() {
        return isInChat;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public void setIaInChat(boolean isInChat) {
        isInChat = isInChat;
    }
}