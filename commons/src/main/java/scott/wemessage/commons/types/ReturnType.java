package scott.wemessage.commons.types;

import scott.wemessage.commons.Constants;

public enum ReturnType {

    UNKNOWN_ERROR(Constants.UNKNOWN_ERROR, "Unknown Error"),
    SENT(Constants.SENT, "Sent"),
    DELIVERED(Constants.DELIVERED, "Delivered"),
    NO_INTERNET(Constants.NO_INTERNET, "No Internet"),
    MESSAGE_SERVER_NOT_AVAILABLE(Constants.MESSAGE_SERVER_NOT_AVAILABLE, "Message Server not available"),
    INVALID_NUMBER(Constants.INVALID_NUMBER, "Invalid Number"),
    NUMBER_NOT_IMESSAGE(Constants.NUMBER_NOT_IMESSAGE, "Account not iMessage"),
    GROUP_CHAT_NOT_FOUND(Constants.GROUP_CHAT_NOT_FOUND, "Group Chat Not Found"),
    NOT_DELIVERED(Constants.NOT_DELIVERED, "Not Delivered"),
    NOT_SENT(Constants.NOT_SENT, "Not Sent"),
    SERVICE_NOT_AVAILABLE(Constants.SERVICE_NOT_AVAILABLE, "Service Not Available"),
    FILE_NOT_FOUND(Constants.FILE_NOT_FOUND, "File Not Found"),
    NULL_MESSAGE(Constants.NULL_MESSAGE, "Empty Message"),
    ASSISTIVE_ACCESS_DISABLED(Constants.ASSISTIVE_ACCESS_DISABLED, "Assistive Access Is Disabled"),
    UI_ERROR(Constants.UI_ERROR, "Messages Application Error"),
    ACTION_PERFORMED(Constants.ACTION_PERFORMED, "Action Performed");

    int code;
    String returnName;

    ReturnType(int code, String returnName){
        this.code = code;
        this.returnName = returnName;
    }

    public int getCode(){
        return code;
    }

    public String getReturnName(){
        return returnName;
    }

    public static ReturnType fromCode(Integer value){
        switch (value){
            case Constants.UNKNOWN_ERROR:
                return ReturnType.UNKNOWN_ERROR;
            case Constants.SENT:
                return ReturnType.SENT;
            case Constants.DELIVERED:
                return ReturnType.DELIVERED;
            case Constants.NO_INTERNET:
                return ReturnType.NO_INTERNET;
            case Constants.MESSAGE_SERVER_NOT_AVAILABLE:
                return ReturnType.MESSAGE_SERVER_NOT_AVAILABLE;
            case Constants.INVALID_NUMBER:
                return ReturnType.INVALID_NUMBER;
            case Constants.NUMBER_NOT_IMESSAGE:
                return ReturnType.NUMBER_NOT_IMESSAGE;
            case Constants.GROUP_CHAT_NOT_FOUND:
                return ReturnType.GROUP_CHAT_NOT_FOUND;
            case Constants.NOT_DELIVERED:
                return ReturnType.NOT_DELIVERED;
            case Constants.NOT_SENT:
                return ReturnType.NOT_SENT;
            case Constants.SERVICE_NOT_AVAILABLE:
                return ReturnType.SERVICE_NOT_AVAILABLE;
            case Constants.FILE_NOT_FOUND:
                return ReturnType.FILE_NOT_FOUND;
            case Constants.NULL_MESSAGE:
                return ReturnType.NULL_MESSAGE;
            case Constants.ASSISTIVE_ACCESS_DISABLED:
                return ReturnType.ASSISTIVE_ACCESS_DISABLED;
            case Constants.UI_ERROR:
                return ReturnType.UI_ERROR;
            case Constants.ACTION_PERFORMED:
                return ReturnType.ACTION_PERFORMED;
            default:
                return null;
        }
    }
}