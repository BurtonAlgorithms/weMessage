package scott.wemessage.app.ui.view.messages.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.percent.PercentFrameLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.utils.RoundedImageView;

import scott.wemessage.R;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;

public class AttachmentImageView extends AttachmentView {

    private boolean isInit = false;

    private PercentFrameLayout attachmentAnimatedImageLayout;
    private FrameLayout attachmentAnimatedImageContainer;
    private ImageView animatedImage;

    private PercentFrameLayout attachmentImageLayout;
    private ImageView attachmentImage;
    private ImageView errorBubble;

    private int incomingDefaultImageOverlayPressedColor = getResources().getColor(R.color.transparent);
    private int incomingDefaultImageOverlaySelectedColor = getResources().getColor(R.color.cornflower_blue_light_40);

    private int outgoingDefaultImageOverlayPressedColor = getResources().getColor(R.color.transparent);
    private int outgoingDefaultImageOverlaySelectedColor = getResources().getColor(R.color.cornflower_blue_light_40);

    public AttachmentImageView(Context context) {
        super(context);
    }

    public AttachmentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bind(MessageView messageView, Attachment attachment, final MessageType messageType, final boolean isErrored){
        init();

        final String attachmentUuid = attachment.getUuid().toString();
        int orientation = getResources().getConfiguration().orientation;
        MimeType.MimeExtension mimeExtension = MimeType.MimeExtension.getExtensionFromString(attachment.getFileType());

        final ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(getContext()).load(url).into(imageView);
            }
        };

        if (mimeExtension == MimeType.MimeExtension.GIF) {
            View imageOverlay = findViewById(R.id.attachmentAnimatedImageOverlay);

            if (orientation == Configuration.ORIENTATION_PORTRAIT){
                PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentAnimatedImageContainer.getLayoutParams();
                layoutParams.getPercentLayoutInfo().widthPercent = 0.9f;
                attachmentAnimatedImageContainer.setLayoutParams(layoutParams);
            } else {
                PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentAnimatedImageContainer.getLayoutParams();
                layoutParams.getPercentLayoutInfo().widthPercent = 0.7f;
                attachmentAnimatedImageContainer.setLayoutParams(layoutParams);
            }

            new AsyncTask<Attachment, Void, AttachmentBitmapResult>(){
                @Override
                protected AttachmentBitmapResult doInBackground(Attachment... params) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(params[0].getFileLocation().getFileLocation(), options);

                    AttachmentBitmapResult attachmentBitmapResult = new AttachmentBitmapResult();
                    attachmentBitmapResult.attachment = params[0];
                    attachmentBitmapResult.options = options;

                    return attachmentBitmapResult;
                }

                @Override
                protected void onPostExecute(AttachmentBitmapResult attachmentBitmapResult) {
                    if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                    PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentAnimatedImageContainer.getLayoutParams();
                    layoutParams.getPercentLayoutInfo().aspectRatio = (float) attachmentBitmapResult.options.outWidth / (float) attachmentBitmapResult.options.outHeight;
                    layoutParams.height = 0;

                    if (messageType == MessageType.INCOMING){
                        layoutParams.gravity = Gravity.START;
                    }else {
                        layoutParams.gravity = Gravity.END;
                    }

                    attachmentAnimatedImageContainer.setLayoutParams(layoutParams);

                    if (animatedImage != null && attachmentBitmapResult.attachment.getFileLocation() != null && attachmentBitmapResult.attachment.getFileLocation().getFile() != null) {
                        imageLoader.loadImage(animatedImage, IOUtils.getUriFromFile(attachmentBitmapResult.attachment.getFileLocation().getFile()).toString());
                    }

                    if (isErrored && messageType == MessageType.OUTGOING){
                        errorBubble.setVisibility(VISIBLE);
                    }else {
                        errorBubble.setVisibility(GONE);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);

            if (messageType == MessageType.INCOMING) {
                ((ImageView) findViewById(R.id.attachmentAnimatedImageMask)).setImageDrawable(getContext().getDrawable(R.drawable.attachment_image_incoming_frame));

                applyStyle(MessageType.INCOMING, imageOverlay);
            } else if (messageType == MessageType.OUTGOING) {
                ((ImageView) findViewById(R.id.attachmentAnimatedImageMask)).setImageDrawable(getContext().getDrawable(R.drawable.attachment_image_outgoing_frame));

                applyStyle(MessageType.OUTGOING, imageOverlay);
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }

            attachmentAnimatedImageLayout.setVisibility(VISIBLE);
            attachmentImageLayout.setVisibility(GONE);

            final String uri = IOUtils.getUriFromFile(attachment.getFileLocation().getFile()).toString();

            animatedImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchFullScreenImageActivity(uri);
                }
            });

            animatedImage.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    getParentFragment().showAttachmentOptionsSheet(attachmentUuid);
                    return true;
                }
            });
        } else {
            View imageOverlay = findViewById(R.id.attachmentImageOverlay);

            if (orientation == Configuration.ORIENTATION_PORTRAIT){
                PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentImage.getLayoutParams();
                layoutParams.getPercentLayoutInfo().widthPercent = 0.9f;
                attachmentImage.setLayoutParams(layoutParams);
            } else {
                PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentImage.getLayoutParams();
                layoutParams.getPercentLayoutInfo().widthPercent = 0.7f;
                attachmentImage.setLayoutParams(layoutParams);
            }

            new AsyncTask<Attachment, Void, AttachmentBitmapResult>(){

                @Override
                protected AttachmentBitmapResult doInBackground(Attachment... params) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(params[0].getFileLocation().getFileLocation(), options);

                    AttachmentBitmapResult attachmentBitmapResult = new AttachmentBitmapResult();
                    attachmentBitmapResult.attachment = params[0];
                    attachmentBitmapResult.options = options;

                    return attachmentBitmapResult;
                }

                @Override
                protected void onPostExecute(AttachmentBitmapResult attachmentBitmapResult) {
                    if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                    PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentImage.getLayoutParams();
                    layoutParams.getPercentLayoutInfo().aspectRatio = (float) attachmentBitmapResult.options.outWidth / (float) attachmentBitmapResult.options.outHeight;
                    layoutParams.height = 0;

                    if (messageType == MessageType.INCOMING){
                        layoutParams.gravity = Gravity.START;
                    }else {
                        layoutParams.gravity = Gravity.END;
                    }

                    attachmentImage.setLayoutParams(layoutParams);

                    if (attachmentImage != null && attachmentBitmapResult.attachment.getFileLocation() != null && attachmentBitmapResult.attachment.getFileLocation().getFile() != null) {
                        imageLoader.loadImage(attachmentImage, IOUtils.getUriFromFile(attachmentBitmapResult.attachment.getFileLocation().getFile()).toString());
                    }

                    if (isErrored && messageType == MessageType.OUTGOING){
                        errorBubble.setVisibility(VISIBLE);
                    }else {
                        errorBubble.setVisibility(GONE);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);

            if (messageType == MessageType.INCOMING) {
                if (attachmentImage != null && attachmentImage instanceof RoundedImageView) {
                    ((RoundedImageView) attachmentImage).setCorners(
                            R.dimen.message_bubble_corners_radius,
                            R.dimen.message_bubble_corners_radius,
                            R.dimen.message_bubble_corners_radius,
                            0
                    );
                }
                applyStyle(MessageType.INCOMING, imageOverlay);
            } else if (messageType == MessageType.OUTGOING) {
                if (attachmentImage != null && attachmentImage instanceof RoundedImageView) {
                    ((RoundedImageView) attachmentImage).setCorners(
                            R.dimen.message_bubble_corners_radius,
                            R.dimen.message_bubble_corners_radius,
                            0,
                            R.dimen.message_bubble_corners_radius
                    );
                }
                applyStyle(MessageType.OUTGOING, imageOverlay);
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }

            final String uri = IOUtils.getUriFromFile(attachment.getFileLocation().getFile()).toString();

            attachmentImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchFullScreenImageActivity(uri);
                }
            });

            attachmentImage.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    getParentFragment().showAttachmentOptionsSheet(attachmentUuid);
                    return true;
                }
            });

            attachmentImageLayout.setVisibility(VISIBLE);
            attachmentAnimatedImageLayout.setVisibility(GONE);
        }
    }

    private void init(){
        if (!isInit) {
            attachmentImageLayout = findViewById(R.id.attachmentImageLayout);
            attachmentImage = findViewById(R.id.attachmentImage);
            errorBubble = findViewById(R.id.errorBubble);

            attachmentAnimatedImageLayout = findViewById(R.id.attachmentAnimatedImageLayout);
            attachmentAnimatedImageContainer = findViewById(R.id.attachmentAnimatedImageContainer);
            animatedImage = findViewById(R.id.attachmentAnimatedImage);

            isInit = true;
        }
    }

    private void applyStyle(MessageType messageType, View imageOverlay) {
        if (messageType == MessageType.INCOMING) {
            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, getIncomingImageOverlayDrawable());
            }
        }else if (messageType == MessageType.OUTGOING){
            if (imageOverlay != null) {
                ViewCompat.setBackground(imageOverlay, getOutgoingImageOverlayDrawable());
            }
        }
    }

    private void launchFullScreenImageActivity(String imageUri){
        Intent broadcastIntent = new Intent(weMessage.BROADCAST_IMAGE_FULLSCREEN_ACTIVITY_START);

        broadcastIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI, imageUri);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
    }

    private Drawable getIncomingImageOverlayDrawable() {
        return getMessageSelector(Color.TRANSPARENT, incomingDefaultImageOverlaySelectedColor, incomingDefaultImageOverlayPressedColor, R.drawable.shape_incoming_message);
    }

    private Drawable getOutgoingImageOverlayDrawable() {
        return getMessageSelector(Color.TRANSPARENT, outgoingDefaultImageOverlaySelectedColor, outgoingDefaultImageOverlayPressedColor, R.drawable.shape_outcoming_message);
    }

    private class AttachmentBitmapResult {
        Attachment attachment;
        BitmapFactory.Options options;
    }
}