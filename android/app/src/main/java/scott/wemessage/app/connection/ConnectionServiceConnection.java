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