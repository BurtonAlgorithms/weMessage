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

package scott.wemessage.app.ui.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.activities.mini.SetDefaultSmsActivity;
import scott.wemessage.app.ui.activities.mini.SetNumberActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class SettingsActivity extends BaseActivity {

    private boolean isBoundToConnectionService = false;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private ViewGroup settingsGoToServer;
    private ViewGroup settingsToggleSmsMode;
    private ViewGroup settingsEditNumber;
    private boolean selfDisconnect;

    private BroadcastReceiver settingsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED)){
                unbindService();
                toggleConnectToServer(true);
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(SettingsActivity.this, null, false);
                    }
                }, true);
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(SettingsActivity.this, null, false);
                    }
                }, true);
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(SettingsActivity.this, null, false);
                    }
                }, true);
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                if (selfDisconnect) return;

                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(SettingsActivity.this, null, false);
                    }
                }, false);
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                showErroredSnackbar(getString(R.string.send_message_error), 5);
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (intent.getExtras() != null){
                    showErroredSnackbar(intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE), 5);
                }else {
                    showErroredSnackbar(getString(R.string.action_perform_error_default), 5);
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                showErroredSnackbar(getString(R.string.result_process_error), 5);
            }else if(intent.getAction().equals(weMessage.BROADCAST_LOGIN_SUCCESSFUL)){
                toggleConnectToServer(false);
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_FAILED)){
                DialogDisplayer.showContactSyncResult(false, SettingsActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS)){
                DialogDisplayer.showContactSyncResult(true, SettingsActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION)){
                DialogDisplayer.showNoAccountsFoundDialog(SettingsActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_SMS_MODE_ENABLED)){
                showToggleSmsMode(false);
            }else if(intent.getAction().equals(weMessage.BROADCAST_SMS_MODE_DISABLED)){
                showToggleSmsMode(true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

        IntentFilter broadcastIntentFilter = new IntentFilter();

        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_FORCED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SEND_MESSAGE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_ACTION_PERFORM_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_RESULT_PROCESS_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_LOGIN_SUCCESSFUL);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_FAILED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SMS_MODE_ENABLED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SMS_MODE_DISABLED);

        LocalBroadcastManager.getInstance(this).registerReceiver(settingsBroadcastReceiver, broadcastIntentFilter);

        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        ImageButton homeButton = toolbar.findViewById(R.id.settingsHomeButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatList();
            }
        });
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        settingsEditNumber = findViewById(R.id.settingsEditNumber);
        settingsGoToServer = findViewById(R.id.settingsConnectToServer);
        settingsToggleSmsMode = findViewById(R.id.settingsToggleSms);
        ViewGroup settingsAbout = findViewById(R.id.settingsAbout);
        ViewGroup settingsContacts = findViewById(R.id.settingsContacts);
        ViewGroup settingsSwitchAccounts = findViewById(R.id.settingsSwitchAccounts);
        ViewGroup settingsSignInOut = findViewById(R.id.settingsSignInOut);

        ((TextView) findViewById(R.id.settingsVersionText)).setText(getString(R.string.settings_version, weMessage.WEMESSAGE_VERSION));

        settingsEditNumber.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                Intent launchIntent = new Intent(weMessage.get(), SetNumberActivity.class);
                launchIntent.putExtra(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, true);
                launchIntent.putExtra(weMessage.BUNDLE_EDIT_NUMBER_FROM_SETTINGS, true);

                startActivity(launchIntent);
                finish();
            }
        });

        settingsGoToServer.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                LaunchActivity.launchActivity(SettingsActivity.this, null, true);
            }
        });

        settingsToggleSmsMode.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                Intent launchIntent = new Intent(weMessage.get(), SetDefaultSmsActivity.class);
                launchIntent.putExtra(weMessage.BUNDLE_SET_SMS_FROM_SETTINGS, true);

                startActivity(launchIntent);
                finish();
            }
        });

        settingsAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = new Intent(weMessage.get(), AboutActivity.class);

                startActivity(launchIntent);
                finish();
            }
        });

        settingsContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(weMessage.get(), ContactListActivity.class);

                startActivity(launchIntent);
                finish();
            }
        });

        settingsSwitchAccounts.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                Intent launcherIntent = new Intent(weMessage.get(), ContactSelectActivity.class);
                launcherIntent.putExtra(weMessage.BUNDLE_SWITCH_ACCOUNTS_MODE, true);

                startActivity(launcherIntent);
                finish();
            }
        });

        settingsSignInOut.setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View view) {
                selfDisconnect = true;

                if (isServiceRunning(ConnectionService.class)) {
                    serviceConnection.getConnectionService().getConnectionHandler().disconnect();
                }

                weMessage.get().signOut(true);
                LaunchActivity.launchActivity(SettingsActivity.this, null, false);
            }
        });

        showEditNumber();
        showToggleSmsMode(!MmsManager.isDefaultSmsApp());

        if (isServiceRunning(ConnectionService.class)){
            serviceConnection.scheduleTask(new Runnable() {
                @Override
                public void run() {
                    toggleConnectToServer(!serviceConnection.getConnectionService().getConnectionHandler().isConnected().get());
                }
            });
        }else {
            toggleConnectToServer(true);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        goToChatList();
    }

    private void bindService(){
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
        isBoundToConnectionService = true;
    }

    private void unbindService(){
        if (isBoundToConnectionService) {
            unbindService(serviceConnection);
            isBoundToConnectionService = false;
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable, boolean doubleButtonView){
        if (doubleButtonView) {
            DialogDisplayer.showDisconnectReasonDialog(this, getSupportFragmentManager(), bundledIntent, defaultMessage, runnable);
        }else {
            DialogDisplayer.showDisconnectReasonDialogSingleButton(this, getSupportFragmentManager(), bundledIntent, defaultMessage, runnable);
        }
    }

    private void showErroredSnackbar(String message, int duration){
        if (!isFinishing() && !isDestroyed()) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.settingsLayout), message, duration * 1000);

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

    private void goToChatList(){
        if (!isFinishing() && !isDestroyed()) {
            Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

            startActivity(returnIntent);
            finish();
        }
    }

    private void toggleConnectToServer(boolean offline){
        if (settingsGoToServer != null)
            settingsGoToServer.setVisibility(offline ? View.VISIBLE : View.GONE);
    }

    private void showEditNumber(){
        if (settingsEditNumber != null)
            settingsEditNumber.setVisibility(MmsManager.isDefaultSmsApp() && !StringUtils.isEmpty(weMessage.get().getSharedPreferences().getString(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER, "")) ? View.VISIBLE : View.GONE);
    }

    private void showToggleSmsMode(boolean isNotSms){
        if (settingsToggleSmsMode != null)
            settingsToggleSmsMode.setVisibility(isNotSms && MmsManager.isPhone() ? View.VISIBLE : View.GONE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}