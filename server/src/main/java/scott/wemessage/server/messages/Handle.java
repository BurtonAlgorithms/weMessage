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

package scott.wemessage.server.messages;

public class Handle {

    private String handleID;
    private long rowID;
    private String country;

    public Handle(){
        this(null, -1L, null);
    }

    public Handle(String handleID, long rowID, String country){
        this.handleID = handleID;
        this.rowID = rowID;
        this.country = country;
    }

    public String getHandleID() {
        return handleID;
    }

    public long getRowID() {
        return rowID;
    }

    public String getCountry() {
        return country;
    }

    public Handle setHandleID(String handleID) {
        this.handleID = handleID;
        return this;
    }

    public Handle setRowID(long rowID) {
        this.rowID = rowID;
        return this;
    }

    public Handle setCountry(String country) {
        this.country = country;
        return this;
    }
}