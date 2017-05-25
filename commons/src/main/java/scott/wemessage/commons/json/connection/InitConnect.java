package scott.wemessage.commons.json.connection;

import scott.wemessage.commons.json.message.security.JSONEncryptedText;

public class InitConnect {

    private int buildVersion;
    private String deviceId;
    private JSONEncryptedText email;
    private JSONEncryptedText password;
    private String deviceType;

    public InitConnect(int buildVersion, String deviceId, JSONEncryptedText email, JSONEncryptedText password, String deviceType){
        this.buildVersion = buildVersion;
        this.deviceId = deviceId;
        this.email = email;
        this.password = password;
        this.deviceType = deviceType;
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
}
