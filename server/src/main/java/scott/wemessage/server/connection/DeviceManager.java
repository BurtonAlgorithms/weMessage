package scott.wemessage.server.connection;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.json.message.JSONNotification;
import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.events.EventManager;
import scott.wemessage.server.events.connection.DeviceJoinEvent;
import scott.wemessage.server.events.connection.DeviceQuitEvent;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.weMessage;

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

            device.sendOutgoingMessage(weMessage.JSON_SUCCESSFUL_CONNECTION, true, Boolean.class);
            devices.putIfAbsent(device.getAddress(), device);
            eventManager.callEvent(new DeviceJoinEvent(eventManager, this, device));
            return true;
        }
        return false;
    }

    public boolean removeDevice(Device device, DisconnectReason reason, String reasonMessage){
        if(hasDevice(device.getAddress())) {
            EventManager eventManager = getMessageServer().getEventManager();

            if (reasonMessage == null) {
                ServerLogger.log(ServerLogger.Level.INFO, TAG, "Disconnecting device with IP Address: " + device.getAddress());
                ServerLogger.emptyLine();
            }else {
                ServerLogger.log(ServerLogger.Level.INFO, TAG, reasonMessage);
                ServerLogger.emptyLine();
            }

            devices.remove(device.getAddress());

            if (reason == DisconnectReason.CLIENT_DISCONNECTED) {
                device.killDeviceByClientMessage();
            }else {
                device.killDevice(reason);
            }
            eventManager.callEvent(new DeviceQuitEvent(eventManager, this, device, reason));
            return true;
        }
        return false;
    }

    public void sendNotification(final String registrationToken, final Message message){
        if (!StringUtils.isEmpty(registrationToken)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        String keys = AESCrypto.keysToString(AESCrypto.generateKeys());
                        String encryptedText = AESCrypto.encryptString(message.getText(), keys);
                        String chatName = "";

                        if (message.getChat() instanceof GroupChat) {
                            if (!StringUtils.isEmpty(((GroupChat) message.getChat()).getDisplayName())) {
                                chatName = ((GroupChat) message.getChat()).getDisplayName();
                            }
                        }

                        JSONNotification notification = new JSONNotification(
                                registrationToken,
                                encryptedText,
                                keys,
                                message.getHandle().getHandleID(),
                                message.getChat().getGuid(),
                                chatName
                        );

                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(notification));
                        Request request = new Request.Builder().url(weMessage.NOTIFICATION_FUNCTION_URL).post(body).build();

                        Response response = client.newCall(request).execute();
                        response.close();
                    } catch (Exception ex) {
                        ServerLogger.error(TAG, "An error occurred while trying to send a notification to Device with Token: " + registrationToken, ex);
                    }
                }
            }).start();
        }
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

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}