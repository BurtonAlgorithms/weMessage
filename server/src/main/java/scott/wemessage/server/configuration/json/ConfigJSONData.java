package scott.wemessage.server.configuration.json;

public class ConfigJSONData {

    private Integer configVersion;
    private Integer port;
    private Boolean createLogFiles;
    private ConfigAccountJSON accountInfo;

    public ConfigJSONData(Integer configVersion, Integer port, Boolean createLogFiles, ConfigAccountJSON accountInfo){
        this.configVersion = configVersion;
        this.port = port;
        this.createLogFiles = createLogFiles;
        this.accountInfo = accountInfo;
    }

    public Integer getConfigVersion() {
        return configVersion;
    }

    public Integer getPort() {
        return port;
    }

    public Boolean getCreateLogFiles() {
        return createLogFiles;
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

    public void setCreateLogFiles(Boolean createLogFiles) {
        this.createLogFiles = createLogFiles;
    }

    public void setAccountInfo(ConfigAccountJSON accountInfo) {
        this.accountInfo = accountInfo;
    }
}