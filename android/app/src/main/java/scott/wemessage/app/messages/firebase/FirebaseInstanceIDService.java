package scott.wemessage.app.messages.firebase;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        if (isServiceRunning(ConnectionService.class)){
            final ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

            Intent intent = new Intent(this, ConnectionService.class);
            bindService(intent, serviceConnection, Context.BIND_IMPORTANT);

            serviceConnection.scheduleTask(new Runnable() {
                @Override
                public void run() {
                    serviceConnection.getConnectionService().getConnectionHandler().updateRegistrationToken(FirebaseInstanceId.getInstance().getToken());
                    unbindService(serviceConnection);
                }
            });
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