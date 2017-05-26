package scott.wemessage.server.configuration.json;

public class ConfigJSONData {

    private Integer configVersion;
    private Integer port;
    private ConfigAccountJSON accountInfo;

    public ConfigJSONData(Integer configVersion, Integer port, ConfigAccountJSON accountInfo){
        this.configVersion = configVersion;
        this.port = port;
        this.accountInfo = accountInfo;
    }

    public Integer getConfigVersion() {
        return configVersion;
    }

    public Integer getPort() {
        return port;
    }

    public ConfigAccountJSON getAccountInfo() {
        return accountInfo;
    }

    public void setConfigVersion(Integer configVersion) {
        this.configVersion = configVersion;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setAccountInfo(ConfigAccountJSON accountInfo) {
        this.accountInfo = accountInfo;
    }
}