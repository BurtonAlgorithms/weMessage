package scott.wemessage.app.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import scott.wemessage.R;
import scott.wemessage.app.weMessage;

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

    public static void showDisconnectReasonDialog(Context context, FragmentManager fragmentManager, Intent bundledIntent, String defaultMessage, Runnable runnable){
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

    public static class AlertDialogFragment extends DialogFragment {

        private Runnable runnable;

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
            builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (runnable != null) {
                runnable.run();
            }

            super.onDismiss(dialog);
        }

        public void setOnDismiss(Runnable runnable){
            this.runnable = runnable;
        }
    }
}