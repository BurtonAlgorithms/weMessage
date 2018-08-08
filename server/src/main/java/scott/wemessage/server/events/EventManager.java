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

package scott.wemessage.server.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;

public final class EventManager extends Thread {

    private final String TAG = "weServer Event Manager";
    private final Object messageServerLock = new Object();
    private final List<Listener> listeners = Collections.synchronizedList(new ArrayList<Listener>());

    private MessageServer messageServer;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public EventManager(MessageServer messageServer){
        this.messageServer = messageServer;
    }

    public void run(){
        isRunning.set(true);
        ServerLogger.log(ServerLogger.Level.INFO, TAG, "Event manager has started");
    }

    public MessageServer getMessageServer(){
        synchronized (messageServerLock){
            return messageServer;
        }
    }

    public void stopService(){
        if (isRunning.get()){
            isRunning.set(false);
            listeners.clear();
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "Event manager is shutting down");
        }
    }

    public boolean registerListener(Listener listener){
        if (!listeners.contains(listener)){
            listeners.add(listener);
            return true;
        }
        return false;
    }

    public boolean unregisterListener(Listener listener){
        if (listeners.contains(listener)){
            listeners.remove(listener);
            return true;
        }
        return false;
    }

    public void callEvent(Event event){
        synchronized (listeners){
            Iterator<Listener> i = listeners.iterator();
            while (i.hasNext()){
                Listener listener = i.next();

                if (listener.getEventIdentifier() == event.getClass()){
                    listener.onEvent(event);
                }
            }
        }
    }
}