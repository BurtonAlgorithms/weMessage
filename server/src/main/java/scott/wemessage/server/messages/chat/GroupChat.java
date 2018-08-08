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

import java.util.List;

import scott.wemessage.server.messages.Handle;

public class GroupChat extends ChatBase {

    private List<Handle> participants;
    private String displayName;

    public GroupChat(){
        this(null, -1L, null, null, null, null);
    }

    public GroupChat(String guid, long rowID, String groupID, String chatIdentifier, String displayName, List<Handle> participants){
        super(guid, rowID, groupID, chatIdentifier);
        this.participants = participants;
        this.displayName = displayName;
    }

    public List<Handle> getParticipants() {
        return participants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public GroupChat setParticipants(List<Handle> participants) {
        this.participants = participants;
        return this;
    }

    public GroupChat setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public GroupChat addParticipant(Handle participant){
        participants.add(participant);
        return this;
    }

    public GroupChat removeParticipant(Handle participant){
        for (Handle h : participants) {
            if (h.getHandleID().equals(participant.getHandleID())) {
                participants.remove(h);
                return this;
            }
        }
        return this;
    }
}