package scott.wemessage.app.view.dialog;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.VideoView;

import scott.wemessage.R;

public class AnimationDialogLayout extends LinearLayout {

    public AnimationDialogLayout(Context context){
        super(context);
    }

    public AnimationDialogLayout(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public AnimationDialogLayout(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public void setAnimationSource(int videoFile){
        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + videoFile);
        VideoView videoView = (VideoView) findViewById(R.id.animationDialogVideoView);
        videoView.setVideoURI(uri);
    }

    public VideoView getVideoView(){
        return (VideoView) findViewById(R.id.animationDialogVideoView);
    }

    public void startAnimation(){
        VideoView videoView = (VideoView) findViewById(R.id.animationDialogVideoView);
        videoView.seekTo(0);
        videoView.start();
    }
}