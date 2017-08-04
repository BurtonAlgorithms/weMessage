package scott.wemessage.app.view.messages.content;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.utils.RoundedImageView;

import scott.wemessage.R;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.weMessage;

public class AttachmentImageView extends AttachmentView {

    private boolean isInit = false;
    private TextView time;
    private ImageView image;
    private View imageOverlay;

    private int defaultIncomingImageTimeTextColor = weMessage.get().getResources().getColor(R.color.warm_grey_four);
    private float defaultIncomingImageTimeTextSize = weMessage.get().getResources().getDimension(R.dimen.message_time_text_size);
    private int incomingDefaultImageOverlayPressedColor = weMessage.get().getResources().getColor(R.color.transparent);
    private int incomingDefaultImageOverlaySelectedColor = weMessage.get().getResources().getColor(R.color.cornflower_blue_light_40);

    private int outgoingImageTimeTextColor = weMessage.get().getResources().getColor(R.color.warm_grey_four);
    private float outgoingImageTimeTextSize = weMessage.get().getResources().getDimension(R.dimen.message_time_text_size);
    private int outgoingDefaultImageOverlayPressedColor = weMessage.get().getResources().getColor(R.color.transparent);
    private int outgoingDefaultImageOverlaySelectedColor = weMessage.get().getResources().getColor(R.color.cornflower_blue_light_40);

    public AttachmentImageView(Context context) {
        super(context);
    }

    public AttachmentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bind(Attachment attachment, MessageType messageType){
        init();

        if (messageType == MessageType.INCOMING){
            if (image != null && image instanceof RoundedImageView) {
                ((RoundedImageView) image).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0
                );
            }
            applyStyle(MessageType.INCOMING);
        } else if (messageType == MessageType.OUTGOING){
            if (image != null && image instanceof RoundedImageView) {
                ((RoundedImageView) image).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0,
                        R.dimen.message_bubble_corners_radius
                );
            }
            applyStyle(MessageType.OUTGOING);
        }

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(getContext()).load(url).into(imageView);
            }
        };

        if (image != null && attachment.getFileLocation() != null && attachment.getFileLocation().getFile() != null) {
            imageLoader.loadImage(image, AndroidIOUtils.getUriFromFile(attachment.getFileLocation().getFile()).toString());
        }

        if (imageOverlay != null) {
            imageOverlay.setSelected(isSelected());
        }
    }

    private void init(){
        if (!isInit) {
            time = (TextView) findViewById(R.id.messageTime);
            image = (ImageView) findViewById(R.id.attachmentImage);
            imageOverlay = findViewById(R.id.imageOverlay);
            isInit = true;
        }
    }

    private void applyStyle(MessageType messageType) {
        if (messageType == MessageType.INCOMING) {
            if (time != null) {
                time.setTextColor(defaultIncomingImageTimeTextColor);
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultIncomingImageTimeTextSize);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, getIncomingImageOverlayDrawable());
            }
        }else if (messageType == MessageType.OUTGOING){
            if (time != null) {
                time.setTextColor(outgoingImageTimeTextColor);
                time.setTextSize(TypedValue.COMPLEX_UNIT_PX, outgoingImageTimeTextSize);
            }

            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, getOutgoingImageOverlayDrawable());
            }
        }
    }

    private Drawable getIncomingImageOverlayDrawable() {
        return getMessageSelector(Color.TRANSPARENT, incomingDefaultImageOverlaySelectedColor, incomingDefaultImageOverlayPressedColor, R.drawable.shape_incoming_message);
    }

    private Drawable getOutgoingImageOverlayDrawable() {
        return getMessageSelector(Color.TRANSPARENT, outgoingDefaultImageOverlaySelectedColor, outgoingDefaultImageOverlayPressedColor, R.drawable.shape_outcoming_message);
    }
}