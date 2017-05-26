package scott.wemessage.server.events.connection;

import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.connection.DisconnectReason;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class DeviceQuitEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;
    private DisconnectReason disconnectReason;

    public DeviceQuitEvent(EventManager eventManager, DeviceManager deviceManager, Device device, DisconnectReason disconnectReason){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
        this.disconnectReason = disconnectReason;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }

    public DisconnectReason getDisconnectReason() {
        return disconnectReason;
    }
}