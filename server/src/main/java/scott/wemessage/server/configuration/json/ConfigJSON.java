package scott.wemessage.server.configuration.json;

import com.google.gson.annotations.SerializedName;

public class ConfigJSON {

    @SerializedName("config")
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