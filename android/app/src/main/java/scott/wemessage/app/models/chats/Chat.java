/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.models.chats;

import scott.wemessage.app.utils.FileLocationContainer;

public abstract class Chat {

    private String identifier;
    private String macGuid;
    private String macGroupID;
    private String macChatIdentifier;
    private boolean isInChat;
    private boolean hasUnreadMessages;
    FileLocationContainer chatPictureFileLocation;

    public Chat(){

    }

    public Chat(String identifier, FileLocationContainer chatPictureFileLocation, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages){
        this.identifier = identifier;
        this.chatPictureFileLocation = chatPictureFileLocation;
        this.macGuid = macGuid;
        this.macGroupID = macGroupID;
        this.macChatIdentifier = macChatIdentifier;
        this.isInChat = isInChat;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract ChatType getChatType();

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

    public Chat setIdentifier(String identifier) {
        this.identifier = identifier;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Chat && ((Chat) obj).getIdentifier().equals(getIdentifier());
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

        public static ChatType stringToChatType(String s){
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