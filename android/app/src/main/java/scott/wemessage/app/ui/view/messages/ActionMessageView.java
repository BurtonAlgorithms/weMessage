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

package scott.wemessage.app.ui.view.messages;

import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import scott.wemessage.app.models.messages.ActionMessage;

public class ActionMessageView implements MessageContentType {

    private ActionMessage actionMessage;

    public ActionMessageView(ActionMessage actionMessage){
        this.actionMessage = actionMessage;
    }

    @Override
    public String getId() {
        return actionMessage.getUuid().toString();
    }

    @Override
    public String getText() {
        return actionMessage.getActionText();
    }

    @Override
    public IUser getUser() {
        return new IUser() {
            @Override
            public String getId() {
                return actionMessage.getUuid().toString();
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getAvatar() {
                return null;
            }
        };
    }

    @Override
    public Date getCreatedAt() {
        return actionMessage.getModernDate();
    }
}
