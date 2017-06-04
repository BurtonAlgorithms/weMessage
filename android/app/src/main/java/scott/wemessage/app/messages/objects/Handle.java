package scott.wemessage.app.messages.objects;

import java.util.UUID;

public class Handle {

    private UUID uuid;
    private String handleID;
    private HandleType handleType;

    public Handle(){

    }

    public Handle(UUID uuid, String handleID, HandleType type){
        this.uuid = uuid;
        this.handleID = handleID;
        this.handleType = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getHandleID() {
        return handleID;
    }

    public HandleType getHandleType() {
        return handleType;
    }

    public boolean isMe(){
        return handleType == HandleType.ME;
    }

    public Handle setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Handle setHandleID(String handleID) {
        this.handleID = handleID;
        return this;
    }

    public Handle setHandleType(HandleType handleType) {
        this.handleType = handleType;
        return this;
    }

    public enum HandleType {
        IMESSAGE("iMessage"),
        SMS("SMS"),
        ME("Me");

        String typeName;

        HandleType(String typeName){
            this.typeName = typeName;
        }

        public String getTypeName(){
            return typeName;
        }

        public static HandleType stringToHandleType(String s){
            if (s == null) return null;

            switch (s.toLowerCase()){
                case "imessage":
                    return HandleType.IMESSAGE;
                case "sms":
                    return HandleType.SMS;
                case "me":
                    return HandleType.ME;
                default:
                    return null;
            }
        }
    }
}