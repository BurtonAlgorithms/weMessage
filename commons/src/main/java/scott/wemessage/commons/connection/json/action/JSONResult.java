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

package scott.wemessage.commons.connection.json.action;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JSONResult {

    @SerializedName("correspondingUUID")
    private String correspondingUUID;

    @SerializedName("result")
    private List<Integer> result;
    
    public JSONResult(String correspondingActionUUID, List<Integer> results){
        this.correspondingUUID = correspondingActionUUID;
        this.result = results;
    }

    public String getCorrespondingUUID() {
        return correspondingUUID;
    }

    public List<Integer> getResult() {
        return result;
    }

    public void setResult(List<Integer> result) {
        this.result = result;
    }

    public void setCorrespondingUUID(String correspondingUUID) {
        this.correspondingUUID = correspondingUUID;
    }
}