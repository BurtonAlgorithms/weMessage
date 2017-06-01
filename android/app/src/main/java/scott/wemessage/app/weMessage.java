package scott.wemessage.app;

import scott.wemessage.commons.Constants;

public final class weMessage extends Constants {

    private weMessage(){ }

    public static final int CONNECTION_TIMEOUT_WAIT = 15;
    public static final String IDENTIFIER_PREFIX = "scott.wemessage.app.";

    public static final String BUNDLE_HOST = IDENTIFIER_PREFIX + "bundleHost";
    public static final String BUNDLE_EMAIL = IDENTIFIER_PREFIX + "bundleEmail";
    public static final String BUNDLE_PASSWORD = IDENTIFIER_PREFIX + "bundlePassword";
    public static final String BUNDLE_ALERT_TITLE = IDENTIFIER_PREFIX + "bundleAlertTitle";
    public static final String BUNDLE_ALERT_MESSAGE = IDENTIFIER_PREFIX + "bundleAlertMessage";
    public static final String BUNDLE_DIALOG_ANIMATION = IDENTIFIER_PREFIX + "bundleDialogAnimation";
    public static final String BUNDLE_IS_BOUND_TO_CONNECTION_SERVICE = IDENTIFIER_PREFIX + "bundleIsBoundToConnectionService";
    public static final String BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE = IDENTIFIER_PREFIX + "bundleDisconnectReasonAlternateMessage";

    public static final String ARG_HOST = IDENTIFIER_PREFIX + "hostArg";
    public static final String ARG_PORT = IDENTIFIER_PREFIX + "portArg";
    public static final String ARG_EMAIL = IDENTIFIER_PREFIX + "emailArg";
    public static final String ARG_PASSWORD = IDENTIFIER_PREFIX + "passwordArg";

    public static final String INTENT_LOGIN_TIMEOUT = IDENTIFIER_PREFIX + "LoginTimeout";
    public static final String INTENT_LOGIN_ERROR = IDENTIFIER_PREFIX + "LoginError";
    public static final String INTENT_CONNECTION_SERVICE_STOPPED = IDENTIFIER_PREFIX + "ConnectionServiceStopped";

    public static final String BROADCAST_DISCONNECT_REASON_ALREADY_CONNECTED = IDENTIFIER_PREFIX + "DisconnectReasonAlreadyConnected";
    public static final String BROADCAST_DISCONNECT_REASON_INVALID_LOGIN = IDENTIFIER_PREFIX + "DisconnectReasonInvalidLogin";
    public static final String BROADCAST_DISCONNECT_REASON_SERVER_CLOSED = IDENTIFIER_PREFIX + "DisconnectReasonServerClosed";
    public static final String BROADCAST_DISCONNECT_REASON_ERROR = IDENTIFIER_PREFIX + "DisconnectReasonError";
    public static final String BROADCAST_DISCONNECT_REASON_FORCED = IDENTIFIER_PREFIX + "DisconnectReasonForced";
    public static final String BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED = IDENTIFIER_PREFIX + "DisconnectReasonClientDisconnected";
    public static final String BROADCAST_DISCONNECT_REASON_INCORRECT_VERSION = IDENTIFIER_PREFIX + "DisconnectReasonIncorrectVersion";

    public static final String BROADCAST_LOGIN_SUCCESSFUL = IDENTIFIER_PREFIX + "LoginSuccessful";
}