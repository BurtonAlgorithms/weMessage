package scott.wemessage.commons.json.connection;

public class ServerMessage {

    private String messageUuid;
    private Object outgoing;

    public ServerMessage(String messageUuid, Object outgoing){
        this.messageUuid = messageUuid;
        this.outgoing = outgoing;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public Object getOutgoing() {
        return outgoing;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public void setOutgoing(Object outgoing) {
        this.outgoing = outgoing;
    }
}