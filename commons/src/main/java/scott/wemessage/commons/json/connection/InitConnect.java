package scott.wemessage.commons.json.connection;

import scott.wemessage.commons.json.message.security.JSONEncryptedText;

public class InitConnect {

    private int buildVersion;
    private String deviceId;
    private JSONEncryptedText email;
    private JSONEncryptedText password;
    private String deviceType;
    private String deviceName;
    private String registrationToken;

    public InitConnect(int buildVersion, String deviceId, JSONEncryptedText email, JSONEncryptedText password, String deviceType, String deviceName, String registrationToken){
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

    public JSONEncryptedText getEmail() {
        return email;
    }

    public JSONEncryptedText getPassword() {
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

    public void setEmail(JSONEncryptedText email) {
        this.email = email;
    }

    public void setPassword(JSONEncryptedText password) {
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
