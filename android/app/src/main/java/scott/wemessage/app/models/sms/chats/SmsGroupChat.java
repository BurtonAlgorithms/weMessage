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

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.commons.utils.StringUtils;

public class SmsGroupChat extends GroupChat implements SmsChat {

    public SmsGroupChat(String identifier, List<Handle> participants, FileLocationContainer groupChatPictureFileLocation, boolean hasUnreadMessages, boolean isDoNotDisturb){
        super(identifier, groupChatPictureFileLocation, "", "", "", true, hasUnreadMessages, isDoNotDisturb, "", participants);
    }

    @Override
    public String getDisplayName() {
        String fullString;

        ArrayList<String> dummyParticipantList = new ArrayList<>();

        for (Handle h : getParticipants()) {
            dummyParticipantList.add(h.getDisplayName());
        }
        dummyParticipantList.remove(dummyParticipantList.size() - 1);

        fullString = StringUtils.join(dummyParticipantList, ", ", 2) + " & " + getParticipants().get(getParticipants().size() - 1).getDisplayName();

        return fullString;
    }

    @Override
    public String getUIDisplayName() {
        return getDisplayName();
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
    public SmsGroupChat setMacGuid(String macGuid) {
        return this;
    }

    @Override
    public SmsGroupChat setMacGroupID(String macGroupID) {
        return this;
    }

    @Override
    public SmsGroupChat setMacChatIdentifier(String macChatIdentifier) {
        return this;
    }

    @Override
    public SmsGroupChat setIsInChat(boolean isInChat) {
        return this;
    }

    @Override
    public SmsGroupChat setDisplayName(String displayName) {
        return this;
    }

    @Override
    public SmsGroupChat addParticipant(Handle handle) {
        return this;
    }

    @Override
    public SmsGroupChat removeParticipant(Handle handle) {
        return this;
    }
}