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
    private final Object connectionHandlerLock = new Object();
    private final IBinder binder = new ConnectionServiceBinder();

    private ConnectionHandler connectionHandler;

    public ConnectionHandler getConnectionHandler(){
        synchronized (connectionHandlerLock){
            return connectionHandler;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (getConnectionHandler().isRunning().get()){
            Intent serviceClosedIntent = new Intent(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(serviceClosedIntent);

            getConnectionHandler().endConnection();
            weMessage.get().dumpMessageManager();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getConnectionHandler() != null && getConnectionHandler().isRunning().get()){
            throw new ConnectionException("There is already a connection to the weServer established.");
        }

        synchronized (connectionHandlerLock){
            ConnectionHandler connectionHandler = new ConnectionHandler(this, intent.getStringExtra(weMessage.ARG_HOST), intent.getIntExtra(weMessage.ARG_PORT, -1),
                    intent.getStringExtra(weMessage.ARG_EMAIL), intent.getStringExtra(weMessage.ARG_PASSWORD), intent.getBooleanExtra(weMessage.ARG_PASSWORD_ALREADY_HASHED, false));

            connectionHandler.start();
            this.connectionHandler = connectionHandler;
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
        getConnectionHandler().endConnection();
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