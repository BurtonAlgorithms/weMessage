package scott.wemessage.app.messages;

public class Handle {

    private String handleID;
    private HandleType handleType;

    public Handle(String handleID, HandleType type){
        this.handleID = handleID;
        this.handleType = type;
    }

    public String getHandleID() {
        return handleID;
    }

    public HandleType getHandleType() {
        return handleType;
    }

    public void setHandleID(String handleID) {
        this.handleID = handleID;
    }

    public void setHandleType(HandleType handleType) {
        this.handleType = handleType;
    }

    public enum HandleType {
        IMESSAGE,
        SMS
    }
}