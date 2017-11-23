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
