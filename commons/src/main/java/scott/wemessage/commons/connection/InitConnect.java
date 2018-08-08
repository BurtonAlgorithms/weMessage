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

import scott.wemessage.commons.connection.security.EncryptedText;

public class InitConnect {

    @SerializedName("buildVersion")
    private int buildVersion;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("email")
    private EncryptedText email;

    @SerializedName("password")
    private EncryptedText password;

    @SerializedName("deviceType")
    private String deviceType;

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("registrationToken")
    private String registrationToken;

    public InitConnect(int buildVersion, String deviceId, EncryptedText email, EncryptedText password, String deviceType, String deviceName, String registrationToken){
        this.buildVersion = buildVersion;
        this.deviceId = deviceId;
        this.email = email;
        this.password = password;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.registrationToken = registrationToken;
    }

    public int getBuildVersion() {
        return buildVersion;
    }

    public String getDeviceId(){
        return deviceId;
    }

    public EncryptedText getEmail() {
        return email;
    }

    public EncryptedText getPassword() {
        return password;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setBuildVersion(int buildVersion) {
        this.buildVersion = buildVersion;
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId;
    }

    public void setEmail(EncryptedText email) {
        this.email = email;
    }

    public void setPassword(EncryptedText password) {
        this.password = password;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }
}
