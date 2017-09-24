package scott.wemessage.server.events.connection;

import scott.wemessage.commons.crypto.EncryptedFile;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.EventManager;

public class FileReceivedEvent extends Event {

    private DeviceManager deviceManager;
    private Device device;
    private EncryptedFile encryptedFile;

    public FileReceivedEvent(EventManager eventManager, DeviceManager deviceManager, Device device, EncryptedFile encryptedFile){
        super(eventManager);
        this.deviceManager = deviceManager;
        this.device = device;
        this.encryptedFile = encryptedFile;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public Device getDevice() {
        return device;
    }

    public EncryptedFile getEncryptedFile() {
        return encryptedFile;
    }
}