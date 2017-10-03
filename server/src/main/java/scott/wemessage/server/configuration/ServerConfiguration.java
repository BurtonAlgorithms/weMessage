package scott.wemessage.server.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.configuration.json.ConfigAccountJSON;
import scott.wemessage.server.configuration.json.ConfigJSON;
import scott.wemessage.server.configuration.json.ConfigJSONData;
import scott.wemessage.server.weMessage;

public final class ServerConfiguration {

    private final Object parentDirectoryLock = new Object();
    private final Object configFileLock = new Object();
    private final Object logFileLock = new Object();

    private final String version = weMessage.WEMESSAGE_VERSION;
    private final String configFileName = weMessage.CONFIG_FILE_NAME;
    private final String logFileName = weMessage.LOG_FILE_NAME;
    private final int buildVersion = weMessage.WEMESSAGE_BUILD_VERSION;
    private final int port;
    private final boolean saveLogsToFile;
    private final String parentPathDirectory;

    private MessageServer messageServer;
    private File parentDirectory;
    private File configFile;
    private File logFile;

    public ServerConfiguration(MessageServer messageServer) throws IOException {
        this.messageServer = messageServer;
        this.parentPathDirectory = Paths.get("").toAbsolutePath().toString();
        this.parentDirectory = new File(parentPathDirectory);

        File configFile = new File(parentDirectory, configFileName);
        File logFile = new File(parentDirectory, logFileName);

        if(!configFile.exists()){
            createConfig(configFile);
        }

        Gson gson = new Gson();
        String jsonString = FileUtils.readFile(configFile.getPath());
        ConfigJSON configJSON = gson.fromJson(jsonString, ConfigJSON.class);

        if (configJSON.getConfig().getConfigVersion() != weMessage.WEMESSAGE_CONFIG_VERSION){
            if (configJSON.getConfig().getConfigVersion() < weMessage.WEMESSAGE_CONFIG_VERSION){
                onUpgrade(weMessage.WEMESSAGE_CONFIG_VERSION, configJSON.getConfig().getConfigVersion(), configJSON.getConfig(), configFile);
            }else if (configJSON.getConfig().getConfigVersion() > weMessage.WEMESSAGE_CONFIG_VERSION){
                onDowngrade(weMessage.WEMESSAGE_CONFIG_VERSION, configJSON.getConfig().getConfigVersion(), configJSON.getConfig(), configFile);
            }
        }

        synchronized (configFileLock) {
            this.configFile = configFile;
        }

        logFile.delete();

        if (configJSON.getConfig().getCreateLogFiles()){
            logFile.createNewFile();

            synchronized (logFileLock){
                this.logFile = logFile;
            }
        }
        this.port = configJSON.getConfig().getPort();
        this.saveLogsToFile = configJSON.getConfig().getCreateLogFiles();
    }

    public final String getVersion(){
        return version;
    }

    public final String getConfigFileName(){
        return configFileName;
    }

    public final int getBuildVersion(){
        return buildVersion;
    }

    public final int getPort(){
        return port;
    }

    public boolean saveLogFiles() {
        return saveLogsToFile;
    }

    public final String getParentDirectoryPath(){
        return parentPathDirectory;
    }

    public String getAccountEmail() throws IOException {
        return getConfigJSON().getConfig().getAccountInfo().getEmail();
    }

    public File getParentDirectory(){
        synchronized (parentDirectoryLock) {
            return parentDirectory;
        }
    }

    public File getConfigFile(){
        synchronized (configFileLock) {
            return configFile;
        }
    }

    public File getLogFile(){
        synchronized (logFileLock){
            return logFile;
        }
    }

    public ConfigJSON getConfigJSON() throws IOException {
        String jsonString = FileUtils.readFile(getConfigFile().getPath());
        return new Gson().fromJson(jsonString, ConfigJSON.class);
    }

    public void writeJsonToConfig(ConfigJSON json) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(json);
        try (PrintWriter out = new PrintWriter(getConfigFile())){
            out.println(jsonOutput);
        }
    }

    private void createConfig(File file) throws IOException {
        file.createNewFile();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ConfigJSON configJSON = new ConfigJSON(
                new ConfigJSONData(weMessage.WEMESSAGE_CONFIG_VERSION, weMessage.DEFAULT_PORT,
                        weMessage.DEFAULT_CREATE_LOG_FILES, weMessage.DEFAULT_CHECK_FOR_UPDATES,
                        weMessage.DEFAULT_SEND_NOTIFICATIONS, weMessage.DEFAULT_TRANSCODE_VIDEO,
                        weMessage.DEFAULT_FFMPEG_LOCATION,
                        new ConfigAccountJSON(
                                weMessage.DEFAULT_EMAIL,
                                weMessage.DEFAULT_PASSWORD,
                                weMessage.DEFAULT_SECRET
                        ))
        );
        String jsonOutput = gson.toJson(configJSON);
        try (PrintWriter out = new PrintWriter(file)){
            out.println(jsonOutput);
        }
    }

    private void createConfigWithJSON(File file, ConfigJSON configJSON) throws IOException{
        file.createNewFile();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String jsonOutput = gson.toJson(configJSON);
        try (PrintWriter out = new PrintWriter(file)){
            out.println(jsonOutput);
        }
    }

    private void onUpgrade(int newVersion, int oldVersion, ConfigJSONData oldJson, File configFile) throws IOException {
        ConfigJSONData newJson = new ConfigJSONData();
        newJson.setConfigVersion(newVersion);

        if (oldVersion >= 1){
            newJson.setPort(oldJson.getPort());
            newJson.setCreateLogFiles(oldJson.getCreateLogFiles());
            newJson.setFfmpegLocation(oldJson.getFfmpegLocation());
            newJson.setAccountInfo(oldJson.getAccountInfo());

            newJson.setCheckForUpdates(weMessage.DEFAULT_CHECK_FOR_UPDATES);
            newJson.setSendNotifications(weMessage.DEFAULT_SEND_NOTIFICATIONS);
            newJson.setTranscodeVideos(weMessage.DEFAULT_TRANSCODE_VIDEO);
        }

        if (oldVersion >= 2){
            newJson.setCheckForUpdates(oldJson.getCheckForUpdates());
            newJson.setSendNotifications(oldJson.getSendNotifications());
            newJson.setTranscodeVideos(oldJson.getTranscodeVideos());
        }

        configFile.delete();
        createConfigWithJSON(configFile, new ConfigJSON(newJson));
    }

    private void onDowngrade(int newVersion, int oldVersion, ConfigJSONData oldJson, File configFile) throws IOException {
        ConfigJSONData newJson = new ConfigJSONData();
        newJson.setConfigVersion(newVersion);

        if (newVersion >= 1){
            newJson.setPort(oldJson.getPort());
            newJson.setCreateLogFiles(oldJson.getCreateLogFiles());
            newJson.setFfmpegLocation(oldJson.getFfmpegLocation());
            newJson.setAccountInfo(oldJson.getAccountInfo());
        }

        if (newVersion >= 2){
            newJson.setCheckForUpdates(oldJson.getCheckForUpdates());
            newJson.setSendNotifications(oldJson.getSendNotifications());
            newJson.setTranscodeVideos(oldJson.getTranscodeVideos());
        }

        configFile.delete();
        createConfigWithJSON(configFile, new ConfigJSON(newJson));
    }
}