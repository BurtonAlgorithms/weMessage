package scott.wemessage.server;

import scott.wemessage.commons.Constants;

public final class weMessage implements Constants {

    private weMessage(){ }

    public static final int WEMESSAGE_CONFIG_VERSION = 2;
    public static final int WEMESSAGE_DATABASE_VERSION = 2;
    public static final int WEMESSAGE_APPLESCRIPT_VERSION = 10;
    public static final int MIN_OS_VERSION = 10;

    public static final boolean DEFAULT_CREATE_LOG_FILES = true;
    public static final boolean DEFAULT_CHECK_FOR_UPDATES = true;
    public static final boolean DEFAULT_SEND_NOTIFICATIONS = true;
    public static final boolean DEFAULT_TRANSCODE_VIDEO = true;

    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String LOG_FILE_NAME = "latest.log";
    public static final String SERVER_DATABASE_FILE_NAME = "weserver.db";
    public static final String DEFAULT_FFMPEG_LOCATION = "bin/ffmpeg/ffmpeg";
    public static final String DEFAULT_EMAIL = "email@icloud.com";
    public static final String DEFAULT_SECRET = "secret";

    public static final String GET_VERSION_FUNCTION_URL = "https://us-central1-wemessage-app.cloudfunctions.net/getVersion";
    public static final String NOTIFICATION_FUNCTION_URL = "https://us-central1-wemessage-app.cloudfunctions.net/sendNotification";
    public static final String SENTRY_DSN = "https://b57de61f5e5d4898999fb49408675ae5:32185c3f6298464fab810ffa40a54b74@sentry.io/250857";
}