package scott.wemessage.app.launch;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.view.button.FontButton;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.AuthenticationUtils.PasswordValidateType;
import scott.wemessage.commons.utils.StringUtils;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class LaunchFragment extends Fragment {

    private final String LAUNCH_ALERT_DIALOG_TAG = "DialogLauncherAlert";

    private ConnectionService connectionService;
    private ConstraintLayout launchConstraintLayout;
    private EditText ipEditText, emailEditText, passwordEditText;
    private FontButton signInButton;
    private ProgressDialog loginProgressDialog;
    private int oldEditTextColor;
    private int errorSnackbarDuration = 5000;

    /**
     *
     * TODO: GET FOCUS OFF OF EDIT TEXT
     *
     *
     */

    private BroadcastReceiver launcherBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (loginProgressDialog != null){
                if(intent.getAction().equals(weMessage.INTENT_CONNECTION_SERVICE_STOPPED)){
                    unbindService();
                }else if (intent.getAction().equals(weMessage.INTENT_LOGIN_TIMEOUT)){
                    loginProgressDialog.dismiss();
                    generateAlertDialog(getString(R.string.timeout_alert_title), getString(R.string.timeout_alert_content)).show(getFragmentManager(), LAUNCH_ALERT_DIALOG_TAG);
                }else if(intent.getAction().equals(weMessage.INTENT_LOGIN_ERROR)){
                    loginProgressDialog.dismiss();
                    generateAlertDialog(getString(R.string.login_error_alert_title), getString(R.string.login_error_alert_content)).show(getFragmentManager(), LAUNCH_ALERT_DIALOG_TAG);
                }
            }
        }
    };

    private ServiceConnection connectionServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionService = null;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(weMessage.INTENT_LOGIN_TIMEOUT);
        intentFilter.addAction(weMessage.INTENT_LOGIN_ERROR);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(launcherBroadcastReceiver, intentFilter);

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
            ipEditText.setText(savedInstanceState.getString(weMessage.BUNDLE_HOST));
            emailEditText.setText(savedInstanceState.getString(weMessage.BUNDLE_EMAIL));
            passwordEditText.setText(savedInstanceState.getString(weMessage.BUNDLE_PASSWORD));
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

                String ipAddress;
                int port;

                if (ipUnformatted.contains(":")){
                    String[] split = ipUnformatted.split(":");

                    try {
                        port = Integer.parseInt(split[1]);
                        ipAddress = split[0];
                    }catch(Exception ex){
                        invalidateField(ipEditText);
                        generateInvalidSnackBar(view, getString(R.string.port_not_valid)).show();
                        return;
                    }
                }else {
                    ipAddress = ipUnformatted;
                    port = weMessage.DEFAULT_PORT;
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

                float currentTextSize = DisplayUtils.convertPixelsToSp(signInButton.getTextSize(), getActivity());
                float finalTextSize = DisplayUtils.convertPixelsToSp(signInButton.getTextSize(), getActivity()) + 7;

                int currentTextColor = getResources().getColor(R.color.heavyBlue);
                int finalTextColor = getResources().getColor(R.color.superHeavyBlue);

                startTextSizeAnimation(signInButton, 0L, 150L, currentTextSize, finalTextSize);
                startTextColorAnimation(signInButton, 0L, 150L, currentTextColor, finalTextColor);

                startTextSizeAnimation(signInButton, 150L, 150L, finalTextSize, currentTextSize);
                startTextColorAnimation(signInButton, 150L, 150L, finalTextColor, currentTextColor);

                Intent startServiceIntent = new Intent(getActivity(), ConnectionService.class);
                startServiceIntent.putExtra(weMessage.ARG_HOST, ipAddress);
                startServiceIntent.putExtra(weMessage.ARG_PORT, port);
                startServiceIntent.putExtra(weMessage.ARG_EMAIL, email);
                startServiceIntent.putExtra(weMessage.ARG_PASSWORD, password);

                getActivity().startService(startServiceIntent);
                bindService();

                loginProgressDialog = new ProgressDialog(getActivity());

                loginProgressDialog.setMessage(getString(R.string.connecting_dialog));
                loginProgressDialog.setCancelable(false);
                loginProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connectionService.endService();
                        dialog.dismiss();
                        generateInvalidSnackBar(view, getString(R.string.connection_cancelled)).show();
                    }
                });
                loginProgressDialog.show();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(weMessage.BUNDLE_HOST, ipEditText.getText().toString());
        outState.putString(weMessage.BUNDLE_EMAIL, emailEditText.getText().toString());
        outState.putString(weMessage.BUNDLE_PASSWORD, passwordEditText.getText().toString());
    }

    //TODO: Unbind service and stuff, if service has not yet connected destroy it?
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(launcherBroadcastReceiver);

        if (connectionService != null) {
            unbindService();
        }

        super.onDestroy();
    }

    private void bindService(){
        Intent intent = new Intent(getActivity(), ConnectionService.class);

        getActivity().bindService(intent, connectionServiceConnection, Context.BIND_IMPORTANT);
    }

    private void unbindService(){
        getActivity().unbindService(connectionServiceConnection);
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

        snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
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

    private DialogFragment generateAlertDialog(String title, String message){
        Bundle bundle = new Bundle();
        DialogFragment dialog = new AlertDialogFragment();

        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, title);
        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, message);
        dialog.setArguments(bundle);

        return dialog;
    }

    public static class AlertDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();

            String title = args.getString(weMessage.BUNDLE_ALERT_TITLE);
            String message = args.getString(weMessage.BUNDLE_ALERT_MESSAGE);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (title != null){
                builder.setTitle(title);
            }

            builder.setMessage(message);
            builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setIcon(R.drawable.ic_app_notification_white);

            return builder.create();
        }
    }
}