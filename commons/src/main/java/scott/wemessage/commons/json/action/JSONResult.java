package scott.wemessage.commons.json.action;

import java.util.List;

public class JSONResult {
    
    private String correspondingUUID;
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