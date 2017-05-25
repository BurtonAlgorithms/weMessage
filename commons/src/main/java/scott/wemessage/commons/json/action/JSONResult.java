package scott.wemessage.commons.json.action;

import java.util.List;

public class JSONResult {
    
    private String correspondingActionUUID;
    private List<Integer> result;
    
    public JSONResult(String correspondingActionUUID, List<Integer> results){
        this.correspondingActionUUID = correspondingActionUUID;
        this.result = results;
    }

    public String getCorrespondingActionUUID() {
        return correspondingActionUUID;
    }

    public List<Integer> getResult() {
        return result;
    }

    public void setResult(List<Integer> result) {
        this.result = result;
    }

    public void setCorrespondingActionUUID(String correspondingActionUUID) {
        this.correspondingActionUUID = correspondingActionUUID;
    }
}