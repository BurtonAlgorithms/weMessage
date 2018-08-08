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

import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.IOUtils;

public class UserView implements IUser {

    private Handle handle;

    public UserView(Handle handle){
        this.handle = handle;
    }

    @Override
    public String getId() {
        try {
            return handle.getUuid().toString();
        }catch(Exception ex){
            return "";
        }
    }

    @Override
    public String getName() {
        return handle.getDisplayName();
    }

    @Override
    public String getAvatar() {
        return IOUtils.getContactIconUri(handle, IOUtils.IconSize.NORMAL);
    }
}