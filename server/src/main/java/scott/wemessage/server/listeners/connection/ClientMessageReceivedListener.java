package scott.wemessage.server.listeners.connection;

import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.ClientMessageReceivedEvent;
import scott.wemessage.server.utils.LoggingUtils;

public class ClientMessageReceivedListener extends Listener {

    public ClientMessageReceivedListener(){
        super(ClientMessageReceivedEvent.class);
    }

    public void onEvent(Event event){
        ClientMessageReceivedEvent e = (ClientMessageReceivedEvent) event;

        try {
            if (e.getClientMessage().getIncoming() instanceof JSONAction) {
                e.getDeviceManager().getMessageServer().getDatabaseManager().queueAction((JSONAction) e.getClientMessage().getIncoming());
            }
        }catch(Exception ex){
            LoggingUtils.error("An error occurred while listening for a client message receive event", ex);
        }

        for (Device device : e.getDeviceManager().getDevices().values()){
            if (!e.getDevice().getAddress().equals(device.getAddress())){
                try {
                    if (e.getClientMessage().getIncoming() instanceof JSONAction) {
                        e.getDevice().sendOutgoingAction((JSONAction) e.getClientMessage().getIncoming());
                    } else if (e.getClientMessage().getIncoming() instanceof JSONMessage) {
                        e.getDevice().sendOutgoingMessage((JSONMessage) e.getClientMessage().getIncoming());
                    }
                }catch(Exception ex){
                    LoggingUtils.error("An error occurred while listening for a client message receive event", ex);
                }
            }
        }
    }
}
