package scott.wemessage.server.listeners.connection;

import scott.wemessage.server.connection.Device;
import scott.wemessage.server.events.Event;
import scott.wemessage.server.events.Listener;
import scott.wemessage.server.events.connection.FileReceivedEvent;

public class FileReceivedListener extends Listener {

    public FileReceivedListener(){
        super(FileReceivedEvent.class);
    }

    public void onEvent(Event event){
        FileReceivedEvent e = (FileReceivedEvent) event;

        for (Device device : e.getDeviceManager().getDevices().values()){
            if (!e.getDevice().getAddress().equals(device.getAddress())){
                e.getDevice().sendOutgoingFile(e.getEncryptedFile());
            }
        }
    }
}