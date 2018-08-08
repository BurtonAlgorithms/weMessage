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

package scott.wemessage.commons.connection.json.message;

import com.google.gson.annotations.SerializedName;

public class JSONContact {

    @SerializedName("id")
    private String id;

    @SerializedName("handleId")
    private String handleId;

    @SerializedName("name")
    private String name;

    @SerializedName("emails")
    private String emails;

    @SerializedName("numbers")
    private String numbers;

    public JSONContact(String id, String handleId, String name, String emails, String numbers){
        this.id = id;
        this.handleId = handleId;
        this.name = name;
        this.emails = emails;
        this.numbers = numbers;
    }

    public String getId() {
        return id;
    }

    public String getHandleId() {
        return handleId;
    }

    public String getName() {
        return name;
    }

    public String getEmails() {
        return emails;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHandleId(String handleId) {
        this.handleId = handleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }
}