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

package scott.wemessage.server.scripts;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.database.MessagesDatabase;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.chat.GroupChat;

public class ScriptChatMetadata {

    private GroupChat chat;

    public ScriptChatMetadata(GroupChat chat){
        this.chat = chat;
    }

    public long getAlgorithmicRow(MessagesDatabase messagesDatabase){
        try {
            return messagesDatabase.getChatRowPositionByRowId(chat.getRowID());
        }catch (Exception ex){
            ServerLogger.error("An error occurred while fetching chat data from the database", ex);
            return -1L;
        }
    }

    public String getGuid(){
        return chat.getGuid();
    }

    public String getNameCheck(){
        if (StringUtils.isEmpty(chat.getDisplayName())){
            List<String> participants = new ArrayList<>();

            for (Handle h : chat.getParticipants()){
                participants.add(h.getHandleID());
            }
            return StringUtils.join(participants, ",", 1);
        }else {
            return chat.getDisplayName();
        }
    }

    public boolean getNoNameFlag(){
        return StringUtils.isEmpty(chat.getDisplayName());
    }
}