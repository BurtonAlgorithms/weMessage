package scott.wemessage.commons.types;

import scott.wemessage.commons.Constants;

public enum DisconnectReason {

    ALREADY_CONNECTED(Constants.DISCONNECT_REASON_ALREADY_CONNECTED),
    INVALID_LOGIN(Constants.DISCONNECT_REASON_INVALID_LOGIN),
    SERVER_CLOSED(Constants.DISCONNECT_REASON_SERVER_CLOSED),
    ERROR(Constants.DISCONNECT_REASON_ERROR),
    FORCED(Constants.DISCONNECT_REASON_FORCED),
    CLIENT_DISCONNECTED(Constants.DISCONNECT_REASON_CLIENT_QUIT),
    INCORRECT_VERSION(Constants.DISCONNECT_REASON_INCORRECT_VERSION);

    Integer code;

    DisconnectReason(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return code;
    }

    public static DisconnectReason fromCode(Integer value){
        switch (value){
            case Constants.DISCONNECT_REASON_ALREADY_CONNECTED:
                return DisconnectReason.ALREADY_CONNECTED;
            case Constants.DISCONNECT_REASON_INVALID_LOGIN:
                return DisconnectReason.INVALID_LOGIN;
            case Constants.DISCONNECT_REASON_SERVER_CLOSED:
                return DisconnectReason.SERVER_CLOSED;
            case Constants.DISCONNECT_REASON_ERROR:
                return DisconnectReason.ERROR;
            case Constants.DISCONNECT_REASON_FORCED:
                return DisconnectReason.FORCED;
            case Constants.DISCONNECT_REASON_CLIENT_QUIT:
                return DisconnectReason.CLIENT_DISCONNECTED;
            case Constants.DISCONNECT_REASON_INCORRECT_VERSION:
                return DisconnectReason.INCORRECT_VERSION;
            default:
                return null;
        }
    }
}