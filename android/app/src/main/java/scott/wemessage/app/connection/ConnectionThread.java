package scott.wemessage.app.connection;

import android.content.Intent;
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

    protected ConnectionThread(ConnectionService service, String ipAddress, int port, String email, String password){
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
            isConnected.set(true);

            if (isConnected.get()) {
                synchronized (inputStreamLock) {
                    inputStream = new ObjectInputStream(getConnectionSocket().getInputStream());
                }
                synchronized (outputStreamLock) {
                    outputStream = new ObjectOutputStream(getConnectionSocket().getOutputStream());
                }
            }


        }catch(SocketTimeoutException ex){
            Intent timeoutIntent = new Intent(weMessage.INTENT_LOGIN_TIMEOUT);
            LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(timeoutIntent);
            getParentService().endService();
            return;
        }catch(IOException ex){
            Log.e(ConnectionService.TAG, "An error occurred while connecting to the weServer.", ex);

            if (isRunning.get()) {
                Intent timeoutIntent = new Intent(weMessage.INTENT_LOGIN_ERROR);
                LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(timeoutIntent);
                getParentService().endService();
                return;
            }
        }

        //TODO: Some stuffs here
    }

    //TODO: Send out the message telling it to terminate connection
    protected void endConnection(){
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
                Log.e(ConnectionService.TAG, "An error occurred while terminating the connection to the weServer.", ex);
                interrupt();
            }
            interrupt();
        }
    }
}