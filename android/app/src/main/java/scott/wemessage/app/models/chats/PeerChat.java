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

import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;

public class PeerChat extends Chat {

    private Handle handle;

    public PeerChat(){

    }

    public PeerChat(String identifier, String macGuid, String macGroupID, String macChatIdentifier, boolean isInChat, boolean hasUnreadMessages, Handle handle) {
        super(identifier, null, macGuid, macGroupID, macChatIdentifier, isInChat, hasUnreadMessages);

        this.handle = handle;

        if (handle != null && weMessage.get().getMessageDatabase().getContactByHandle(handle) != null){
            this.chatPictureFileLocation = weMessage.get().getMessageDatabase().getContactByHandle(handle).getContactPictureFileLocation();
        }
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PEER;
    }

    @Override
    public FileLocationContainer getChatPictureFileLocation() {
        if (getHandle() != null && weMessage.get().getMessageDatabase().getContactByHandle(getHandle()) != null){
            return weMessage.get().getMessageDatabase().getContactByHandle(getHandle()).getContactPictureFileLocation();
        }

        return null;
    }

    public Handle getHandle() {
        return handle;
    }

    public PeerChat setHandle(Handle handle) {
        this.handle = handle;
        return this;
    }
}