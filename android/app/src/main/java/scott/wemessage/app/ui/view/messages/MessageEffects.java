package scott.wemessage.app.ui.view.messages;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import scott.wemessage.R;
import scott.wemessage.app.utils.view.DisplayUtils;

public final class MessageEffects {

    private MessageEffects(){ }

    static void toggleInvisibleInk(final WebView invisibleInkView, final ViewGroup bubble, TextView text, TextView time){
        bubble.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        text.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
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

    static void performGentle(final Activity activity, final ViewGroup bubbleView, final TextView text, final TextView time){
        bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        float initialTextSize = DisplayUtils.convertPixelsToSp(text.getTextSize(), activity);
        final int initialHeight = bubbleView.getMeasuredHeight();
        final int initialWidth = bubbleView.getMeasuredWidth();
        final int finalHeight = (int) Math.round(initialHeight * 1.1);
        final int finalWidth = (int) Math.round(initialWidth * 1.2);

        text.setTextSize(0);
        time.setAlpha(0.0f);

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
                ValueAnimator timeAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
                timeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        time.setAlpha((Float) valueAnimator.getAnimatedValue());
                    }
                });
                timeAnimation.setDuration(500);
                timeAnimation.start();
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
                widthAnimation.setDuration(2000);
                widthAnimation.start();
            }
        }, 500L);
    }

    static void performLoud(final Activity activity, final ViewGroup bubbleView, final TextView text, final TextView time){
        bubbleView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final float initialTextSize = DisplayUtils.convertPixelsToSp(text.getTextSize(), activity);
        final float initialTimeSize = DisplayUtils.convertPixelsToSp(time.getTextSize(), activity);
        final int initialHeight = bubbleView.getMeasuredHeight();
        final int initialWidth = bubbleView.getMeasuredWidth();
        final int finalHeight = (int) Math.round(initialHeight * 1.8);
        final int finalWidth = (int) Math.round(initialWidth * 1.9);

        text.setTextSize(0);
        time.setTextSize((initialTimeSize / 2));

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

        ValueAnimator timeAnimation = ValueAnimator.ofFloat((initialTimeSize / 2), initialTimeSize * 2.0f);
        timeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                time.setTextSize((Float) valueAnimator.getAnimatedValue());
            }
        });
        timeAnimation.setDuration(1000);
        timeAnimation.start();

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

                ValueAnimator timeAnimation = ValueAnimator.ofFloat(initialTimeSize * 2.0f, initialTimeSize);
                timeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        time.setTextSize((Float) valueAnimator.getAnimatedValue());
                    }
                });
                timeAnimation.setDuration(750);
                timeAnimation.start();
            }
        }, 1000L);
    }

    static void performConfetti(final Context context, final Toolbar toolbar, final RelativeLayout animationLayout, final ViewGroup conversationLayout){
        final KonfettiView konfettiView = new KonfettiView(context);
        konfettiView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        toolbar.setVisibility(View.GONE);
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
                if (context == null) return;

                animationLayout.setVisibility(View.GONE);
                animationLayout.setAlpha(0.0f);
                animationLayout.removeView(konfettiView);
            }
        }, 5500L);
    }
}