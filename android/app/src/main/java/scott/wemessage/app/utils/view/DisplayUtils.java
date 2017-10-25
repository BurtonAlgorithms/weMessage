package scott.wemessage.app.utils.view;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import scott.wemessage.R;

public class DisplayUtils {

    public static int convertDpToRoundedPixel(float dp, Context context){
        return Math.round(convertDpToPixel(dp, context));
    }

    public static float convertDpToPixel(float dp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float convertPixelsToDp(float px, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float convertSpToPixel(float sp, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static float convertPixelsToSp(float px, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.scaledDensity;
    }

    public static void showPositiveSnackbar(Context context, View view, int duration, String message){
        final Snackbar snackbar = Snackbar.make(view, message, duration * 1000);

        snackbar.setAction(context.getString(R.string.ok_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(context.getResources().getColor(R.color.colorHeader));

        snackbar.show();
    }

    public static void showErroredSnackbar(Context context, View view, int duration, String message){
        final Snackbar snackbar = Snackbar.make(view, message, duration * 1000);

        snackbar.setAction(context.getString(R.string.dismiss_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(context.getResources().getColor(R.color.brightRedText));

        snackbar.show();
    }
}