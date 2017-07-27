package scott.wemessage.server.events.connection;

import scott.wemessage.commons.json.connection.ClientMessage;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class ClientMessageReceivedEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;
    private ClientMessage clientMessage;
    private Boolean wasActionSuccessful;

    public ClientMessageReceivedEvent(EventManager eventManager, DeviceManager deviceManager, Device device, ClientMessage clientMessage, Boolean wasActionSuccessful){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
        this.clientMessage = clientMessage;
        this.wasActionSuccessful = wasActionSuccessful;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }

    public ClientMessage getClientMessage() {
        return clientMessage;
    }

    public Boolean getWasActionSuccessful() {
        return wasActionSuccessful;
    }
}
