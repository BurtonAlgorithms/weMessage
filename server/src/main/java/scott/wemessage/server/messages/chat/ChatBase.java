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