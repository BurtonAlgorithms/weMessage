package scott.wemessage.server;

import scott.wemessage.commons.Constants;

public final class weMessage extends Constants {

    private weMessage(){}

    public static final int WEMESSAGE_CONFIG_VERSION = 1;
    public static final int DEFAULT_PORT = 2222;
    public static final int MINIMUM_CONNECT_PASSWORD_LENGTH = 8;

    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String SERVER_DATABASE_FILE_NAME = "weserver.db";
    public static final String DEFAULT_EMAIL = "email@icloud.com";
    public static final String DEFAULT_PASSWORD = "password";
    public static final String DEFAULT_SECRET = "secret";

}