package scott.wemessage.app.view.messages.content;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.utils.view.DisplayUtils;

public abstract class AttachmentView extends RelativeLayout {

    public AttachmentView(Context context) {
        super(context);
    }

    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void bind(Attachment attachment, MessageType messageType);

    public void setMargins(Integer topMargin, Integer bottomMargin, Integer startMargin, Integer endMargin){
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();

        if (startMargin != null) {
            layoutParams.setMarginStart(Math.round(DisplayUtils.convertDpToPixel(startMargin, getContext())));
        }
        if (endMargin != null) {
            layoutParams.setMarginEnd(Math.round(DisplayUtils.convertDpToPixel(endMargin, getContext())));
        }
        if (topMargin != null) {
            layoutParams.topMargin = Math.round(DisplayUtils.convertDpToPixel(topMargin, getContext()));
        }
        if (bottomMargin != null) {
            layoutParams.bottomMargin = Math.round(DisplayUtils.convertDpToPixel(bottomMargin, getContext()));
        }
    }

    /**
     *
     * These methods come from the ChatKit Library
     *
     * **/

    protected Drawable getMessageSelector(@ColorInt int normalColor, @ColorInt int selectedColor, @ColorInt int pressedColor, @DrawableRes int shape) {
        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{selectedColor, pressedColor, normalColor}
                ));
        return drawable;
    }

    protected Drawable getVectorDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(getContext(), drawable);
    }

    public enum MessageType {
        INCOMING,
        OUTGOING
    }
}