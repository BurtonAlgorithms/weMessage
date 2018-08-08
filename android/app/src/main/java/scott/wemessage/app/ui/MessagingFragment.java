/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.IConnectionBinder;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.types.ActionType;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;

public abstract class MessagingFragment extends Fragment {

    private final int ERROR_SNACKBAR_DURATION = 5;

    @Override
    public void onResume() {
        super.onResume();

        startConnectionServiceSilently();
    }

    protected void showMessageSendFailureSnackbar(ReturnType returnType){
        switch (returnType) {
            case INVALID_NUMBER:
                showErroredSnackbar(getString(R.string.message_delivery_failure_invalid_number));
                break;
            case NUMBER_NOT_IMESSAGE:
                showErroredSnackbar(getString(R.string.message_delivery_failure_imessage));
                break;
            case GROUP_CHAT_NOT_FOUND:
                showErroredSnackbar(getString(R.string.message_delivery_failure_group_chat));
                break;
            case SERVICE_NOT_AVAILABLE:
                showErroredSnackbar(getString(R.string.message_delivery_failure_service));
                break;
            case ASSISTIVE_ACCESS_DISABLED:
                showErroredSnackbar(getString(R.string.message_delivery_failure_assistive));
                break;
            case UI_ERROR:
                showErroredSnackbar(getString(R.string.message_delivery_failure_ui_error));
                break;
            default:
                showErroredSnackbar(getString(R.string.message_delivery_failure_unknown));
                break;
        }
    }

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

    protected void showAttachmentSendFailureSnackbar(FailReason reason){
        switch (reason){
            case MEMORY:
                showErroredSnackbar(getString(R.string.attachment_send_failure_size));
                break;
            default:
                showErroredSnackbar(getString(R.string.attachment_send_failure_generic));
                break;
        }
    }

    protected void showAttachmentReceiveFailureSnackbar(FailReason reason){
        switch (reason){
            case MEMORY:
                showErroredSnackbar(getString(R.string.attachment_receive_failure_size));
                break;
            default:
                showErroredSnackbar(getString(R.string.attachment_receive_failure_generic));
                break;
        }
    }

    protected boolean isConnectionServiceRunning(){
        return isServiceRunning(ConnectionService.class);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startConnectionServiceSilently(){
        if (!isConnectionServiceRunning() && weMessage.get().isSignedIn(true)){
            SharedPreferences sharedPreferences = weMessage.get().getSharedPreferences();
            Intent startServiceIntent = new Intent(getActivity(), ConnectionService.class);

            String host = sharedPreferences.getString(weMessage.SHARED_PREFERENCES_LAST_HOST, "");
            String ipAddress;
            int port;

            if (host.contains(":")) {
                String[] split = host.split(":");

                port = Integer.parseInt(split[1]);
                ipAddress = split[0];
            } else {
                ipAddress = host;
                port = weMessage.DEFAULT_PORT;
            }

            startServiceIntent.putExtra(weMessage.ARG_HOST, ipAddress);
            startServiceIntent.putExtra(weMessage.ARG_PORT, port);
            startServiceIntent.putExtra(weMessage.ARG_EMAIL, sharedPreferences.getString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, ""));
            startServiceIntent.putExtra(weMessage.ARG_PASSWORD, sharedPreferences.getString(weMessage.SHARED_PREFERENCES_LAST_HASHED_PASSWORD, ""));
            startServiceIntent.putExtra(weMessage.ARG_PASSWORD_ALREADY_HASHED, true);
            startServiceIntent.putExtra(weMessage.ARG_FAILOVER_IP, sharedPreferences.getString(weMessage.SHARED_PREFERENCES_LAST_FAILOVER_IP, ""));

            getActivity().startService(startServiceIntent);

            if (this instanceof IConnectionBinder) {
                ((IConnectionBinder) this).bindService();
            }
        }
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

            View snackbarView = snackbar.getView();
            TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);

            snackbar.show();
        }
    }
}