package scott.wemessage.commons.json.action;

public class JSONAction {

    private Integer methodType;
    private String[] args;

    public JSONAction(Integer methodType, String[] args){
        this.methodType = methodType;
        this.args = args;
    }

    public Integer getMethodType() {
        return methodType;
    }

    public String[] getArgs() {
        return args;
    }

    public void setMethodType(Integer methodType) {
        this.methodType = methodType;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}