package scott.wemessage.commons.json.connection;

public class ClientMessage {

    private String messageUuid;
    private Object incoming;

    public ClientMessage(String messageUuid, Object incoming){
        this.messageUuid = messageUuid;
        this.incoming = incoming;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public Object getIncoming() {
        return incoming;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public void setIncoming(Object incoming) {
        this.incoming = incoming;
    }
}