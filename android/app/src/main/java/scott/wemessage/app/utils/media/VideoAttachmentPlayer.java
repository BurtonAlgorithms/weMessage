package scott.wemessage.app.utils.media;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;

public class VideoAttachmentPlayer extends EasyVideoPlayer {

    private OnClickCallback clickCallback;

    public VideoAttachmentPlayer(Context context) {
        super(context);
    }

    public VideoAttachmentPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoAttachmentPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setClickCallback(OnClickCallback clickCallback){
        this.clickCallback = clickCallback;
    }

    @Override
    public void showControls() {
        super.showControls();

        if (clickCallback != null){
            clickCallback.onShowControls();
        }
    }

    @Override
    public void hideControls() {
        super.hideControls();

        if (clickCallback != null){
            clickCallback.onHideControls();
        }
    }

    public interface OnClickCallback {

        void onShowControls();

        void onHideControls();
    }
}