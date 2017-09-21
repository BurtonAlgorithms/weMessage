package scott.wemessage.app.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import scott.wemessage.R;
import scott.wemessage.app.weMessage;

public class MessageImageActivity extends AppCompatActivity {

    private TextView doneButton;
    private ImageView imageView;

    private String imageUri;
    private String previousChatId;
    private boolean isCollapsed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.message_image_full_screen);

        if (savedInstanceState == null) {
            isCollapsed = false;
            imageUri = getIntent().getStringExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI);
            previousChatId = getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        } else {
            isCollapsed = savedInstanceState.getBoolean("isCollapsed");
            imageUri = savedInstanceState.getString("fullScreenImageUri");
            previousChatId = savedInstanceState.getString("previousChatId");
        }

        doneButton = (TextView) findViewById(R.id.doneButton);
        imageView = (ImageView) findViewById(R.id.messageFullScreenImageView);

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

        imageView.setSoundEffectsEnabled(false);
        imageView.setOnClickListener(new View.OnClickListener() {
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

        Glide.with(this).load(imageUri).into(imageView);
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
    public void onBackPressed() {
        returnToConversationScreen();
    }

    private void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        finish();
    }
}