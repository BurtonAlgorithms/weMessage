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

package scott.wemessage.app.models.users;

import java.util.UUID;

public abstract class ContactInfo {

    public abstract UUID getUuid();

    public abstract String getDisplayName();

    public abstract ContactInfo findRoot();

    public abstract Handle pullHandle(boolean iMessage);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof Handle){
            Handle objectHandle = (Handle) obj;

            if (this instanceof Handle){
                if (objectHandle.getHandleID().equals(((Handle) this).getHandleID())) return true;
            }else if (this instanceof Contact){
                for (Handle h : ((Contact) this).getHandles()){
                    if (h.getHandleID().equals(objectHandle.getHandleID())) return true;
                }
            }
        }else if (obj instanceof Contact){
            Contact objectContact = (Contact) obj;

            if (this instanceof Handle){
                for (Handle h : objectContact.getHandles()){
                    if (h.getHandleID().equals(((Handle) this).getHandleID())) return true;
                }
            }else if (this instanceof Contact){
                if (objectContact.getUuid().toString().equals(getUuid().toString())) return true;
            }
        }
        return false;
    }
}