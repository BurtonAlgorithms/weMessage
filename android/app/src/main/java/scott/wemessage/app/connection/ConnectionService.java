package scott.wemessage.app.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.weMessage;

public class ConnectionService extends Service {

    private final String TAG = "Connection Service";
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

    //TODO: On destroy stuff <--- Use a started / connected boolean for this
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (connectionThreadLock){
            ConnectionThread connectionThread = new ConnectionThread(this, intent.getStringExtra(weMessage.ARG_HOST), intent.getIntExtra(weMessage.ARG_PORT, -1),
                    intent.getStringExtra(weMessage.ARG_EMAIL), intent.getStringExtra(weMessage.ARG_PASSWORD));

            connectionThread.start();
            this.connectionThread = connectionThread;
        }

        //TODO: Don't allow multiple threads
        //TODO: implement start sticky stuff?
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void endService(){
        Intent serviceClosedIntent = new Intent(weMessage.INTENT_CONNECTION_SERVICE_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(serviceClosedIntent);

        getConnectionThread().endConnection();
        stopSelf();
    }

    public class ConnectionThread extends Thread {

        private final Object serviceLock = new Object();
        private final Object socketLock = new Object();
        private final Object inputStreamLock = new Object();
        private final Object outputStreamLock = new Object();

        private AtomicBoolean isRunning = new AtomicBoolean(false);
        private AtomicBoolean isConnected = new AtomicBoolean(false);

        private ConnectionService service;
        private Socket connectionSocket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        private final String ipAddress;
        private final int port;
        private String email;
        private String password;

        public ConnectionThread(ConnectionService service, String ipAddress, int port, String email, String password){
            this.service = service;
            this.ipAddress = ipAddress;
            this.port = port;
            this.email = email;
            this.password = password;
        }

        public AtomicBoolean isRunning(){
            return isRunning;
        }

        public AtomicBoolean isConnected(){
            return isConnected;
        }

        public ConnectionService getParentService(){
            synchronized (serviceLock){
                return service;
            }
        }

        public Socket getConnectionSocket(){
            synchronized (socketLock){
                return connectionSocket;
            }
        }

        public ObjectInputStream getInputStream(){
            synchronized (inputStreamLock){
                return inputStream;
            }
        }

        public ObjectOutputStream getOutputStream(){
            synchronized (outputStreamLock){
                return outputStream;
            }
        }

        public void run(){
            isRunning.set(true);

            synchronized (socketLock) {
                connectionSocket = new Socket();
            }

            try {
                getConnectionSocket().connect(new InetSocketAddress(ipAddress, port), weMessage.CONNECTION_TIMEOUT_WAIT * 1000);

                synchronized (inputStreamLock){
                    inputStream = new ObjectInputStream(getConnectionSocket().getInputStream());
                }
                synchronized (outputStreamLock){
                    outputStream = new ObjectOutputStream(getConnectionSocket().getOutputStream());
                }


            }catch(SocketTimeoutException ex){
                Intent timeoutIntent = new Intent(weMessage.INTENT_LOGIN_TIMEOUT);
                LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(timeoutIntent);
                getParentService().endService();
                return;
            }catch(IOException ex){
                Log.e(TAG, "An error occurred while connecting to the weServer.", ex);

                if (isRunning.get()) {
                    Intent timeoutIntent = new Intent(weMessage.INTENT_LOGIN_ERROR);
                    LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(timeoutIntent);
                    getParentService().endService();
                    return;
                }
            }


        }

        public void endConnection(){
            if (isRunning.get()) {
                isRunning.set(false);

                try {
                    if (isConnected.get()) {
                        isConnected.set(false);
                        getInputStream().close();
                        getOutputStream().close();
                    }
                    getConnectionSocket().close();
                } catch (Exception ex) {
                    Log.e(TAG, "An error occurred while terminating the connection to the weServer.", ex);
                    interrupt();
                }
                interrupt();
            }
        }
    }

    public class ConnectionServiceBinder extends Binder {

        public ConnectionService getService(){
            return ConnectionService.this;
        }
    }
}