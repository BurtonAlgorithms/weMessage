package scott.wemessage.app.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.media.MediaDownloadCallbacks;
import scott.wemessage.app.utils.media.VideoAttachmentPlayer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;

public class MessageVideoActivity extends BaseActivity implements EasyVideoCallback, MediaDownloadCallbacks, VideoAttachmentPlayer.OnClickCallback {

    private VideoAttachmentPlayer videoPlayer;
    private TextView doneButton;
    private ImageButton downloadButton;

    private boolean isTaskRunning = false;
    private String videoUri;
    private String previousChatId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_video_full_screen);

        if (savedInstanceState == null) {
            videoUri = getIntent().getStringExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI);
            previousChatId = getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        } else {
            videoUri = savedInstanceState.getString("fullScreenVideoUri");
            previousChatId = savedInstanceState.getString("previousChatId");
        }

        doneButton = findViewById(R.id.doneButton);
        downloadButton = findViewById(R.id.downloadVideoButton);
        videoPlayer = findViewById(R.id.messageVideoPlayer);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToConversationScreen();
            }
        });

        downloadButton.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                saveToGallery();
            }
        });

        videoPlayer.setSource(Uri.parse(videoUri));
        videoPlayer.setCallback(this);
        videoPlayer.setClickCallback(this);
        videoPlayer.setAutoFullscreen(true);
        videoPlayer.setLeft(EasyVideoPlayer.LEFT_ACTION_RESTART);
        videoPlayer.setRight(EasyVideoPlayer.RIGHT_ACTION_NONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("fullScreenVideoUri", videoUri);
        outState.putString("previousChatId", previousChatId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        videoPlayer.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoPlayer.release();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case weMessage.REQUEST_PERMISSION_WRITE_STORAGE:
                if (isGranted(grantResults)){
                    saveToGallery();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        returnToConversationScreen();
    }

    @Override
    public boolean canMediaDownloadTaskStart(String attachmentUri) {
        return !isTaskRunning;
    }

    @Override
    public void onMediaDownloadTaskStart(String attachmentUri) {
        isTaskRunning = true;
    }

    @Override
    public void onMediaDownloadTaskFinish(String attachmentUri) {
        isTaskRunning = false;
    }

    @Override
    public void onStarted(EasyVideoPlayer player) { }

    @Override
    public void onPaused(EasyVideoPlayer player) { }

    @Override
    public void onPreparing(EasyVideoPlayer player) { }

    @Override
    public void onPrepared(EasyVideoPlayer player) { }

    @Override
    public void onBuffering(int percent) { }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.playback_error_title), getString(R.string.play_video_attachment_error));

        alertDialogFragment.setOnDismiss(new Runnable() {
            @Override
            public void run() {
                returnToConversationScreen();
            }
        });
        alertDialogFragment.show(getSupportFragmentManager(), "PlayBackErrorAlertDialog");
        AppLogger.error("An error occurred while trying to load a video.", e);
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) { }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) { }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) { }

    @Override
    public void onShowControls() {
        final View doneBar = findViewById(R.id.doneBar);

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        doneBar.animate().alpha(1.f).translationY(result).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                doneBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onHideControls() {
        final View doneBar = findViewById(R.id.doneBar);
        int doneBarHeight = doneBar.getHeight();

        doneBar.animate().alpha(0.f).translationY(-doneBarHeight).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                doneBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveToGallery(){
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.no_media_write_permission), "WritePermissionAlertFragment", weMessage.REQUEST_PERMISSION_WRITE_STORAGE)) return;
        if (videoPlayer.isControlsShown()) videoPlayer.hideControls();

        IOUtils.saveMediaToGallery(MessageVideoActivity.this, MessageVideoActivity.this, findViewById(R.id.messageVideoParentView), MimeType.VIDEO, videoUri);
    }

    private void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        finish();
    }

    private boolean hasPermission(final String permission, String rationaleString, String alertTagId, final int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)){
                DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), rationaleString);

                alertDialogFragment.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    }
                });
                alertDialogFragment.show(getSupportFragmentManager(), alertTagId);
                return false;
            } else {
                requestPermissions(new String[] { permission }, requestCode);
                return false;
            }
        }
        return true;
    }

    private boolean isGranted(int[] grantResults){
        return (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }
}