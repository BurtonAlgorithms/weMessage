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

package scott.wemessage.server.database;

import java.util.List;

import scott.wemessage.server.messages.Message;

public class DatabaseSnapshot {

    private List<Message> messages;

    public DatabaseSnapshot(List<Message> messages){
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }
    
    public Message getMessage(String guid){
        for (Message message : getMessages()){
            if (message.getGuid().equals(guid)){
                return message;
            }
        }
        return null;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}