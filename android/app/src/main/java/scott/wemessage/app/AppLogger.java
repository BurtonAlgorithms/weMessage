/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

public final class AppLogger {

    private static final String DEFAULT_TAG = "weMessage";
    static final boolean USE_CRASHLYTICS = true;

    private AppLogger() { }

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

            if (USE_CRASHLYTICS) {
                Crashlytics.log(message);
                Crashlytics.logException(ex);
            }
        }else {
            Log.e(tag, message, ex);

            if (USE_CRASHLYTICS) {
                Crashlytics.log(tag + "  " + message);
                Crashlytics.logException(ex);
            }
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