package scott.wemessage.app.utils;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

public class AndroidUtils {

    public static String getDeviceName(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        String deviceName;

        if (adapter != null) {
            deviceName = adapter.getName();
        } else {
            deviceName = getInternalDeviceName();
        }
        return deviceName;
    }

    private static String getInternalDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);

        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}