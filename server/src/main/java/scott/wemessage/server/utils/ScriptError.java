package scott.wemessage.server.utils;

import com.google.gson.annotations.SerializedName;

public class ScriptError {

    @SerializedName("callScript")
    public String callScript;

    @SerializedName("error")
    public String error;

    public ScriptError(String callScript, String error){
        this.callScript = callScript;
        this.error = error;
    }

    public String getCallScript() {
        return callScript;
    }

    public String getError() {
        return error;
    }

    public void setCallScript(String callScript) {
        this.callScript = callScript;
    }

    public void setError(String error) {
        this.error = error;
    }
}
