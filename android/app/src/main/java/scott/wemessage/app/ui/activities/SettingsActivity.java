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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.weMessage;

public class SettingsActivity extends AppCompatActivity {

    private boolean isBoundToConnectionService = false;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver settingsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED)){
                unbindService();
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
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

        LocalBroadcastManager.getInstance(this).registerReceiver(settingsBroadcastReceiver, broadcastIntentFilter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        ImageButton homeButton = (ImageButton) toolbar.findViewById(R.id.settingsHomeButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatList();
            }
        });

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        TextView settingsVersionText = (TextView) findViewById(R.id.settingsVersionText);
        ViewGroup settingsBlockedContacts = (ViewGroup) findViewById(R.id.settingsBlockedContacts);
        ViewGroup settingsSignInOut = (ViewGroup) findViewById(R.id.settingsSignInOut);

        settingsVersionText.setText(getString(R.string.settings_version, weMessage.WEMESSAGE_VERSION));
        settingsBlockedContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(weMessage.get(), BlockedContactsActivity.class);

                startActivity(launchIntent);
                finish();
            }
        });

        settingsSignInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.getConnectionService().getConnectionHandler().disconnect();
                weMessage.get().signOut();

                //TODO: Go home, end service if is connected, move to signOut()?
            }
        });
    }

    @Override
    public void onResume() {
        if (!isServiceRunning(ConnectionService.class)){
            goToLauncher();
        }

        super.onResume();
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

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(this, getSupportFragmentManager(), bundledIntent, defaultMessage, runnable);
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
            snackbar.setActionTextColor(getResources().getColor(R.color.lightRed));

            snackbar.show();
        }
    }

    private void goToLauncher(){
        if (!isFinishing() && !isDestroyed()) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            finish();
        }
    }

    private void goToChatList(){
        if (!isFinishing() && !isDestroyed()) {
            Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

            startActivity(returnIntent);
            finish();
        }
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