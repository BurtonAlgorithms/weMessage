package scott.wemessage.app.ui.view.messages.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.percent.PercentFrameLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.utils.RoundedImageView;

import scott.wemessage.R;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.weMessage;

public class AttachmentVideoView extends AttachmentView {

    private boolean isInit = false;

    private PercentFrameLayout attachmentVideoThumbnailLayout;
    private ImageView errorBubble;
    private ImageView attachmentVideoThumbnail;

    public AttachmentVideoView(Context context) {
        super(context);
    }

    public AttachmentVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bind(MessageView messageView, Attachment attachment, final MessageType messageType, final boolean isErrored){
        init();
        final String attachmentUuid = attachment.getUuid().toString();

        attachmentVideoThumbnailLayout.setAlpha(0.0f);

        new AsyncTask<Attachment, Void, ThumbnailBitmap>(){
            @Override
            protected ThumbnailBitmap doInBackground(Attachment... params) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                retriever.setDataSource(params[0].getFileLocation().getFileLocation());

                ThumbnailBitmap thumbnailBitmap = new ThumbnailBitmap();

                thumbnailBitmap.bitmap = ThumbnailUtils.createVideoThumbnail(params[0].getFileLocation().getFileLocation(),
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);


                thumbnailBitmap.width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                thumbnailBitmap.height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                retriever.release();

                return thumbnailBitmap;
            }

            @Override
            protected void onPostExecute(ThumbnailBitmap thumbnailBitmap) {
                if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) attachmentVideoThumbnail.getLayoutParams();
                layoutParams.getPercentLayoutInfo().aspectRatio = (float) thumbnailBitmap.width / (float) thumbnailBitmap.height;
                layoutParams.height = 0;

                if (messageType == MessageType.INCOMING){
                    layoutParams.gravity = Gravity.START;
                }else {
                    layoutParams.gravity = Gravity.END;
                }

                attachmentVideoThumbnail.setLayoutParams(layoutParams);
                attachmentVideoThumbnail.setImageBitmap(thumbnailBitmap.bitmap);
                attachmentVideoThumbnailLayout.animate().alpha(1.0f).setDuration(250);

                if (isErrored && messageType == MessageType.OUTGOING){
                    errorBubble.setVisibility(VISIBLE);
                }else {
                    errorBubble.setVisibility(GONE);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment);

        if (messageType == MessageType.INCOMING) {
            if (attachmentVideoThumbnail != null && attachmentVideoThumbnail instanceof RoundedImageView) {
                ((RoundedImageView) attachmentVideoThumbnail).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0
                );
            }
        } else if (messageType == MessageType.OUTGOING) {
            if (attachmentVideoThumbnail != null && attachmentVideoThumbnail instanceof RoundedImageView) {
                ((RoundedImageView) attachmentVideoThumbnail).setCorners(
                        R.dimen.message_bubble_corners_radius,
                        R.dimen.message_bubble_corners_radius,
                        0,
                        R.dimen.message_bubble_corners_radius
                );
            }
        }
        final String uri = IOUtils.getUriFromFile(attachment.getFileLocation().getFile()).toString();

        attachmentVideoThumbnailLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFullScreenVideoActivity(uri);
            }
        });

        attachmentVideoThumbnailLayout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getParentFragment().showAttachmentOptionsSheet(attachmentUuid);
                return true;
            }
        });
    }

    private void init(){
        if (!isInit) {
            attachmentVideoThumbnailLayout = findViewById(R.id.attachmentVideoThumbnailLayout);
            attachmentVideoThumbnail = (RoundedImageView) findViewById(R.id.attachmentVideoThumbnail);
            errorBubble = findViewById(R.id.errorBubble);
            isInit = true;
        }
    }

    private void launchFullScreenVideoActivity(String imageUri){
        Intent broadcastIntent = new Intent(weMessage.BROADCAST_VIDEO_FULLSCREEN_ACTIVITY_START);

        broadcastIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI, imageUri);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
    }

    private class ThumbnailBitmap {
        int height;
        int width;
        Bitmap bitmap;
    }
}