package scott.wemessage.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

public class AndroidIOUtils {

    /**
     * Creates a Uri which parses the given encoded URI string.
     * @param context The context
     * @param resId The res ID for the required URI
     * @throws NullPointerException if uriString is null
     * @return Uri for the resource ID
     */

    public static final Uri getUriFromResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        Resources res = context.getResources();

        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));

        return resUri;
    }
}