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

package scott.wemessage.app.models.sms.chats;

import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.users.Handle;

public class SmsPeerChat extends PeerChat implements SmsChat {

    public SmsPeerChat(String identifier, Handle handle, boolean hasUnreadMessages){
        super(identifier, "", "", "", true, hasUnreadMessages, handle);
    }

    @Override
    public String getMacGuid() {
        return "";
    }

    @Override
    public String getMacGroupID() {
        return "";
    }

    @Override
    public String getMacChatIdentifier() {
        return "";
    }

    @Override
    public boolean isInChat() {
        return true;
    }

    @Override
    public SmsPeerChat setMacGuid(String macGuid) {
        return this;
    }

    @Override
    public SmsPeerChat setMacGroupID(String macGroupID) {
        return this;
    }

    @Override
    public SmsPeerChat setMacChatIdentifier(String macChatIdentifier) {
        return this;
    }

    @Override
    public SmsPeerChat setIsInChat(boolean isInChat) {
        return this;
    }
}