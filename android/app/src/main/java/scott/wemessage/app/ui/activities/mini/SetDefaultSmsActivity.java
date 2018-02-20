package scott.wemessage.app.ui.activities.mini;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.SettingsActivity;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.view.dialog.AlertDialogLayout;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class SetDefaultSmsActivity extends BaseActivity {

    private boolean isSmsChooserOpen = false;
    private boolean isSettingsIntentOpen = false;
    private boolean isPermissionOnlyMode = false;
    private boolean isLaunchedFromSettings = false;

    private Button continueButton, cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_default_sms);

        continueButton = findViewById(R.id.continueButton);
        cancelButton = findViewById(R.id.cancelButton);

        if (getIntent().getBooleanExtra(weMessage.BUNDLE_SET_SMS_PERMISSION_ERROR, false)){
            isPermissionOnlyMode = true;

            findViewById(R.id.setSmsText).setVisibility(View.GONE);
            continueButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);

            showFixPermissionsDialog();
            return;
        }

        if (MmsManager.isDefaultSmsApp()){
            weMessage.get().getSharedPreferences().edit().putBoolean(weMessage.SHARED_PREFERENCES_PROMPT_FOR_SMS, false).apply();
            returnToLastScreen();
            return;
        }

        isLaunchedFromSettings = getIntent().getBooleanExtra(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, false);

        continueButton.setOnClickListener(new OnClickWaitListener(750L) {
            @Override
            public void onWaitClick(View v) {
                float currentTextSize = DisplayUtils.convertPixelsToSp(continueButton.getTextSize(), SetDefaultSmsActivity.this);
                float finalTextSize = DisplayUtils.convertPixelsToSp(continueButton.getTextSize(), SetDefaultSmsActivity.this) + 7;

                int currentTextColor = getResources().getColor(R.color.heavyBlue);
                int finalTextColor = getResources().getColor(R.color.superHeavyBlue);

                startTextSizeAnimation(continueButton, 0L, 150L, currentTextSize, finalTextSize);
                startTextColorAnimation(continueButton, 0L, 150L, currentTextColor, finalTextColor);

                startTextSizeAnimation(continueButton, 150L, 150L, finalTextSize, currentTextSize);
                startTextColorAnimation(continueButton, 150L, 150L, finalTextColor, currentTextColor);

                setDefaultSmsApp();
            }
        });

        cancelButton.setOnClickListener(new OnClickWaitListener(750L) {
            @Override
            public void onWaitClick(View v) {
                weMessage.get().getSharedPreferences().edit().putBoolean(weMessage.SHARED_PREFERENCES_PROMPT_FOR_SMS, false).apply();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnToLastScreen();
                    }
                }, 250L);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isSettingsIntentOpen) {
            isSettingsIntentOpen = false;

            if (settingsNoAccess()) {
                if (isPermissionOnlyMode){
                    DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_is_default_deny_permission));
                    alertDialogFragment.setOnDismiss(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    alertDialogFragment.setCancelableOnTouchedOutside(false);
                    alertDialogFragment.disableBackPress(true);
                    alertDialogFragment.show(getSupportFragmentManager(), "SmsAppNotChosenFailedAlert");
                }else {
                    DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_permission_failed)).show(getSupportFragmentManager(), "SmsPermissionsFailedAlert");
                }
            }else {
                if (isPermissionOnlyMode){
                    returnToLastScreen();
                }else {
                    setDefaultSmsApp();
                }
            }
        }else if (isSmsChooserOpen){
            isSmsChooserOpen = false;

            if (MmsManager.isDefaultSmsApp()){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnToLastScreen();
                    }
                }, 250L);
            }else {
                DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_not_chosen)).show(getSupportFragmentManager(), "SmsAppNotChosenAlert");
            }
        }
    }

    @Override
    public void onBackPressed() { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case weMessage.REQUEST_PERMISSION_SMS:
                if (isGranted(grantResults)){
                    if (isPermissionOnlyMode){
                        if (settingsNoAccess()){
                            setSettings();
                        }else {
                            returnToLastScreen();
                        }
                    }else {
                        setDefaultSmsApp();
                    }
                }else {
                    if (isPermissionOnlyMode) {
                        DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_is_default_deny_permission));
                        alertDialogFragment.setOnDismiss(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                        alertDialogFragment.setCancelableOnTouchedOutside(false);
                        alertDialogFragment.disableBackPress(true);
                        alertDialogFragment.show(getSupportFragmentManager(), "SmsAppNotChosenFailedAlert");
                    } else {
                        DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_permission_failed)).show(getSupportFragmentManager(), "SmsPermissionsFailedAlert");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setDefaultSmsApp(){
        if (!MmsManager.hasSmsPermissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            final List<String> neededPermissions = new ArrayList<>();
            boolean rationaleShown = false;

            String[] permissions = new String[] {
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.RECEIVE_MMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE
            };

            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(permission);
                }
            }

            if (!isPermissionOnlyMode) {
                for (String s : neededPermissions) {
                    if (shouldShowRequestPermissionRationale(s)) {
                        DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_rationale));

                        alertDialogFragment.setOnDismiss(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    requestPermissions(neededPermissions.toArray(new String[neededPermissions.size()]), weMessage.REQUEST_PERMISSION_SMS);
                            }
                        });
                        alertDialogFragment.show(getSupportFragmentManager(), "SmsPermissionsRationaleAlert");
                        rationaleShown = true;
                        break;
                    }
                }
            }

            if (!rationaleShown){
                requestPermissions(neededPermissions.toArray(new String[neededPermissions.size()]), weMessage.REQUEST_PERMISSION_SMS);
            }
        } else if (settingsNoAccess()) {
            setSettings();
        } else {
            if (!isPermissionOnlyMode) {
                isSmsChooserOpen = true;

                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            }
        }
    }

    private void setSettings(){
        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragment = DialogDisplayer.generateAlertDialogDouble(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_write_settings), getString(R.string.ok_button));

        alertDialogFragment.setOnDismiss(new Runnable() {
            @Override
            public void run() {
                isSettingsIntentOpen = true;

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        alertDialogFragment.setDenyRunnable(new Runnable() {
            @Override
            public void run() {
               DialogDisplayer.AlertDialogFragment alertDialogDeny = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.set_default_sms_rationale));

               if (isPermissionOnlyMode){
                   alertDialogDeny.setOnDismiss(new Runnable() {
                       @Override
                       public void run() {
                           finish();
                       }
                   });
                   alertDialogDeny.setCancelableOnTouchedOutside(false);
               }

               alertDialogDeny.show(getSupportFragmentManager(), "SmsPermissionsWriteSettingsRationale");
            }
        });
        alertDialogFragment.show(getSupportFragmentManager(), "SmsPermissionsWriteSettingsAlert");
    }

    private void showFixPermissionsDialog(){
        SmsFixPermissionsDialog fixPermissionsDialog = new SmsFixPermissionsDialog();
        fixPermissionsDialog.setRunnables(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    setDefaultSmsApp();
                }
            });
        fixPermissionsDialog.show(getSupportFragmentManager(), "SmsFixPermissionsAlertDialog");
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

    private boolean isGranted(int[] grantResults){
        if (grantResults.length > 0){
            for (int i = 0; i < grantResults.length; i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) return false;
            }
            return true;
        }

        return false;
    }

    private void returnToLastScreen(){
        if (isLaunchedFromSettings){
            Intent launcherIntent;

            if (MmsManager.isDefaultSmsApp() && StringUtils.isEmpty(MmsManager.getPhoneNumber())){
                launcherIntent = new Intent(weMessage.get(), SetNumberActivity.class);
                launcherIntent.putExtra(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, true);
            }else {
                launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);
            }

            startActivity(launcherIntent);
            finish();
        }else {
            LaunchActivity.launchActivity(SetDefaultSmsActivity.this, null, false);
        }
    }

    public static boolean settingsNoAccess(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(weMessage.get());
    }

    public static class SmsFixPermissionsDialog extends DialogFragment {
        private int mode = -1;
        private Runnable cancelRunnable;
        private Runnable permissionRunnable;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialogLayout alertDialogLayout = (AlertDialogLayout) getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

            alertDialogLayout.setTitle(getString(R.string.permissions_error_title));
            alertDialogLayout.setMessage(getString(R.string.set_default_sms_is_default_no_permissions));

            builder.setView(alertDialogLayout);
            builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mode = 1;
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(getString(R.string.quit_app), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mode = 2;
                    dialog.dismiss();
                }
            });

            setRetainInstance(true);

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) return false;
                    return true;
                }
            });

            return dialog;
        }

        @Override
        public void onDestroyView() {
            Dialog dialog = getDialog();

            if (dialog != null && getRetainInstance()) {
                dialog.setDismissMessage(null);
            }
            super.onDestroyView();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mode != -1) {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded()) {
                    if (mode == 1){
                        new Handler().postDelayed(permissionRunnable, 100L);
                    }else if (mode == 2){
                        new Handler().postDelayed(cancelRunnable, 100L);
                    }
                }
            }

            super.onDismiss(dialog);
        }

        @Override
        public void show(FragmentManager manager, String tag) {
            try {
                super.show(manager, tag);
            }catch(Exception ex){
                AppLogger.log(AppLogger.Level.ERROR, null, "Attempted to show a dialog when display was exited.");
            }
        }

        public void setRunnables(Runnable cancelRunnable, Runnable permissionRunnable){
            this.cancelRunnable = cancelRunnable;
            this.permissionRunnable = permissionRunnable;
        }
    }
}