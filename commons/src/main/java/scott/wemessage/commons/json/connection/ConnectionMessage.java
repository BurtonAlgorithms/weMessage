package scott.wemessage.commons.json.connection;

public abstract class ConnectionMessage {

    private String messageUuid;

    public ConnectionMessage(String messageUuid){
        this.messageUuid = messageUuid;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }
}