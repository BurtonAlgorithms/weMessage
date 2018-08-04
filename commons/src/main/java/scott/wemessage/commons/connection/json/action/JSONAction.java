package scott.wemessage.commons.connection.json.action;

import com.google.gson.annotations.SerializedName;

public class JSONAction {

    @SerializedName("actionType")
    private Integer actionType;

    @SerializedName("args")
    private String[] args;

    public JSONAction(Integer actionType, String[] args){
        this.actionType = actionType;
        this.args = args;
    }

    public Integer getActionType() {
        return actionType;
    }

    public String[] getArgs() {
        return args;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}