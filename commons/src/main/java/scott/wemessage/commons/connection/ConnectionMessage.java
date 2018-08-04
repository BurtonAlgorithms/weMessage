package scott.wemessage.commons.connection;

import com.google.gson.annotations.SerializedName;

public abstract class ConnectionMessage {

    @SerializedName("messageUuid")
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