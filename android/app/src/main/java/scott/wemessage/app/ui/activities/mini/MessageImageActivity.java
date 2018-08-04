package scott.wemessage.app.ui.activities.mini;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import scott.wemessage.R;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.ConversationActivity;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.media.MediaDownloadCallbacks;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;

public class MessageImageActivity extends BaseActivity implements MediaDownloadCallbacks {

    private TextView doneButton;
    private PhotoView photoView;
    private ImageButton downloadButton;

    private boolean isCollapsed;
    private boolean isTaskRunning = false;
    private String imageUri;
    private String previousChatId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_message_image);

        if (savedInstanceState == null) {
            isCollapsed = false;
            imageUri = getIntent().getStringExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI);
            previousChatId = getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        } else {
            isCollapsed = savedInstanceState.getBoolean("isCollapsed");
            imageUri = savedInstanceState.getString("fullScreenImageUri");
            previousChatId = savedInstanceState.getString("previousChatId");
        }

        doneButton = findViewById(R.id.doneButton);
        downloadButton = findViewById(R.id.downloadImageButton);
        photoView = findViewById(R.id.messageFullScreenImageView);

        if (isCollapsed){
            View doneBar = findViewById(R.id.doneBar);
            int doneBarHeight = doneBar.getHeight();

            doneBar.setY(-doneBarHeight);
            doneBar.setVisibility(View.GONE);

            findViewById(R.id.messageImageParentView).setBackgroundColor(getResources().getColor(R.color.black));
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToConversationScreen();
            }
        });

        photoView.setSoundEffectsEnabled(false);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View doneBar = findViewById(R.id.doneBar);
                int doneBarHeight = doneBar.getHeight();

                if (!isCollapsed){
                    isCollapsed = true;

                    doneBar.animate().alpha(0.f).translationY(-doneBarHeight).setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            doneBar.setVisibility(View.GONE);
                        }
                    });

                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(android.R.color.white), getResources().getColor(android.R.color.black));
                    colorAnimation.setDuration(250);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            findViewById(R.id.messageImageParentView).setBackgroundColor((int) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();
                } else {
                    isCollapsed = false;

                    doneBar.animate().alpha(1.0f).translationY(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            doneBar.setVisibility(View.VISIBLE);
                        }
                    });

                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(android.R.color.black), getResources().getColor(android.R.color.white));
                    colorAnimation.setDuration(250);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            findViewById(R.id.messageImageParentView).setBackgroundColor((int) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                }
            }
        });

        downloadButton.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                saveToGallery();
            }
        });

        Glide.with(this).load(imageUri).into(photoView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isCollapsed", isCollapsed);
        outState.putString("fullScreenImageUri", imageUri);
        outState.putString("previousChatId", previousChatId);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

    private void saveToGallery(){
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.no_media_write_permission), "WritePermissionAlertFragment", weMessage.REQUEST_PERMISSION_WRITE_STORAGE)) return;

        IOUtils.saveMediaToGallery(this, this, findViewById(R.id.messageImageParentView), MimeType.IMAGE, imageUri);
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