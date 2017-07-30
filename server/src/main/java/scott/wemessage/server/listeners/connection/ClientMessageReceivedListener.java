package scott.wemessage.server.listeners.connection;

import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.utils.ByteArrayAdapter;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.ClientMessageReceivedEvent;
import scott.wemessage.server.security.ServerBase64Wrapper;

public class ClientMessageReceivedListener extends Listener {

    public ClientMessageReceivedListener(){
        super(ClientMessageReceivedEvent.class);
    }

    public void onEvent(Event event){
        ClientMessageReceivedEvent e = (ClientMessageReceivedEvent) event;

        if (e.getWasActionSuccessful() != null && e.getWasActionSuccessful()) {
            try {
                if (e.getClientMessage().isJsonOfType(JSONAction.class, new ByteArrayAdapter(new ServerBase64Wrapper()))) {
                    e.getDeviceManager().getMessageServer().getDatabaseManager().queueAction((JSONAction) e.getClientMessage().getIncoming(JSONAction.class, new ByteArrayAdapter(new ServerBase64Wrapper())));
                }
            } catch (Exception ex) {
                ServerLogger.error("An error occurred while listening for a client message receive event", ex);
            }
        }

        for (Device device : e.getDeviceManager().getDevices().values()){
            if (!e.getDevice().getAddress().equals(device.getAddress())){
                try {
                    if (e.getClientMessage().isJsonOfType(JSONAction.class, new ByteArrayAdapter(new ServerBase64Wrapper()))) {
                        if (e.getWasActionSuccessful() != null && e.getWasActionSuccessful()) {
                            e.getDevice().sendOutgoingAction((JSONAction) e.getClientMessage().getIncoming(JSONAction.class, new ByteArrayAdapter(new ServerBase64Wrapper())));
                        }
                    } else if (e.getClientMessage().isJsonOfType(JSONMessage.class, new ByteArrayAdapter(new ServerBase64Wrapper()))) {
                        e.getDevice().sendOutgoingMessage((JSONMessage) e.getClientMessage().getIncoming(JSONMessage.class, new ByteArrayAdapter(new ServerBase64Wrapper())));
                    }
                }catch(Exception ex){
                    ServerLogger.error("An error occurred while listening for a client message receive event", ex);
                }
            }
        }
    }
}