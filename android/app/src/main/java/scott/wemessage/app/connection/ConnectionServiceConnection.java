package scott.wemessage.app.connection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;

public class ConnectionServiceConnection implements ServiceConnection {

    private ConnectionService connectionService;
    private ArrayList<Runnable> runnableArrayList = new ArrayList<>();
    private boolean isConnected = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
        isConnected = true;

        for (Runnable runnable : runnableArrayList){
            runnable.run();
        }
        runnableArrayList.clear();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        connectionService = null;
        isConnected = false;
    }

    public ConnectionService getConnectionService(){
        return connectionService;
    }

    public void scheduleTask(Runnable runnable){
        if (isConnected){
            runnable.run();
        }else {
            runnableArrayList.add(runnable);
        }
    }
}