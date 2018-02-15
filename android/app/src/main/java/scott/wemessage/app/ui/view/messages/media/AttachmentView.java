package scott.wemessage.app.ui.view.messages.media;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import scott.wemessage.R;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.ui.ConversationFragment;
import scott.wemessage.app.ui.view.messages.MessageView;
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

    public abstract void bind(MessageView messageView, Attachment attachment, MessageType messageType, boolean isErrored);

    public void setBottomPadding(int paddingDp){
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
        layoutParams.bottomMargin = DisplayUtils.convertDpToRoundedPixel(paddingDp, getContext());

        setLayoutParams(layoutParams);
    }

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

    protected ConversationFragment getParentFragment(){
        return ((ConversationFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.conversationFragmentContainer));
    }

    private AppCompatActivity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    public enum MessageType {
        INCOMING,
        OUTGOING
    }
}