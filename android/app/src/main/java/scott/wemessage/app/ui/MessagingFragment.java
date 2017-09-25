package scott.wemessage.app.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import scott.wemessage.R;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.types.ActionType;
import scott.wemessage.commons.types.ReturnType;

public abstract class MessagingFragment extends Fragment {

    private final int ERROR_SNACKBAR_DURATION = 5;

    protected void showActionFailureSnackbar(JSONAction jsonAction, ReturnType returnType){
        ActionType actionType = ActionType.fromCode(jsonAction.getActionType());

        if (actionType == null){
            showErroredSnackbar(getString(R.string.action_failure_generic));
            return;
        }

        switch (actionType){
            case CREATE_GROUP:
                switch (returnType){
                    case UNKNOWN_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_unknown));
                        break;
                    case INVALID_NUMBER:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_invalid_number));
                        break;
                    case NUMBER_NOT_IMESSAGE:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_number_not_imessage));
                        break;
                    case ASSISTIVE_ACCESS_DISABLED:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_assistive_access));
                        break;
                    case UI_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_ui_error));
                        break;
                    default:
                        showErroredSnackbar(getString(R.string.action_failure_create_group_unknown));
                        break;
                }
                break;
            case RENAME_GROUP:
                switch (returnType){
                    case UNKNOWN_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_rename_group_unknown));
                        break;
                    case GROUP_CHAT_NOT_FOUND:
                        showErroredSnackbar(getString(R.string.action_failure_rename_group_not_found));
                        break;
                    case ASSISTIVE_ACCESS_DISABLED:
                        showErroredSnackbar(getString(R.string.action_failure_rename_group_assistive_access));
                        break;
                    case UI_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_rename_group_ui_error));
                        break;
                    default:
                        showErroredSnackbar(getString(R.string.action_failure_rename_group_unknown));
                        break;
                }
                break;
            case ADD_PARTICIPANT:
                switch (returnType){
                    case UNKNOWN_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_unknown));
                        break;
                    case INVALID_NUMBER:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_invalid_number));
                        break;
                    case NUMBER_NOT_IMESSAGE:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_number_not_imessage));
                        break;
                    case GROUP_CHAT_NOT_FOUND:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_group_not_found));
                        break;
                    case ASSISTIVE_ACCESS_DISABLED:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_assistive_access));
                        break;
                    case UI_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_ui_error));
                        break;
                    default:
                        showErroredSnackbar(getString(R.string.action_failure_add_participant_unknown));
                        break;
                }
                break;
            case REMOVE_PARTICIPANT:
                switch (returnType){
                    case UNKNOWN_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_unknown));
                        break;
                    case INVALID_NUMBER:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_invalid_number));
                        break;
                    case GROUP_CHAT_NOT_FOUND:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_group_not_found));
                        break;
                    case ASSISTIVE_ACCESS_DISABLED:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_assistive_access));
                        break;
                    case UI_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_ui_error));
                        break;
                    default:
                        showErroredSnackbar(getString(R.string.action_failure_remove_participant_unknown));
                        break;
                }
                break;
            case LEAVE_GROUP:
                switch (returnType){
                    case UNKNOWN_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_leave_chat_unknown));
                        break;
                    case GROUP_CHAT_NOT_FOUND:
                        showErroredSnackbar(getString(R.string.action_failure_leave_chat_group_not_found));
                        break;
                    case ASSISTIVE_ACCESS_DISABLED:
                        showErroredSnackbar(getString(R.string.action_failure_leave_chat_assistive_access));
                        break;
                    case UI_ERROR:
                        showErroredSnackbar(getString(R.string.action_failure_leave_chat_ui_error));
                        break;
                    default:
                        showErroredSnackbar(getString(R.string.action_failure_leave_chat_unknown));
                        break;
                }
                break;
            default:
                showErroredSnackbar(getString(R.string.action_failure_generic));
                break;
        }
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void showErroredSnackbar(String message){
        if (getView() != null) {
            final Snackbar snackbar = Snackbar.make(getView(), message, ERROR_SNACKBAR_DURATION * 1000);

            snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.brightRedText));
            snackbar.show();
        }
    }
}