package scott.wemessage.app;

import android.util.Log;

public final class AppLogger {

    private AppLogger() { }

    private static final String DEFAULT_TAG = "weMessage";

    public static void log(String message){
        log(null, null, message);
    }

    public static void log(String prefix, String message){
        log(null, prefix, message);
    }

    public static void log(Level level, String tag, String message){
        if (level == null){
            if(tag == null) {
                Log.i(DEFAULT_TAG, message);
            }else {
                Log.i(DEFAULT_TAG, message);
            }
            return;
        }

        switch(level){
            case INFO:
                Log.i(tag, message);
                break;
            case WARNING:
                Log.w(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
            default:
                if(tag == null) {
                    Log.i(DEFAULT_TAG, message);
                }else {
                    Log.i(tag, message);
                }
        }
    }

    public static void error(String tag, String message, Exception ex){
        if(tag == null) {
            Log.e(DEFAULT_TAG, message, ex);
        }else {
            Log.e(tag, message, ex);
        }
    }

    public static void error(String message, Exception ex){
        error(null, message, ex);
    }

    public enum Level {
        INFO,
        WARNING,
        ERROR
    }
}