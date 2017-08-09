package scott.wemessage.server;

import scott.wemessage.commons.Constants;

public final class weMessage implements Constants {

    private weMessage(){ }

    public static final int WEMESSAGE_CONFIG_VERSION = 1;
    public static final int MIN_OS_VERSION = 12;
    public static final boolean CREATE_LOG_FILES = true;

    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String LOG_FILE_NAME = "latest.log";
    public static final String SERVER_DATABASE_FILE_NAME = "weserver.db";
    public static final String DEFAULT_FFMPEG_LOCATION = "bin/ffmpeg/ffmpeg";
    public static final String DEFAULT_EMAIL = "email@icloud.com";
    public static final String DEFAULT_SECRET = "secret";
}