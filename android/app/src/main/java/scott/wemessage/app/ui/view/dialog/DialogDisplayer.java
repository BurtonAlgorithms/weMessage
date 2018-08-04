package scott.wemessage.app.ui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class DialogDisplayer {

    private static final String LAUNCH_ALERT_DIALOG_TAG = "DialogLauncherAlert";

    public static AlertDialogFragment generateAlertDialog(String title, String message){
        Bundle bundle = new Bundle();
        AlertDialogFragment dialog = new AlertDialogFragment();

        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, title);
        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, message);
        dialog.setArguments(bundle);

        return dialog;
    }

    public static AlertDialogFragmentDouble generateAlertDialogDouble(String title, String message, String positiveButton){
        Bundle bundle = new Bundle();
        AlertDialogFragmentDouble dialog = new AlertDialogFragmentDouble();

        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, title);
        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, message);
        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, positiveButton);
        dialog.setArguments(bundle);

        return dialog;
    }

    public static AlertDialogFragmentDouble generateOfflineDialog(Context context, String message){
        Bundle bundle = new Bundle();
        AlertDialogFragmentDouble dialog = new AlertDialogFragmentDouble();

        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, MmsManager.isDefaultSmsApp() ? context.getString(R.string.sms_mode) : context.getString(R.string.offline_mode));
        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, message);
        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.reconnect_button));
        dialog.setArguments(bundle);

        return dialog;
    }


    public static void showDisconnectReasonDialog(Context context, FragmentManager fragmentManager, Intent bundledIntent, String defaultMessage){
        String message = defaultMessage;

        if (bundledIntent.getExtras() != null){
            String alternateMessageExtra = bundledIntent.getExtras().getString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE);
            if (alternateMessageExtra != null){
                message = alternateMessageExtra;
            }
        }
        generateAlertDialog(context.getString(R.string.login_error_alert_title), message).show(fragmentManager, LAUNCH_ALERT_DIALOG_TAG);
    }

    public static void showDisconnectReasonDialogSingleButton(Context context, FragmentManager fragmentManager, Intent bundledIntent, String defaultMessage, Runnable runnable){
        String message = defaultMessage;

        if (bundledIntent.getExtras() != null){
            String alternateMessageExtra = bundledIntent.getExtras().getString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE);
            if (alternateMessageExtra != null){
                message = alternateMessageExtra;
            }
        }

        AlertDialogFragment alertDialogFragment = generateAlertDialog(context.getString(R.string.login_error_alert_title), message);

        alertDialogFragment.setOnDismiss(runnable);
        alertDialogFragment.show(fragmentManager, LAUNCH_ALERT_DIALOG_TAG);
    }

    public static void showDisconnectReasonDialog(Context context, FragmentManager fragmentManager, Intent bundledIntent, String defaultMessage, Runnable runnable){
        String message = defaultMessage;

        if (bundledIntent.getExtras() != null){
            String alternateMessageExtra = bundledIntent.getExtras().getString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE);
            if (alternateMessageExtra != null){
                message = alternateMessageExtra;
            }
        }

        Bundle bundle = new Bundle();
        AlertDialogFragmentDouble alertDialogFragmentDouble = new AlertDialogFragmentDouble();

        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.login_error_alert_title));
        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, message);
        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.reconnect_button));
        alertDialogFragmentDouble.setArguments(bundle);

        alertDialogFragmentDouble.setOnDismiss(runnable);
        alertDialogFragmentDouble.show(fragmentManager, LAUNCH_ALERT_DIALOG_TAG);
    }

    public static void showContactSyncResult(boolean success, Context context, FragmentManager fragmentManager){
        if (success){
            generateAlertDialog(context.getString(R.string.contact_sync_success), context.getString(R.string.contact_sync_success_message)).show(fragmentManager, "ContactSyncResultAlert");
        }else {
            generateAlertDialog(context.getString(R.string.contact_sync_fail), context.getString(R.string.contact_sync_fail_message)).show(fragmentManager, "ContactSyncResultAlert");
        }
    }

    public static void showNoAccountsFoundDialog(Context context, FragmentManager fragmentManager){
        AlertDialogFragment alertDialogFragment = generateAlertDialog(context.getString(R.string.login_error_alert_title), context.getString(R.string.no_accounts_found_server_warning));
        alertDialogFragment.setCancelableOnTouchedOutside(false);
        alertDialogFragment.show(fragmentManager, "NoAccountsFoundDialog");
    }

    public static void showContactSyncDialog(final Context context, final FragmentManager fragmentManager, final Runnable macRunnable, final Runnable androidRunnable){
        ContactSyncAlertDialog contactSyncAlertDialog = new ContactSyncAlertDialog();

        contactSyncAlertDialog.setSyncRunnables(
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_android_message));
                        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.start_process));
                        alertDialogFragmentDouble.setArguments(bundle);

                        alertDialogFragmentDouble.setOnDismiss(androidRunnable);
                        alertDialogFragmentDouble.show(fragmentManager, "ContactSyncDialogPhone");
                    }
                },

                new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_mac_message));
                        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.start_process));
                        alertDialogFragmentDouble.setArguments(bundle);

                        if (weMessage.get().getSharedPreferences().getBoolean(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW, true)) {
                            alertDialogFragmentDouble.setOnDismiss(new Runnable() {
                                @Override
                                public void run() {
                                    if (context == null || fragmentManager == null) return;

                                    weMessage.get().getSharedPreferences().edit().putBoolean(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW, false).apply();

                                    Bundle bundle = new Bundle();
                                    DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                                    bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                                    bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_message_permission));
                                    bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.ok_button));
                                    alertDialogFragmentDouble.setArguments(bundle);

                                    alertDialogFragmentDouble.setOnDismiss(macRunnable);
                                    alertDialogFragmentDouble.show(fragmentManager,  "ContactSyncDialogComputerPermission");
                                }
                            });
                        }else {
                            alertDialogFragmentDouble.setOnDismiss(macRunnable);
                        }
                        alertDialogFragmentDouble.show(fragmentManager, "ContactSyncDialogComputer");
                    }
                });
        contactSyncAlertDialog.show(fragmentManager, "ContactSyncDialog");
    }

    public static class AlertDialogFragment extends DialogFragment {

        private Runnable runnable;
        private boolean isCancelableOnTouchOutside = true;
        private boolean backButtonDisabled = false;
        private boolean linkify = false;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();

            String title = args.getString(weMessage.BUNDLE_ALERT_TITLE);
            String message = args.getString(weMessage.BUNDLE_ALERT_MESSAGE);
            String positiveButton;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialogLayout alertDialogLayout = (AlertDialogLayout) getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

            alertDialogLayout.setTitle(title);
            alertDialogLayout.setMessage(message);

            if (linkify){
                alertDialogLayout.linkify();
            }

            builder.setView(alertDialogLayout);

            if (!StringUtils.isEmpty(args.getString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON))){
                positiveButton = args.getString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON);
            }else {
                positiveButton = getString(R.string.ok_button);
            }

            builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            setRetainInstance(linkify || isCancelableOnTouchOutside || runnable != null);

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(isCancelableOnTouchOutside);

            if (backButtonDisabled){
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) return false;
                        return true;
                    }
                });
            }

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
            if (runnable != null) {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded()) {
                    runnable.run();
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

        public void linkify(boolean value){
            linkify = value;
        }

        public void setOnDismiss(Runnable runnable){
            this.runnable = runnable;
        }

        public void setCancelableOnTouchedOutside(boolean cancelableOnTouchedOutside){
            isCancelableOnTouchOutside = cancelableOnTouchedOutside;
        }

        public void disableBackPress(boolean disabled){
            backButtonDisabled = disabled;
        }
    }

    public static class AlertDialogFragmentDouble extends DialogFragment {
        private boolean runRunnable = false;
        private Runnable runnable;
        private Runnable denyRunnable;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();

            String title = args.getString(weMessage.BUNDLE_ALERT_TITLE);
            String message = args.getString(weMessage.BUNDLE_ALERT_MESSAGE);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialogLayout alertDialogLayout = (AlertDialogLayout) getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

            alertDialogLayout.setTitle(title);
            alertDialogLayout.setMessage(message);

            builder.setView(alertDialogLayout);
            builder.setPositiveButton(args.getString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runRunnable = true;
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.dismiss_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runRunnable = false;
                    dialog.dismiss();
                }
            });

            setRetainInstance(denyRunnable != null || runnable != null);

            return builder.create();
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
            if (runRunnable && runnable != null) {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded()) {
                    new Handler().postDelayed(runnable, 100L);
                }
            }else {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded() && denyRunnable != null) {
                    new Handler().postDelayed(denyRunnable, 100L);
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

        public void setOnDismiss(Runnable runnable){
            this.runnable = runnable;
        }

        public void setDenyRunnable(Runnable runnable){
            this.denyRunnable = runnable;
        }
    }

    public static class ContactSyncAlertDialog extends DialogFragment {
        private int syncMode = -1;
        private Runnable androidRunnable;
        private Runnable macRunnable;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialogLayout alertDialogLayout = (AlertDialogLayout) getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

            alertDialogLayout.setTitle(getString(R.string.sync_contacts_title));
            alertDialogLayout.setMessage(getString(R.string.sync_contacts_message));

            builder.setView(alertDialogLayout);
            builder.setPositiveButton(getString(R.string.sync_with_mac), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    syncMode = 1;
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton(getString(R.string.sync_with_phone), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    syncMode = 2;
                    dialog.dismiss();
                }
            });

            setRetainInstance(true);

            return builder.create();
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
            if (syncMode != -1) {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded()) {
                    if (syncMode == 1){
                        new Handler().postDelayed(macRunnable, 100L);
                    }else if (syncMode == 2){
                        new Handler().postDelayed(androidRunnable, 100L);
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

        public void setSyncRunnables(Runnable androidSync, Runnable macSync){
            this.androidRunnable = androidSync;
            this.macRunnable = macSync;
        }
    }
}