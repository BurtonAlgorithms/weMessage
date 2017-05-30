package scott.wemessage.server.connection;

import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.events.EventManager;
import scott.wemessage.server.events.connection.DeviceJoinEvent;
import scott.wemessage.server.events.connection.DeviceQuitEvent;
import scott.wemessage.server.ServerLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DeviceManager extends Thread {

    private final String TAG = "weServer Device Service";
    private final Object messageServerLock = new Object();
    private final Object socketListenerLock = new Object();

    private MessageServer messageServer;
    private int PORT;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private ServerSocket socketListener;
    private ConcurrentHashMap<String, Device> devices = new ConcurrentHashMap<>();

    public DeviceManager(MessageServer server){
        this.messageServer = server;
        this.PORT = getMessageServer().getConfiguration().getPort();
    }

    public MessageServer getMessageServer(){
        synchronized (messageServerLock) {
            return messageServer;
        }
    }

    public ServerSocket getSocketListener(){
        synchronized (socketListenerLock){
            return socketListener;
        }
    }

    public ConcurrentHashMap<String, Device> getDevices(){
        return devices;
    }

    public Device getDeviceById(String deviceId){
        for (Device device : devices.values()){
            if (device.getDeviceId().equals(deviceId)){
                return device;
            }
        }
        return null;
    }

    public Device getDeviceByAddress(String address){
        return devices.get(address);
    }

    public boolean hasDevice(String address){
        return devices.containsKey(address);
    }

    public boolean addDevice(Device device){
        if (!hasDevice(device.getAddress())) {
            EventManager eventManager = getMessageServer().getEventManager();

            devices.putIfAbsent(device.getAddress(), device);
            eventManager.callEvent(new DeviceJoinEvent(eventManager, this, device));
            return true;
        }
        return false;
    }

    public boolean removeDevice(Device device, DisconnectReason reason, String reasonMessage){
        if(hasDevice(device.getAddress())) {
            EventManager eventManager = getMessageServer().getEventManager();

            device.killDevice(reason);
            devices.remove(device.getAddress());
            eventManager.callEvent(new DeviceQuitEvent(eventManager, this, device, reason));

            if (reasonMessage == null) {
                ServerLogger.log(ServerLogger.Level.INFO, TAG, "Disconnecting device with IP Address: " + device.getAddress());
            }else {
                ServerLogger.log(ServerLogger.Level.INFO, TAG, reasonMessage);
            }
            return true;
        }
        return false;
    }

    public void stopService() {
        if (isRunning.get()) {
            isRunning.set(false);
            try {
                getSocketListener().close();
                ServerLogger.log(ServerLogger.Level.INFO, TAG, "Device Service is shutting down. Disconnecting all active connections...");

                for (Device device : devices.values()) {
                    removeDevice(device, DisconnectReason.SERVER_CLOSED, null);
                }
            } catch (Exception ex) {
                ServerLogger.error(TAG, "An error occurred while shutting down the Device Service.", ex);
            }
        }
    }

    public void run(){
        try {
            synchronized (socketListenerLock) {
                socketListener = new ServerSocket(PORT);
            }
            isRunning.set(true);
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "Device Service has started");

            while (isRunning.get()) {
                try {
                    Device device = new Device(this, getSocketListener().accept());
                    device.start();
                }catch (Exception ex){
                    if (!getSocketListener().isClosed()) {
                        ServerLogger.error(TAG, "Caught an error while trying to initialize a device.", ex);
                    }
                }
            }
        }catch(IOException ex){
            ServerLogger.error(TAG, "An error has occurred in initializing the Device Manager. Shutting down.", ex);
            getMessageServer().shutdown(-1, false);
        }
    }

    public void sendOutgoingMessage(Device device, String prefix, Object outgoingData){
        device.sendOutgoingMessage(prefix, outgoingData);
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}