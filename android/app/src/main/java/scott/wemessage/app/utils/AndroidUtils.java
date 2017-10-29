package scott.wemessage.app.utils;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.webkit.MimeTypeMap;

import scott.wemessage.commons.types.MimeType;

public class AndroidUtils {

    public static boolean hasMemoryForOperation(long memoryNeeded){
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long maxHeapSize = runtime.maxMemory() / 1048576L;
        long availableHeapSize = maxHeapSize - usedMemory;

        if ((memoryNeeded / 1048576L) > (availableHeapSize - 10)) {
            return false;
        }
        return true;
    }

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

    public static MimeType getMimeTypeFromPath(String path){
        return MimeType.getTypeFromString(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase()));
    }

    public static String getMimeTypeStringFromPath(String path){
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase());
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