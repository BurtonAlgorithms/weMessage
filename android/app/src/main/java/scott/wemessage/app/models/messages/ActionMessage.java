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

package scott.wemessage.app.models.messages;

import java.util.Date;
import java.util.UUID;

import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.commons.utils.DateUtils;

public class ActionMessage extends MessageBase {

    private UUID uuid;
    private Chat chat;
    private String actionText;
    private Long date;

    public ActionMessage(){

    }

    public ActionMessage(UUID uuid, Chat chat, String actionText, Long date){
        this.uuid = uuid;
        this.chat = chat;
        this.actionText = actionText;
        this.date = date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Chat getChat() {
        return chat;
    }

    public String getActionText() {
        return actionText;
    }

    public Long getDate() {
        return date;
    }

    @Override
    public Long getTimeIdentifier() {
        return date;
    }

    public Date getModernDate(){
        if (date == null || date == -1) return null;

        return DateUtils.getDateUsing2001(date);
    }

    public ActionMessage setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ActionMessage setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    public ActionMessage setActionText(String actionText) {
        this.actionText = actionText;
        return this;
    }

    public ActionMessage setDate(Long date) {
        this.date = date;
        return this;
    }
}