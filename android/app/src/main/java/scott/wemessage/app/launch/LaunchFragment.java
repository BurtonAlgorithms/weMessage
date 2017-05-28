package scott.wemessage.app.launch;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import scott.wemessage.R;
import scott.wemessage.app.view.button.FontButton;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.AuthenticationUtils.PasswordValidateType;
import scott.wemessage.commons.utils.StringUtils;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class LaunchFragment extends Fragment {

    private ConstraintLayout launchConstraintLayout;
    private EditText ipEditText, emailEditText, passwordEditText;
    private FontButton signInButton;
    private int oldEditTextColor;
    private int errorSnackbarDuration = 5000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_launch, container, false);

        launchConstraintLayout = (ConstraintLayout) view.findViewById(R.id.launchConstraintLayout);
        ipEditText = (EditText) view.findViewById(R.id.launchIpEditText);
        emailEditText = (EditText) view.findViewById(R.id.launchEmailEditText);
        passwordEditText = (EditText) view.findViewById(R.id.launchPasswordEditText);
        signInButton = (FontButton) view.findViewById(R.id.signInButton);
        oldEditTextColor = emailEditText.getCurrentTextColor();

        if (savedInstanceState != null){
            ipEditText.setText(savedInstanceState.getString("ip"));
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }

        launchConstraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!(v instanceof EditText)){
                    clearEditTexts();
                }
                return true;
            }
        });

        ipEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    resetEditText(ipEditText);
                }
            }
        });

        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    resetEditText(emailEditText);
                }
            }
        });

        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    resetEditText(passwordEditText);
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEditTexts();

                String ipUnformatted = ipEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (StringUtils.isEmpty(ipUnformatted)){
                    invalidateField(ipEditText);
                    generateInvalidSnackBar(view, getString(R.string.no_ip)).show();
                    return;
                }

                if (StringUtils.isEmpty(email)){
                    invalidateField(emailEditText);
                    generateInvalidSnackBar(view, getString(R.string.no_email)).show();
                    return;
                }

                if (!AuthenticationUtils.isValidEmailFormat(email)){
                    invalidateField(emailEditText);
                    generateInvalidSnackBar(view, getString(R.string.invalid_email_format)).show();
                    return;
                }

                if (StringUtils.isEmpty(password)){
                    invalidateField(passwordEditText);
                    generateInvalidSnackBar(view, getString(R.string.no_password)).show();
                    return;
                }

                PasswordValidateType validateType = AuthenticationUtils.isValidPasswordFormat(password);

                if (validateType == PasswordValidateType.LENGTH_TOO_SMALL){
                    invalidateField(passwordEditText);
                    generateInvalidSnackBar(view, getString(R.string.password_too_short, weMessage.MINIMUM_PASSWORD_LENGTH)).show();
                    return;
                }

                if (validateType == PasswordValidateType.PASSWORD_TOO_EASY){
                    invalidateField(passwordEditText);
                    generateInvalidSnackBar(view, getString(R.string.password_too_easy)).show();
                    return;
                }

                resetEditText(ipEditText);
                resetEditText(emailEditText);
                resetEditText(passwordEditText);

                //TODO: work on button animation click, fade in font and color
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("ip", ipEditText.getText().toString());
        outState.putString("email", emailEditText.getText().toString());
        outState.putString("password", passwordEditText.getText().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        Animation invalidShake = AnimationUtils.loadAnimation(getActivity(), R.anim.invalid_shake);
        invalidShake.setInterpolator(new CycleInterpolator(7F));

        colorAnimation.start();
        editText.startAnimation(invalidShake);
    }

    private Snackbar generateInvalidSnackBar(View view, String message){
        final Snackbar snackbar = Snackbar.make(view, message, errorSnackbarDuration);

        snackbar.setAction(getString(R.string.dismissButton), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.lightRed));

        return snackbar;
    }

    private void resetEditText( EditText editText){
        editText.getBackground().setColorFilter(getResources().getColor(R.color.colorHeader), PorterDuff.Mode.SRC_ATOP);
        editText.setTextColor(oldEditTextColor);
    }

    private void clearEditTexts(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        if (getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
}