package scott.wemessage.server.listeners.connection;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.DeviceUpdateEvent;

public class DeviceUpdateListener extends Listener {

    public DeviceUpdateListener(){
        super(DeviceUpdateEvent.class);
    }

    public void onEvent(Event e){
        DeviceUpdateEvent event = (DeviceUpdateEvent) e;

        try {
            event.getMessageServer().getDatabaseManager().setRegistrationToken(event.getDevice().getDeviceId(), event.getDevice().getRegistrationToken());
        }catch (Exception ex){
            ServerLogger.error("An error occurred while updating Device: " + event.getDevice().getAddress() + ". \nPlease disconnect it or it will not work as expected.", ex);
        }
    }
}