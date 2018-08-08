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

package scott.wemessage.server.utils;

import com.google.gson.annotations.SerializedName;

public class ScriptError {

    @SerializedName("callScript")
    public String callScript;

    @SerializedName("error")
    public String error;

    public ScriptError(String callScript, String error){
        this.callScript = callScript;
        this.error = error;
    }

    public String getCallScript() {
        return callScript;
    }

    public String getError() {
        return error;
    }

    public void setCallScript(String callScript) {
        this.callScript = callScript;
    }

    public void setError(String error) {
        this.error = error;
    }
}
