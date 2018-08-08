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

public class JSONAction {

    @SerializedName("actionType")
    private Integer actionType;

    @SerializedName("args")
    private String[] args;

    public JSONAction(Integer actionType, String[] args){
        this.actionType = actionType;
        this.args = args;
    }

    public Integer getActionType() {
        return actionType;
    }

    public String[] getArgs() {
        return args;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}