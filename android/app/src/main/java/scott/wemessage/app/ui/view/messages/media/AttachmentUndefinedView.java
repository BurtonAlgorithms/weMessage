package scott.wemessage.app.ui.view.messages.media;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.stfalcon.chatkit.utils.RoundedImageView;

import scott.wemessage.R;
import scott.wemessage.app.messages.models.Attachment;
import scott.wemessage.app.ui.view.font.FontTextView;
import scott.wemessage.app.ui.view.messages.MessageView;

public class AttachmentUndefinedView extends AttachmentView {

    private boolean isInit = false;

    private ImageView undefinedAttachmentImageView;
    private FontTextView undefinedAttachmentTextView;

    public AttachmentUndefinedView(Context context) {
        super(context);
    }

    public AttachmentUndefinedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentUndefinedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bind(MessageView messageView, Attachment attachment, MessageType messageType, boolean isErrored){
        init();

        if (messageType == MessageType.INCOMING) {
            if (undefinedAttachmentImageView != null && undefinedAttachmentImageView instanceof RoundedImageView) {
                ((RoundedImageView) undefinedAttachmentImageView).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0
                );
            }
        } else if (messageType == MessageType.OUTGOING) {
            if (undefinedAttachmentImageView != null && undefinedAttachmentImageView instanceof RoundedImageView) {
                ((RoundedImageView) undefinedAttachmentImageView).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }
    }

    public void setLost(boolean isLost){
        if (isLost){
            undefinedAttachmentTextView.setText(R.string.file_not_found);
        }
    }

    private void init(){
        if (!isInit) {
            undefinedAttachmentImageView = (RoundedImageView) findViewById(R.id.undefinedAttachmentImageView);
            undefinedAttachmentTextView = findViewById(R.id.undefinedAttachmentTextView);

            isInit = true;
        }
    }
}