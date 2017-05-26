package scott.wemessage.server.configuration;

import scott.wemessage.server.MessageServer;
import scott.wemessage.server.configuration.json.ConfigJSON;
import scott.wemessage.server.configuration.json.ConfigJSONData;
import scott.wemessage.server.configuration.json.ConfigAccountJSON;
import scott.wemessage.server.utils.FileUtils;
import scott.wemessage.server.utils.LoggingUtils;
import scott.wemessage.server.weMessage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ServerConfiguration {

    private final Object parentDirectoryLock = new Object();
    private final Object configFileLock = new Object();

    private final String version = weMessage.WEMESSAGE_VERSION;
    private final String configFileName = weMessage.CONFIG_FILE_NAME;
    private final int buildVersion = weMessage.WEMESSAGE_BUILD_VERSION;
    private final int port;

    private final String parentPathDirectory;
    private File parentDirectory;
    private File configFile;

    public ServerConfiguration(MessageServer messageServer) throws IOException {
        this.parentPathDirectory = Paths.get("").toAbsolutePath().toString();
        this.parentDirectory = new File(parentPathDirectory);

        File configFile = new File(parentDirectory, configFileName);

        if(!configFile.exists()){
            createConfig(configFile);
        }

        Gson gson = new Gson();
        String jsonString = FileUtils.readFile(configFile.getPath(), StandardCharsets.UTF_8);
        ConfigJSON configJSON = gson.fromJson(jsonString, ConfigJSON.class);

        if (configJSON.getConfig().getConfigVersion() != weMessage.WEMESSAGE_CONFIG_VERSION){
            LoggingUtils.log(LoggingUtils.Level.ERROR, messageServer.TAG, "The config version and the server version do not match! Resetting config.");
            LoggingUtils.log(LoggingUtils.Level.ERROR, messageServer.TAG, "Note: You will have to reconfigure your config details. Shutting down!");

            configFile.delete();
            createConfig(configFile);
            messageServer.shutdown(-1, false);
        }

        synchronized (configFileLock) {
            this.configFile = configFile;
        }
        this.port = configJSON.getConfig().getPort();
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

    public final String getParentDirectoryPath(){
        return parentPathDirectory;
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

    public ConfigJSON getConfigJSON() throws IOException {
        String jsonString = FileUtils.readFile(getConfigFile().getPath(), StandardCharsets.UTF_8);
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
}