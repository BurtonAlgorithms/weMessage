package scott.wemessage.app.messages.objects.chat;

import java.util.UUID;

public abstract class Chat {

    private UUID uuid;
    private ChatType chatType;
    private String macGuid;
    private String macGroupID;
    private String macChatIdentifier;
    private boolean isInChat;

    public Chat(){

    }

    public Chat(UUID uuid, ChatType chatType, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat){
        this.uuid = uuid;
        this.chatType = chatType;
        this.macGuid = macGuid;
        this.macGroupID = macGroupID;
        this.macChatIdentifier = macChatIdentifier;
        this.isInChat = isInChat;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChatType getChatType() {
        return chatType;
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

    public Chat setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Chat setMacGuid(String macGuid) {
        this.macGuid = macGuid;
        return this;
    }

    public Chat setMacGroupID(String macGroupID) {
        this.macGroupID = macGroupID;
        return this;
    }

    public Chat setMacChatIdentifier(String macChatIdentifier) {
        this.macChatIdentifier = macChatIdentifier;
        return this;
    }

    public Chat setIsInChat(boolean isInChat) {
        this.isInChat = isInChat;
        return this;
    }

    public enum ChatType {
        PEER("peer"),
        GROUP("group");

        String typeName;

        ChatType(String typeName){
            this.typeName = typeName;
        }

        public String getTypeName(){
            return typeName;
        }

        public static ChatType stringToHandleType(String s){
            if (s == null) return null;

            switch (s.toLowerCase()){
                case "peer":
                    return ChatType.PEER;
                case "group":
                    return ChatType.GROUP;
                default:
                    return null;
            }
        }
    }
}