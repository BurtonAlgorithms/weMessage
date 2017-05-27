package scott.wemessage.commons;

public abstract class Constants {

    public static final String WEMESSAGE_VERSION = "Alpha 0.1";
    public static final int WEMESSAGE_BUILD_VERSION = 1;
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final String DEFAULT_PASSWORD = "password";

    public static final String JSON_INIT_CONNECT = "INIT CONNECT - ";
    public static final String JSON_VERIFY_PASSWORD_SECRET = "VERIFY PASSWORD - SECRET: ";
    public static final String JSON_CONNECTION_TERMINATED = "CONNECTION TERMINATED - ";
    public static final String JSON_NEW_MESSAGE = "NEW MESSAGE - ";
    public static final String JSON_ACTION = "ACTION - ";
    public static final String JSON_MESSAGE_UPDATED = "MESSAGE UPDATED - ";
    public static final String JSON_RETURN_RESULT = "RETURN RESULT - ";

    public static final int DISCONNECT_REASON_ALREADY_CONNECTED = 3000;
    public static final int DISCONNECT_REASON_INVALID_LOGIN = 3001;
    public static final int DISCONNECT_REASON_SERVER_CLOSED = 3002;
    public static final int DISCONNECT_REASON_ERROR = 3003;
    public static final int DISCONNECT_REASON_FORCED = 3004;
    public static final int DISCONNECT_REASON_CLIENT_QUIT = 3005;
    public static final int DISCONNECT_REASON_INCORRECT_VERSION = 3006;

    public static final int ACTION_SEND_MESSAGE = 4000;
    public static final int ACTION_SEND_GROUP_MESSAGE = 4001;
    public static final int ACTION_RENAME_GROUP = 4002;
    public static final int ACTION_ADD_PARTICIPANT = 4003;
    public static final int ACTION_REMOVE_PARTICIPANT = 4004;
    public static final int ACTION_LEAVE_GROUP = 4005;
    public static final int ACTION_CREATE_GROUP = 4006;

    public static final int UNKNOWN_ERROR = 999;
    public static final int SENT = 1000;
    public static final int DELIVERED = 1001;
    public static final int NO_INTERNET = 1002;
    public static final int MESSAGE_SERVER_NOT_AVAILABLE = 1003;
    public static final int INVALID_NUMBER = 1004;
    public static final int NUMBER_NOT_IMESSAGE = 1005;
    public static final int GROUP_CHAT_NOT_FOUND = 1006;
    public static final int NOT_DELIVERED = 1007;
    public static final int NOT_SENT = 1008;
    public static final int SERVICE_NOT_AVAILABLE = 1009;
    public static final int FILE_NOT_FOUND = 1010;
    public static final int NULL_MESSAGE = 1011;
    public static final int ASSISTIVE_ACCESS_DISABLED = 1012;
    public static final int UI_ERROR = 1013;
    public static final int ACTION_PERFORMED = 1014;
}
