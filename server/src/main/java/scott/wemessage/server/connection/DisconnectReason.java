package scott.wemessage.server.connection;

import scott.wemessage.server.weMessage;

public enum DisconnectReason {

    ALREADY_CONNECTED(weMessage.DISCONNECT_REASON_ALREADY_CONNECTED),
    INVALID_LOGIN(weMessage.DISCONNECT_REASON_INVALID_LOGIN),
    SERVER_CLOSED(weMessage.DISCONNECT_REASON_SERVER_CLOSED),
    ERROR(weMessage.DISCONNECT_REASON_ERROR),
    FORCED(weMessage.DISCONNECT_REASON_FORCED),
    CLIENT_DISCONNECTED(weMessage.DISCONNECT_REASON_CLIENT_QUIT),
    INCORRECT_VERSION(weMessage.DISCONNECT_REASON_INCORRECT_VERSION);

    int code;

    DisconnectReason(Integer code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}