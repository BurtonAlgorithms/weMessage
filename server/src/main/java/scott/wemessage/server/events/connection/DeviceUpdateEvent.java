package scott.wemessage.server.events.connection;

import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class DeviceUpdateEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;

    public DeviceUpdateEvent(EventManager eventManager, DeviceManager deviceManager, Device device){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }
}