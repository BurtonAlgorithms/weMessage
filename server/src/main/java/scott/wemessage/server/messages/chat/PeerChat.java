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

import scott.wemessage.server.messages.Handle;

public class PeerChat extends ChatBase {

    private Handle peer;

    public PeerChat(){
        this(null, -1L, null, null, null);
    }

    public PeerChat(String guid, long rowID, String groupID, String chatIdentifier, Handle peer){
        super(guid, rowID, groupID, chatIdentifier);
        this.peer = peer;
    }

    public Handle getPeer() {
        return peer;
    }

    public PeerChat setPeer(Handle peer) {
        this.peer = peer;
        return this;
    }
}