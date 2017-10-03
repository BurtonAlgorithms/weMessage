package scott.wemessage.server.configuration.json;

public class ConfigJSONData {

    private Integer configVersion;
    private Integer port;
    private Boolean createLogFiles;
    private Boolean checkForUpdates;
    private Boolean sendNotifications;
    private Boolean transcodeVideos;
    private String ffmpegLocation;
    private ConfigAccountJSON accountInfo;

    public ConfigJSONData(){

    }

    public ConfigJSONData(Integer configVersion, Integer port, Boolean createLogFiles,
                          Boolean checkForUpdates, Boolean sendNotifications, Boolean transcodeVideos,
                          String ffmpegLocation, ConfigAccountJSON accountInfo){
        this.configVersion = configVersion;
        this.port = port;
        this.createLogFiles = createLogFiles;
        this.checkForUpdates = checkForUpdates;
        this.sendNotifications = sendNotifications;
        this.transcodeVideos = transcodeVideos;
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

    public Boolean getCheckForUpdates() {
        return checkForUpdates;
    }

    public Boolean getSendNotifications() {
        return sendNotifications;
    }

    public Boolean getTranscodeVideos() {
        return transcodeVideos;
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

    public void setCheckForUpdates(Boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
    }

    public void setSendNotifications(Boolean sendNotifications) {
        this.sendNotifications = sendNotifications;
    }

    public void setTranscodeVideos(Boolean transcodeVideos) {
        this.transcodeVideos = transcodeVideos;
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public void setAccountInfo(ConfigAccountJSON accountInfo) {
        this.accountInfo = accountInfo;
    }
}