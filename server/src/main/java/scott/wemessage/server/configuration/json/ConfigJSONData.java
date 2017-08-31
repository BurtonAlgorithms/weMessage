package scott.wemessage.server.configuration.json;

public class ConfigJSONData {

    private Integer configVersion;
    private Integer port;
    private Boolean createLogFiles;
    private String ffmpegLocation;
    private ConfigAccountJSON accountInfo;

    public ConfigJSONData(){

    }

    public ConfigJSONData(Integer configVersion, Integer port, Boolean createLogFiles, String ffmpegLocation, ConfigAccountJSON accountInfo){
        this.configVersion = configVersion;
        this.port = port;
        this.createLogFiles = createLogFiles;
        this.ffmpegLocation = ffmpegLocation;
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

    public String getFfmpegLocation() {
        return ffmpegLocation;
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

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public void setAccountInfo(ConfigAccountJSON accountInfo) {
        this.accountInfo = accountInfo;
    }
}