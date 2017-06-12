package scott.wemessage.app.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AndroidRuntimeException;

import scott.wemessage.app.weMessage;

public class ConnectionService extends Service {

    protected static final String TAG = "Connection Service";
    private final Object connectionThreadLock = new Object();
    private final IBinder binder = new ConnectionServiceBinder();

    private ConnectionThread connectionThread;

    public ConnectionThread getConnectionThread(){
        synchronized (connectionThreadLock){
            return connectionThread;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (getConnectionThread().isRunning().get()){
            Intent serviceClosedIntent = new Intent(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(serviceClosedIntent);

            getConnectionThread().endConnection();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getConnectionThread() != null && getConnectionThread().isRunning().get()){
            throw new ConnectionException("There is already a connection to the weServer established.");
        }

        synchronized (connectionThreadLock){
            ConnectionThread connectionThread = new ConnectionThread(this, intent.getStringExtra(weMessage.ARG_HOST), intent.getIntExtra(weMessage.ARG_PORT, -1),
                    intent.getStringExtra(weMessage.ARG_EMAIL), intent.getStringExtra(weMessage.ARG_PASSWORD), intent.getBooleanExtra(weMessage.ARG_PASSWORD_ALREADY_HASHED, false));

            connectionThread.start();
            this.connectionThread = connectionThread;
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void endService(){
        Intent serviceClosedIntent = new Intent();
        serviceClosedIntent.setAction(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(serviceClosedIntent);
        getConnectionThread().endConnection();
        stopSelf();
    }

    public class ConnectionServiceBinder extends Binder {

        public ConnectionService getService(){
            return ConnectionService.this;
        }
    }

    public static class ConnectionException extends AndroidRuntimeException {
        public ConnectionException(String name) {
            super(name);
        }

        public ConnectionException(String name, Throwable cause) {
            super(name, cause);
        }

        public ConnectionException(Exception cause) {
            super(cause);
        }
    }
}