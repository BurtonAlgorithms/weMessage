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

package scott.wemessage.commons.connection.json.message;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JSONChat {

    @SerializedName("macGuid")
    private String macGuid;

    @SerializedName("macGroupID")
    private String macGroupID;

    @SerializedName("macChatIdentifier")
    private String macChatIdentifier;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("participants")
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