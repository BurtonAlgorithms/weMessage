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

package scott.wemessage.commons.connection;

import com.google.gson.annotations.SerializedName;

public class FirebaseVersionMessage {

    @SerializedName("latestVersion")
    private String latestVersion;

    @SerializedName("latestBuildVersion")
    private String latestBuildVersion;

    public FirebaseVersionMessage(String latestVersion, String latestBuildVersion){
        this.latestVersion = latestVersion;
        this.latestBuildVersion = latestBuildVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public int getLatestBuildVersion() {
        return Integer.parseInt(latestBuildVersion);
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void setLatestBuildVersion(int latestBuildVersion) {
        this.latestBuildVersion = String.valueOf(latestBuildVersion);
    }
}