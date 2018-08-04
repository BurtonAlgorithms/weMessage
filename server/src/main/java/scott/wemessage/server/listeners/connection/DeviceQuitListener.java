package scott.wemessage.server.listeners.connection;

import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.DeviceQuitEvent;

public class DeviceQuitListener extends Listener {

    public DeviceQuitListener(){
        super(DeviceQuitEvent.class);
    }

    public void onEvent(Event e){
        DeviceQuitEvent event = (DeviceQuitEvent) e;
    }
}