package scott.wemessage.commons.connection.json.action;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JSONResult {

    @SerializedName("correspondingUUID")
    private String correspondingUUID;

    @SerializedName("result")
    private List<Integer> result;
    
    public JSONResult(String correspondingActionUUID, List<Integer> results){
        this.correspondingUUID = correspondingActionUUID;
        this.result = results;
    }

    public String getCorrespondingUUID() {
        return correspondingUUID;
    }

    public List<Integer> getResult() {
        return result;
    }

    public void setResult(List<Integer> result) {
        this.result = result;
    }

    public void setCorrespondingUUID(String correspondingUUID) {
        this.correspondingUUID = correspondingUUID;
    }
}