package scott.wemessage.server.configuration.json;

public class ConfigJSON {

    private ConfigJSONData config;

    public ConfigJSON(ConfigJSONData config){
        this.config = config;
    }

    public ConfigJSONData getConfig() {
        return config;
    }

    public void setConfig(ConfigJSONData config) {
        this.config = config;
    }
}