package scott.wemessage.app.chats.objects;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public abstract class Chat {

    private UUID uuid;
    private ChatType chatType;
    private String macGuid;
    private String macGroupID;
    private String macChatIdentifier;
    private boolean isInChat;
    private boolean hasUnreadMessages;
    private FileLocationContainer chatPictureFileLocation;

    public Chat(){

    }

    public Chat(UUID uuid, ChatType chatType, FileLocationContainer chatPictureFileLocation, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages){
        this.uuid = uuid;
        this.chatType = chatType;
        this.chatPictureFileLocation = chatPictureFileLocation;
        this.macGuid = macGuid;
        this.macGroupID = macGroupID;
        this.macChatIdentifier = macChatIdentifier;
        this.isInChat = isInChat;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public FileLocationContainer getChatPictureFileLocation() {
        return chatPictureFileLocation;
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

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public Chat setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Chat setChatPictureFileLocation(FileLocationContainer chatPictureFileLocation) {
        this.chatPictureFileLocation = chatPictureFileLocation;
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

    public Chat setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
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