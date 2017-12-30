package scott.wemessage.app.ui.view.messages;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.RawRes;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.util.concurrent.ConcurrentHashMap;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.utils.view.DisplayUtils;

public final class MessageEffects {

    private MessageEffects(){ }

    static void toggleInvisibleInk(final WebView invisibleInkView, final ViewGroup bubble, TextView text, LinearLayout replay){
        bubble.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        text.setVisibility(View.GONE);
        replay.setVisibility(View.GONE);
        invisibleInkView.setVisibility(View.VISIBLE);

        String invisibleInk = "<html><head><style type='text/css' rel='stylesheet'> html { margin: 0; padding: 0em; }</style></head><body><span class='ink'>" + text.getText() + "</span> <script src='jquery.js' type='text/javascript'></script> <script src='invisibleink.js' type='text/javascript'></script></body></html>";

        invisibleInkView.loadDataWithBaseURL("file:///android_asset/js/", invisibleInk, "text/html", "UTF-8", null);
        invisibleInkView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                invisibleInkView.loadUrl("javascript:weMessage.resizeInvisibleInk(document.body.getBoundingClientRect().height)");
                super.onPageFinished(view, url);
            }
        });
    }

    static void performGentle(final AnimationCallbacks animationCallbacks, final Message message, final Activity activity, final ViewGroup bubbleView, final TextView text, final LinearLayout replay){
        if (animationCallbacks.getAnimatedMessages().containsKey(message.getUuid().toString())) return;

        animationCallbacks.getAnimatedMessages().put(message.getUuid().toString(), message);
        bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        float initialTextSize = DisplayUtils.convertPixelsToSp(text.getTextSize(), activity);
        final int initialHeight = bubbleView.getMeasuredHeight();
        final int initialWidth = bubbleView.getMeasuredWidth();
        final int finalHeight = (int) Math.round(initialHeight * 1.1);
        final int finalWidth = (int) Math.round(initialWidth * 1.2);

        text.setTextSize(0);
        replay.setAlpha(0.0f);

        ValueAnimator heightAnimation = ValueAnimator.ofInt(0, finalHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                layoutParams.height = val;
                bubbleView.setLayoutParams(layoutParams);
            }
        });
        heightAnimation.setDuration(500);
        heightAnimation.start();

        ValueAnimator widthAnimation = ValueAnimator.ofInt(0, finalWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                layoutParams.width = val;
                bubbleView.setLayoutParams(layoutParams);
            }
        });
        widthAnimation.setDuration(500);
        widthAnimation.start();

        ValueAnimator textAnimation = ValueAnimator.ofFloat(0, initialTextSize);
        textAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                text.setTextSize((Float) valueAnimator.getAnimatedValue());
            }
        });
        textAnimation.setDuration(1500);
        textAnimation.start();

        textAnimation.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                ValueAnimator replayAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
                replayAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        replay.setAlpha((Float) valueAnimator.getAnimatedValue());
                    }
                });
                replayAnimation.setDuration(500);
                replayAnimation.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity == null) return;

                ValueAnimator heightAnimation = ValueAnimator.ofInt(finalHeight, initialHeight);
                heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                        layoutParams.height = val;
                        bubbleView.setLayoutParams(layoutParams);
                    }
                });
                heightAnimation.setDuration(2000);
                heightAnimation.start();

                ValueAnimator widthAnimation = ValueAnimator.ofInt(finalWidth, initialWidth);
                widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                        layoutParams.width = val;
                        bubbleView.setLayoutParams(layoutParams);
                    }
                });

                widthAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (activity == null || animationCallbacks == null) return;

                        animationCallbacks.getAnimatedMessages().remove(message.getUuid().toString());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) { }

                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
                widthAnimation.setDuration(2000);
                widthAnimation.start();
            }
        }, 500L);
    }

    static void performLoud(final AnimationCallbacks animationCallbacks, final Message message, final Activity activity, final ViewGroup bubbleView, final TextView text, final TextView replayText, final ImageView replayImageView){
        if (animationCallbacks.getAnimatedMessages().containsKey(message.getUuid().toString())) return;

        animationCallbacks.getAnimatedMessages().put(message.getUuid().toString(), message);
        bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final float initialTextSize = DisplayUtils.convertPixelsToSp(text.getTextSize(), activity);
        final float initialReplaySize = DisplayUtils.convertPixelsToSp(replayText.getTextSize(), activity);
        final float initialReplayImageSize = DisplayUtils.convertPixelsToDp(replayImageView.getWidth(), activity);
        final int initialHeight = bubbleView.getMeasuredHeight();
        final int initialWidth = bubbleView.getMeasuredWidth();
        final int finalHeight = (int) Math.round(initialHeight * 1.8);
        final int finalWidth = (int) Math.round(initialWidth * 1.9);

        text.setTextSize(0);
        replayText.setTextSize((initialReplaySize / 2));

        ValueAnimator heightAnimation = ValueAnimator.ofInt(0, finalHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                layoutParams.height = val;
                bubbleView.setLayoutParams(layoutParams);
            }
        });
        heightAnimation.setDuration(1000);
        heightAnimation.start();

        ValueAnimator widthAnimation = ValueAnimator.ofInt(0, finalWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                layoutParams.width = val;
                bubbleView.setLayoutParams(layoutParams);
            }
        });
        widthAnimation.setDuration(1000);
        widthAnimation.start();

        ValueAnimator textAnimation = ValueAnimator.ofFloat(0, initialTextSize * 2.0f);
        textAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                text.setTextSize((Float) valueAnimator.getAnimatedValue());
            }
        });
        textAnimation.setDuration(1000);
        textAnimation.start();

        ValueAnimator replayAnimation = ValueAnimator.ofFloat((initialReplaySize / 2), initialReplaySize * 2.0f);
        replayAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                replayText.setTextSize((Float) valueAnimator.getAnimatedValue());
            }
        });
        replayAnimation.setDuration(1000);
        replayAnimation.start();

        ValueAnimator replayImageAnimation = ValueAnimator.ofFloat((initialReplayImageSize / 2), initialReplayImageSize * 2.0f);
        replayImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams layoutParams = replayImageView.getLayoutParams();
                layoutParams.width = (int) DisplayUtils.convertDpToPixel(((Float) valueAnimator.getAnimatedValue()), activity);
                layoutParams.height = (int) DisplayUtils.convertDpToPixel(((Float) valueAnimator.getAnimatedValue()), activity);
                replayImageView.setLayoutParams(layoutParams);
            }
        });
        replayImageAnimation.setDuration(1000);
        replayImageAnimation.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation shake = AnimationUtils.loadAnimation(activity, R.anim.loud_effect);
                bubbleView.startAnimation(shake);
            }
        }, 250L);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity == null) return;

                ValueAnimator heightAnimation = ValueAnimator.ofInt(finalHeight, initialHeight);
                heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                        layoutParams.height = val;
                        bubbleView.setLayoutParams(layoutParams);
                    }
                });
                heightAnimation.setDuration(750);
                heightAnimation.start();

                ValueAnimator widthAnimation = ValueAnimator.ofInt(finalWidth, initialWidth);
                widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = bubbleView.getLayoutParams();
                        layoutParams.width = val;
                        bubbleView.setLayoutParams(layoutParams);
                    }
                });

                widthAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) { }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (activity == null || animationCallbacks == null) return;

                        animationCallbacks.getAnimatedMessages().remove(message.getUuid().toString());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) { }

                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
                widthAnimation.setDuration(750);
                widthAnimation.start();

                ValueAnimator textAnimation = ValueAnimator.ofFloat(initialTextSize * 2.0f, initialTextSize);
                textAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        text.setTextSize((Float) valueAnimator.getAnimatedValue());
                    }
                });
                textAnimation.setDuration(750);
                textAnimation.start();

                ValueAnimator replayAnimation = ValueAnimator.ofFloat(initialReplaySize * 2.0f, initialReplaySize);
                replayAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        replayText.setTextSize((Float) valueAnimator.getAnimatedValue());
                    }
                });
                replayAnimation.setDuration(750);
                replayAnimation.start();

                ValueAnimator replayImageAnimation = ValueAnimator.ofFloat(initialReplayImageSize * 2.0f, initialReplayImageSize);
                replayImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        ViewGroup.LayoutParams layoutParams = replayImageView.getLayoutParams();
                        layoutParams.width = (int) DisplayUtils.convertDpToPixel(((Float) valueAnimator.getAnimatedValue()), activity);
                        layoutParams.height = (int) DisplayUtils.convertDpToPixel(((Float) valueAnimator.getAnimatedValue()), activity);
                        replayImageView.setLayoutParams(layoutParams);
                    }
                });
                replayImageAnimation.setDuration(750);
                replayImageAnimation.start();
            }
        }, 1000L);
    }

    static void performConfetti(final AnimationCallbacks animationCallbacks, final Context context, final Toolbar toolbar, final RelativeLayout animationLayout, final ViewGroup conversationLayout){
        if (animationCallbacks.isGlobalEffectPlaying()) return;

        animationCallbacks.setGlobalEffectPlaying(true);
        toolbar.setVisibility(View.GONE);

        final KonfettiView konfettiView = new KonfettiView(context);
        konfettiView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        animationLayout.addView(konfettiView);
        animationLayout.setVisibility(View.VISIBLE);
        animationLayout.setAlpha(1.0f);
        conversationLayout.setAlpha(0.5f);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context == null) return;

                Resources res = context.getResources();

                konfettiView.build()
                        .addColors(res.getColor(R.color.konfetti_blue), res.getColor(R.color.konfetti_orange), res.getColor(R.color.konfetti_purple), res.getColor(R.color.konfetti_pink), res.getColor(R.color.konfetti_yellow))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(new Size(12, 5f), new Size(16, 6f))
                        .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, 50f)
                        .stream(30, 5000L);
            }
        }, 500L);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context == null) return;

                ValueAnimator konfettiAlpha = ValueAnimator.ofFloat(1.0f, 0.0f);
                konfettiAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        konfettiView.setAlpha((Float) valueAnimator.getAnimatedValue());
                    }
                });
                konfettiAlpha.setDuration(1000);
                konfettiAlpha.start();

                ValueAnimator conversationAlpha = ValueAnimator.ofFloat(0.5f, 1.0f);
                conversationAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        conversationLayout.setAlpha((Float) valueAnimator.getAnimatedValue());
                    }
                });
                conversationAlpha.setDuration(1000);
                conversationAlpha.start();

                toolbar.setAlpha(0.0f);
                toolbar.setVisibility(View.VISIBLE);

                ValueAnimator toolbarAlpha = ValueAnimator.ofFloat(0.0f, 1.0f);
                toolbarAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        toolbar.setAlpha((Float) valueAnimator.getAnimatedValue());
                    }
                });
                toolbarAlpha.setDuration(1000);
                toolbarAlpha.start();
            }
        }, 4000L);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context == null || animationCallbacks == null) return;

                animationLayout.setVisibility(View.GONE);
                animationLayout.setAlpha(0.0f);
                animationLayout.removeView(konfettiView);
                animationCallbacks.setGlobalEffectPlaying(false);
            }
        }, 5500L);
    }

    static void performFireworks(final AnimationCallbacks animationCallbacks, final Activity context, final Toolbar toolbar, final RelativeLayout animationLayout, final ViewGroup conversationLayout){
        performBaseVideoAnimation(R.raw.fireworks, ScalableType.CENTER_CROP, animationCallbacks, context, toolbar, animationLayout, conversationLayout);
    }

    static void performShootingStar(final AnimationCallbacks animationCallbacks, final Activity context, final Toolbar toolbar, final RelativeLayout animationLayout, final ViewGroup conversationLayout){
        performBaseVideoAnimation(R.raw.comet, ScalableType.CENTER_TOP_CROP, animationCallbacks, context, toolbar, animationLayout, conversationLayout);
    }

    private static void performBaseVideoAnimation(@RawRes int resId, final ScalableType scalableType, final AnimationCallbacks animationCallbacks, final Activity context, final Toolbar toolbar, final RelativeLayout animationLayout, final ViewGroup conversationLayout){
        if (animationCallbacks.isGlobalEffectPlaying()) return;

        animationCallbacks.setGlobalEffectPlaying(true);
        toolbar.setVisibility(View.GONE);

        final ScalableVideoView videoView = new ScalableVideoView(context);
        videoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        animationLayout.addView(videoView);
        animationLayout.setVisibility(View.VISIBLE);
        animationLayout.setAlpha(1.0f);
        conversationLayout.setAlpha(0.5f);

        try {
            videoView.setRawData(resId);
            videoView.prepare(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                    videoView.setScalableType(scalableType);
                    videoView.invalidate();
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoView.release();

                    if (context == null) return;

                    ValueAnimator textureViewAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
                    textureViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            videoView.setAlpha((Float) valueAnimator.getAnimatedValue());
                        }
                    });
                    textureViewAnimator.setDuration(250);
                    textureViewAnimator.start();

                    ValueAnimator conversationAlpha = ValueAnimator.ofFloat(0.5f, 1.0f);
                    conversationAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            conversationLayout.setAlpha((Float) valueAnimator.getAnimatedValue());
                        }
                    });
                    conversationAlpha.setDuration(250);
                    conversationAlpha.start();

                    toolbar.setAlpha(0.0f);
                    toolbar.setVisibility(View.VISIBLE);

                    ValueAnimator toolbarAlpha = ValueAnimator.ofFloat(0.0f, 1.0f);
                    toolbarAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            toolbar.setAlpha((Float) valueAnimator.getAnimatedValue());
                        }
                    });
                    toolbarAlpha.setDuration(250);
                    toolbarAlpha.start();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (context == null || animationCallbacks == null)
                                        return;

                                    animationLayout.setVisibility(View.GONE);
                                    animationLayout.setAlpha(0.0f);
                                    animationLayout.removeView(videoView);
                                    animationCallbacks.setGlobalEffectPlaying(false);
                                }
                            }, 250L);
                        }
                    });
                }
            });
        }catch (Exception ex){
            AppLogger.error("An error occurred while trying to play a video animation", ex);
        }
    }

    public interface AnimationCallbacks {
        ConcurrentHashMap<String, Message> getAnimatedMessages();

        boolean isGlobalEffectPlaying();

        void setGlobalEffectPlaying(boolean value);
    }
}