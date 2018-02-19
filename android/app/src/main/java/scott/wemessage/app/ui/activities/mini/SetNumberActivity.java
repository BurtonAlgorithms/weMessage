package scott.wemessage.app.ui.activities.mini;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.UUID;

import scott.wemessage.R;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.SettingsActivity;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class SetNumberActivity extends BaseActivity {

    private static final String BUNDLE_PHONE_NUMBER = "bundlePhoneNumber";

    private int oldEditTextColor;
    private int errorSnackbarDuration = 5000;
    private boolean isLaunchedFromSettings = false;
    private boolean isEditNumberFromSettings = false;

    private ScrollView setNumberMainLayout;
    private ConstraintLayout setNumberConstraintLayout;
    private EditText setNumberEditText;
    private Button continueButton, cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_number);

        setNumberMainLayout = findViewById(R.id.setNumberScrollView);
        setNumberConstraintLayout = findViewById(R.id.setNumberConstraintLayout);
        setNumberEditText = findViewById(R.id.setNumberEditText);
        continueButton = findViewById(R.id.continueButton);
        cancelButton = findViewById(R.id.cancelButton);
        oldEditTextColor = setNumberEditText.getCurrentTextColor();

        if (savedInstanceState != null) {
            isLaunchedFromSettings = savedInstanceState.getBoolean(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS);
            isEditNumberFromSettings = savedInstanceState.getBoolean(weMessage.BUNDLE_EDIT_NUMBER_FROM_SETTINGS);
            setNumberEditText.setText(savedInstanceState.getString(BUNDLE_PHONE_NUMBER));
        }else {
            isLaunchedFromSettings = getIntent().getBooleanExtra(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, false);
            isEditNumberFromSettings = getIntent().getBooleanExtra(weMessage.BUNDLE_EDIT_NUMBER_FROM_SETTINGS, false);
        }

        setNumberConstraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!(v instanceof EditText)) {
                    clearEditTexts();
                }
                return true;
            }
        });

        setNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    resetEditText(setNumberEditText);
                }
            }
        });

        setNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    clearEditText(setNumberEditText, true);
                }
                return false;
            }
        });

        continueButton.setOnClickListener(new OnClickWaitListener(750L) {
            @Override
            public void onWaitClick(View v) {
                clearEditTexts();

                String phoneNumber = setNumberEditText.getText().toString();

                if (StringUtils.isEmpty(phoneNumber)) {
                    invalidateField(setNumberEditText);
                    generateInvalidSnackBar(setNumberMainLayout, getString(R.string.no_phone)).show();
                    return;
                }

                if (!PhoneNumberUtil.getInstance().isPossibleNumber(phoneNumber, Resources.getSystem().getConfiguration().locale.getCountry())){
                    invalidateField(setNumberEditText);
                    generateInvalidSnackBar(setNumberMainLayout, getString(R.string.phone_number_invalid)).show();
                    return;
                }

                resetEditText(setNumberEditText);

                float currentTextSize = DisplayUtils.convertPixelsToSp(continueButton.getTextSize(), SetNumberActivity.this);
                float finalTextSize = DisplayUtils.convertPixelsToSp(continueButton.getTextSize(), SetNumberActivity.this) + 7;

                int currentTextColor = getResources().getColor(R.color.heavyBlue);
                int finalTextColor = getResources().getColor(R.color.superHeavyBlue);

                startTextSizeAnimation(continueButton, 0L, 150L, currentTextSize, finalTextSize);
                startTextColorAnimation(continueButton, 0L, 150L, currentTextColor, finalTextColor);

                startTextSizeAnimation(continueButton, 150L, 150L, finalTextSize, currentTextSize);
                startTextColorAnimation(continueButton, 150L, 150L, finalTextColor, currentTextColor);

                Handle handle = weMessage.get().getMessageDatabase().getHandleByHandleID(phoneNumber);

                if (handle == null){
                    handle = new Handle(UUID.randomUUID(), phoneNumber, Handle.HandleType.ME, false, false);
                }

                weMessage.get().getSharedPreferences().edit().putString(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER, phoneNumber).apply();
                weMessage.get().getMessageManager().addHandle(handle, false);
                weMessage.get().getCurrentSession().setSmsHandle(handle);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isLaunchedFromSettings){
                            goToSettings();
                        }else {
                            LaunchActivity.launchActivity(SetNumberActivity.this, null, false);
                        }
                    }
                }, 250L);
            }
        });

        if (isEditNumberFromSettings){
            ((TextView) findViewById(R.id.setNumberText)).setText(getString(R.string.phone_number_not_detected_settings));
            if (StringUtils.isEmpty(setNumberEditText.getText().toString())) setNumberEditText.setText(weMessage.get().getSharedPreferences().getString(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER, ""));
        }else {
            ((TextView) findViewById(R.id.setNumberText)).setText(getString(R.string.phone_number_not_detected));
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditNumberFromSettings) {
                    goToSettings();
                }else {
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isEditNumberFromSettings){
            goToSettings();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, isLaunchedFromSettings);
        outState.putBoolean(weMessage.BUNDLE_EDIT_NUMBER_FROM_SETTINGS, isEditNumberFromSettings);
        outState.putString(BUNDLE_PHONE_NUMBER, setNumberEditText.getText().toString());
    }

    private void invalidateField(final EditText editText){
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.colorHeader), getResources().getColor(R.color.invalidRed));
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                editText.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                editText.setTextColor((int) animation.getAnimatedValue());
            }
        });

        Animation invalidShake = AnimationUtils.loadAnimation(this, R.anim.invalid_shake);
        invalidShake.setInterpolator(new CycleInterpolator(7F));

        colorAnimation.start();
        editText.startAnimation(invalidShake);
    }

    private void resetEditText( EditText editText){
        editText.getBackground().setColorFilter(getResources().getColor(R.color.colorHeader), PorterDuff.Mode.SRC_ATOP);
        editText.setTextColor(oldEditTextColor);
    }

    private void clearEditTexts() {
        closeKeyboard();
        clearEditText(setNumberEditText, false);
    }

    private void clearEditText(final EditText editText, boolean closeKeyboard){
        if (closeKeyboard) {
            closeKeyboard();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.clearFocus();
            }
        }, 100);
    }

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void goToSettings(){
        Intent launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);

        startActivity(launcherIntent);
        finish();
    }

    private void startTextSizeAnimation(final TextView view, long startDelay, long duration, float startSize, float endSize){
        ValueAnimator textSizeAnimator = ValueAnimator.ofFloat(startSize, endSize);
        textSizeAnimator.setDuration(duration);
        textSizeAnimator.setStartDelay(startDelay);

        textSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setTextSize((float) valueAnimator.getAnimatedValue());
            }
        });
        textSizeAnimator.start();
    }

    private void startTextColorAnimation(final TextView view, long startDelay, long duration, int startColor, int endColor){
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(duration);
        colorAnimation.setStartDelay(startDelay);

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTextColor((int) animation.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private Snackbar generateInvalidSnackBar(View view, String message){
        final Snackbar snackbar = Snackbar.make(view, message, errorSnackbarDuration);

        snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.brightRedText));

        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);

        return snackbar;
    }

    public static DialogDisplayer.AlertDialogFragment generateAlertDialog(String title, String message){
        return DialogDisplayer.generateAlertDialog(title, message);
    }
}