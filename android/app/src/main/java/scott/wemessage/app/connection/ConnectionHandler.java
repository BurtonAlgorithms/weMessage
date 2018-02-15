package scott.wemessage.app.connection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.users.Account;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.security.CryptoFile;
import scott.wemessage.app.security.CryptoType;
import scott.wemessage.app.security.DecryptionTask;
import scott.wemessage.app.security.EncryptionTask;
import scott.wemessage.app.security.FileDecryptionTask;
import scott.wemessage.app.security.KeyTextPair;
import scott.wemessage.app.security.util.AndroidBase64Wrapper;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.ClientMessage;
import scott.wemessage.commons.connection.ConnectionMessage;
import scott.wemessage.commons.connection.ContactBatch;
import scott.wemessage.commons.connection.Heartbeat;
import scott.wemessage.commons.connection.InitConnect;
import scott.wemessage.commons.connection.ServerMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.action.JSONResult;
import scott.wemessage.commons.connection.json.message.JSONAttachment;
import scott.wemessage.commons.connection.json.message.JSONChat;
import scott.wemessage.commons.connection.json.message.JSONContact;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.connection.security.EncryptedFile;
import scott.wemessage.commons.connection.security.EncryptedText;
import scott.wemessage.commons.types.ActionType;
import scott.wemessage.commons.types.DeviceType;
import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.ByteArrayAdapter;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.commons.utils.StringUtils;

public final class ConnectionHandler extends Thread {

    private final String TAG = ConnectionService.TAG;
    private final long UPDATE_MESSAGES_ATTEMPT_QUEUE = 20;
    private final int TIME_TO_CONNECT = 1;

    private final Object serviceLock = new Object();
    private final Object socketLock = new Object();
    private final Object inputStreamLock = new Object();
    private final Object outputStreamLock = new Object();
    private final Object heartbeatThreadLock = new Object();

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean hasTriedAuthenticating = new AtomicBoolean(false);
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private AtomicBoolean isSyncingContacts = new AtomicBoolean(false);

    private ConcurrentHashMap<String, ConnectionMessage>connectionMessagesMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String>messageAndConnectionMessageMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Attachment> fileAttachmentsMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, JSONAction> currentOutgoingActionsMap = new ConcurrentHashMap<>();

    private ByteArrayAdapter byteArrayAdapter = new ByteArrayAdapter(new AndroidBase64Wrapper());
    private ConnectionService service;
    private Socket connectionSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private HeartbeatThread heartbeatThread;

    private final String ipAddress;
    private final int port;
    private boolean fastConnect = false;
    private String emailPlainText;
    private String passwordPlainText, passwordHashedText;
    private String failoverIpAddress;

    protected ConnectionHandler(ConnectionService service, String ipAddress, int port, String emailPlainText, String password, boolean alreadyHashed, String failoverIpAddress){
        this.service = service;
        this.ipAddress = ipAddress;
        this.port = port;
        this.emailPlainText = emailPlainText;
        this.failoverIpAddress = failoverIpAddress;

        if (alreadyHashed){
            this.passwordHashedText = password;
        }else {
            this.passwordPlainText = password;
        }
    }

    public AtomicBoolean isRunning(){
        return isRunning;
    }

    public AtomicBoolean isConnected(){
        return isConnected;
    }

    private ConnectionService getParentService(){
        synchronized (serviceLock){
            return service;
        }
    }

    public Socket getConnectionSocket(){
        synchronized (socketLock){
            return connectionSocket;
        }
    }

    private ObjectInputStream getInputStream(){
        synchronized (inputStreamLock){
            return inputStream;
        }
    }

    private ObjectOutputStream getOutputStream(){
        synchronized (outputStreamLock){
            return outputStream;
        }
    }

    private HeartbeatThread getHeartbeatThread(){
        synchronized (heartbeatThreadLock){
            return heartbeatThread;
        }
    }

    private ServerMessage getIncomingMessage(String prefix, Object incomingStream){
        String data = ((String) incomingStream).split(prefix)[1];
        ServerMessage serverMessage = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(data, ServerMessage.class);

        connectionMessagesMap.put(serverMessage.getMessageUuid(), serverMessage);
        return serverMessage;
    }

    private synchronized void sendOutgoingObject(Object object) throws IOException {
        getOutputStream().writeObject(object);
        getOutputStream().flush();
    }

    private void sendHeartbeat() throws IOException {
        sendOutgoingObject(new Heartbeat(Heartbeat.Type.CLIENT));
    }

    private void sendOutgoingMessage(String prefix, Object outgoingData, Class<?> dataClass) throws IOException {
        Type type = TypeToken.get(dataClass).getType();
        String outgoingDataJson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().toJson(outgoingData, type);
        ClientMessage clientMessage = new ClientMessage(UUID.randomUUID().toString(), outgoingDataJson);
        String outgoingJson = new Gson().toJson(clientMessage);

        if (dataClass == JSONAction.class){
            currentOutgoingActionsMap.put(clientMessage.getMessageUuid(), (JSONAction) outgoingData);
        }

        sendOutgoingObject(prefix + outgoingJson);
        connectionMessagesMap.put(clientMessage.getMessageUuid(), clientMessage);
    }

    private void sendOutgoingMessageWithCrypto(String prefix, Object outgoingData, Class<?> dataClass) throws IOException {
        Type type = TypeToken.get(dataClass).getType();
        String outgoingDataJson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().toJson(outgoingData, type);
        ClientMessage clientMessage = new ClientMessage(UUID.randomUUID().toString(), outgoingDataJson);
        String outgoingJson = new Gson().toJson(clientMessage);

        if (dataClass == JSONAction.class){
            currentOutgoingActionsMap.put(clientMessage.getMessageUuid(), (JSONAction) outgoingData);
        }

        EncryptionTask encryptionTask = new EncryptionTask(prefix + outgoingJson, null, CryptoType.AES);
        encryptionTask.runEncryptTask();

        sendOutgoingObject(KeyTextPair.toEncryptedText(encryptionTask.getEncryptedText()));
        connectionMessagesMap.put(clientMessage.getMessageUuid(), clientMessage);
    }

    private void sendOutgoingGenericAction(final ActionType actionType, final String... args){
        try {
            JSONAction jsonAction;

            switch (actionType) {
                case ADD_PARTICIPANT:
                    jsonAction = new JSONAction(actionType.getCode(), new String[]{args[0], args[1]});
                    break;
                case CREATE_GROUP:
                    jsonAction = new JSONAction(actionType.getCode(), new String[]{args[0], args[1], args[2]});
                    break;
                case LEAVE_GROUP:
                    jsonAction = new JSONAction(actionType.getCode(), new String[]{args[0]});
                    break;
                case REMOVE_PARTICIPANT:
                    jsonAction = new JSONAction(actionType.getCode(), new String[]{args[0], args[1]});
                    break;
                case RENAME_GROUP:
                    jsonAction = new JSONAction(actionType.getCode(), new String[]{args[0], args[1]});
                    break;
                default:
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "Could not perform action due to an unknown Action Type", new NullPointerException("Could not perform action due to an unknown Action Type"));
                    return;
            }

            boolean isActionQueued = false;

            for (JSONAction action : currentOutgoingActionsMap.values()){
               if (action.getActionType() == actionType.getCode()){
                   if (actionType != ActionType.CREATE_GROUP){
                       if (Arrays.equals(jsonAction.getArgs(), action.getArgs())){
                           isActionQueued = true;
                           break;
                       }
                   }
               }
           }

           if (!isActionQueued) {
               sendOutgoingMessageWithCrypto(weMessage.JSON_ACTION, jsonAction, JSONAction.class);
           }
        }catch(Exception ex){
            sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
            AppLogger.error(TAG, "An error occurred while trying to send an action to be performed", ex);
        }
    }

    public void sendOutgoingMessage(final Message message, final boolean performMessageManagerAdd){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (performMessageManagerAdd) {
                        weMessage.get().getMessageManager().addMessage(message, false);
                    }

                    Type type = TypeToken.get(JSONMessage.class).getType();
                    String clientMessageUuid = UUID.randomUUID().toString();
                    String outgoingDataJson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create()
                            .toJson(message.toJson(ConnectionHandler.this), type);
                    ClientMessage clientMessage = new ClientMessage(clientMessageUuid, outgoingDataJson);
                    String outgoingJson = new Gson().toJson(clientMessage);

                    if (message.getFailedAttachments().size() > 0){
                        HashMap<String, Attachment> passedAttachments = new HashMap<>();

                        for (Attachment a : message.getAttachments()){
                            passedAttachments.put(a.getUuid().toString(), a);
                        }

                        for (Attachment a : message.getFailedAttachments().keySet()){
                            passedAttachments.remove(a.getUuid().toString());
                        }

                        message.setAttachments(new ArrayList<>(passedAttachments.values()));
                        weMessage.get().getMessageManager().updateMessage(message.getIdentifier(), message, false);

                        for (Attachment a : message.getFailedAttachments().keySet()){
                            weMessage.get().getMessageManager().alertAttachmentSendFailure(a, message.getFailedAttachments().get(a));
                        }
                    }

                    connectionMessagesMap.put(clientMessage.getMessageUuid(), clientMessage);
                    messageAndConnectionMessageMap.put(clientMessageUuid, message.getIdentifier());

                    sendOutgoingObject(weMessage.JSON_NEW_MESSAGE + outgoingJson);
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_SEND_MESSAGE_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to send a new message", ex);
                }
            }
        }).start();
    }

    public void sendOutgoingFile(EncryptedFile encryptedFile, Attachment attachment) throws IOException {
        sendOutgoingObject(encryptedFile);
        fileAttachmentsMap.put(encryptedFile.getUuid(), attachment);
    }

    public void sendOutgoingAddParticipantAction(final GroupChat chat, final String participant){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendOutgoingGenericAction(ActionType.ADD_PARTICIPANT, chat.getMacGuid(), participant);
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to perform the AddParticipant action", ex);
                }
            }
        }).start();
    }

    public void sendOutgoingRemoveParticipantAction(final GroupChat chat, final String participant){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendOutgoingGenericAction(ActionType.REMOVE_PARTICIPANT, chat.getMacGuid(), participant);
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to perform the RemoveParticipant action", ex);
                }
            }
        }).start();
    }

    public void sendOutgoingRenameGroupAction(final GroupChat chat, final String newTitle){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendOutgoingGenericAction(ActionType.RENAME_GROUP, chat.getMacGuid(), newTitle);
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to perform the RenameGroup action", ex);
                }
            }
        }).start();
    }

    public void sendOutgoingCreateGroupAction(final String groupName, final List<String>participants, final String initMessage){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    sendOutgoingGenericAction(ActionType.CREATE_GROUP, groupName, StringUtils.join(participants, ",", 1), initMessage);
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to perform the CreateGroup action", ex);
                }
            }
        }).start();
    }

    public void sendOutgoingLeaveGroupAction(final GroupChat chat){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendOutgoingGenericAction(ActionType.LEAVE_GROUP, chat.getMacGuid());
                }catch(Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                    AppLogger.error(TAG, "An error occurred while trying to perform the LeaveGroup action", ex);
                }
            }
        }).start();
    }

    public void updateRegistrationToken(final String token){
        if (isConnected.get()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendOutgoingMessage(weMessage.JSON_REGISTRATION_TOKEN, token, String.class);
                    }catch (Exception ex){
                        AppLogger.error(TAG, "An error occurred while updating the instance's registration token.", ex);
                    }
                }
            }).start();
        }
    }

    public void requestContactSync(){
        if (isSyncingContacts.get()) return;
        isSyncingContacts.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendOutgoingObject(weMessage.JSON_CONTACT_SYNC + "START");
                }catch (Exception ex){
                    sendLocalBroadcast(weMessage.BROADCAST_CONTACT_SYNC_FAILED, null);
                    isSyncingContacts.set(false);
                    AppLogger.error(TAG, "An error occurred while trying to sync contacts.", ex);
                }
            }
        }).start();
    }

    public void run(){
        isRunning.set(true);

        synchronized (socketLock) {
            connectionSocket = new Socket();
        }

        String hostToCheck;

        if (port == weMessage.DEFAULT_PORT) {
            hostToCheck = ipAddress;
        } else {
            hostToCheck = ipAddress + ":" + port;
        }

        SharedPreferences sharedPref = weMessage.get().getSharedPreferences();
        fastConnect = (sharedPref.getString(weMessage.SHARED_PREFERENCES_LAST_HOST, "").equals(hostToCheck) && sharedPref.getString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, "").equalsIgnoreCase(emailPlainText));

        if (!fastConnect) {
            try {
                Thread.sleep(TIME_TO_CONNECT * 1000);
            } catch (Exception ex) { }
        }

        try {
            getConnectionSocket().connect(new InetSocketAddress(ipAddress, port), weMessage.CONNECTION_TIMEOUT_WAIT * 1000);

            synchronized (outputStreamLock) {
                outputStream = new ObjectOutputStream(getConnectionSocket().getOutputStream());
            }

            synchronized (inputStreamLock) {
                inputStream = new ObjectInputStream(getConnectionSocket().getInputStream());
            }
        }catch (IOException ex){
            try {
                if (getInputStream() != null) getInputStream().close();
            }catch (Exception exc){}

            try {
                if (getOutputStream() != null) getOutputStream().close();
            }catch (Exception exc){}

            try {
                getConnectionSocket().close();
            }catch (Exception exc){}

            boolean attemptReconnect = false;
            String failoverIp = null;
            int port = -1;

            if (!StringUtils.isEmpty(failoverIpAddress)){
                if (failoverIpAddress.contains(":")){
                    String[] split = failoverIpAddress.split(":");
                    try {
                        port = Integer.parseInt(split[1]);
                        failoverIp = split[0];
                        attemptReconnect = true;
                    }catch (Exception exception){ }
                }else {
                    failoverIp = failoverIpAddress;
                    port = weMessage.DEFAULT_PORT;
                    attemptReconnect = true;
                }
            }

            if (attemptReconnect){
                synchronized (socketLock) {
                    connectionSocket = new Socket();
                }

                try {
                    getConnectionSocket().connect(new InetSocketAddress(failoverIp, port), weMessage.CONNECTION_TIMEOUT_WAIT * 1000);

                    synchronized (outputStreamLock) {
                        outputStream = new ObjectOutputStream(getConnectionSocket().getOutputStream());
                    }

                    synchronized (inputStreamLock) {
                        inputStream = new ObjectInputStream(getConnectionSocket().getInputStream());
                    }
                }catch (IOException exc){
                    handleConnectExceptions(exc);
                    return;
                }
            }else {
                handleConnectExceptions(ex);
                return;
            }
        }

        while (isRunning.get() && !hasTriedAuthenticating.get()){
            try {
                String incoming = (String) getInputStream().readObject();
                if (incoming.startsWith(weMessage.JSON_VERIFY_PASSWORD_SECRET)){
                    ServerMessage message = getIncomingMessage(weMessage.JSON_VERIFY_PASSWORD_SECRET, incoming);
                    EncryptedText secretEncrypted = (EncryptedText) message.getOutgoing(EncryptedText.class);

                    DecryptionTask secretDecryptionTask = new DecryptionTask(new KeyTextPair(secretEncrypted.getEncryptedText(), secretEncrypted.getKey()), CryptoType.AES);
                    EncryptionTask emailEncryptionTask = new EncryptionTask(emailPlainText, null, CryptoType.AES);

                    secretDecryptionTask.runDecryptTask();
                    emailEncryptionTask.runEncryptTask();

                    String secretString = secretDecryptionTask.getDecryptedText();

                    String hashedPass;

                    if (passwordPlainText == null){
                        hashedPass = passwordHashedText;
                    }else {
                        EncryptionTask hashPasswordTask = new EncryptionTask(passwordPlainText, secretString, CryptoType.BCRYPT);
                        hashPasswordTask.runEncryptTask();

                        hashedPass = hashPasswordTask.getEncryptedText().getEncryptedText();
                        this.passwordHashedText = hashedPass;
                    }

                    EncryptionTask encryptHashedPasswordTask = new EncryptionTask(hashedPass, null, CryptoType.AES);
                    encryptHashedPasswordTask.runEncryptTask();

                    KeyTextPair encryptedEmail = emailEncryptionTask.getEncryptedText();
                    KeyTextPair encryptedHashedPassword = encryptHashedPasswordTask.getEncryptedText();

                    InitConnect initConnect = new InitConnect(
                            weMessage.WEMESSAGE_BUILD_VERSION,
                            Settings.Secure.getString(getParentService().getContentResolver(), Settings.Secure.ANDROID_ID),
                            KeyTextPair.toEncryptedText(encryptedEmail),
                            KeyTextPair.toEncryptedText(encryptedHashedPassword),
                            DeviceType.ANDROID.getTypeName(),
                            AndroidUtils.getDeviceName(),
                            FirebaseInstanceId.getInstance().getToken()
                    );

                    sendOutgoingMessage(weMessage.JSON_INIT_CONNECT, initConnect, InitConnect.class);
                    hasTriedAuthenticating.set(true);
                }
            }catch(SocketException ex){
                boolean socketOpenCheck = false;

                try {
                    if (getStackTrace(ex).contains("Socket closed") || getInputStream().read() == -1){
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_authentication_message));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        getParentService().endService();
                        socketOpenCheck = true;
                    }
                }catch (Exception exc){ }

                if (!socketOpenCheck) {
                    AppLogger.error(TAG, "An error occurred while authenticating login information", ex);

                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_authentication_message));
                    sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                    getParentService().endService();
                }
                return;
            }catch (EOFException ex){
                Bundle extras = new Bundle();
                extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_authentication_message));
                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                getParentService().endService();

                return;
            }catch(Exception ex){
                AppLogger.error(TAG, "An error occurred while authenticating login information", ex);

                Bundle extras = new Bundle();
                extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_authentication_message));
                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                getParentService().endService();
                return;
            }
        }

        while (isRunning.get()){
            try {
                MessageDatabase database = weMessage.get().getMessageDatabase();
                final Object incomingObject;

                try {
                    incomingObject = getInputStream().readObject();
                }catch (OutOfMemoryError oom){
                    System.gc();
                    weMessage.get().getMessageManager().alertAttachmentReceiveFailure(FailReason.MEMORY);
                    continue;
                }catch (SocketException ex){
                    if (getStackTrace(ex).contains("Socket closed")){
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED, null);
                        getParentService().endService();
                        return;
                    }else {
                        throw ex;
                    }
                }

                if (incomingObject instanceof Heartbeat) continue;

                if (incomingObject instanceof EncryptedFile){
                    EncryptedFile encryptedFile = (EncryptedFile) incomingObject;

                    if (fileAttachmentsMap.get(encryptedFile.getUuid()) == null && database.getAttachmentByUuid(encryptedFile.getUuid()) == null) {

                        if (!AndroidUtils.hasMemoryForOperation(encryptedFile.getEncryptedData().length)){
                            weMessage.get().getMessageManager().alertAttachmentReceiveFailure(FailReason.MEMORY);
                            encryptedFile = null;
                            continue;
                        }

                        String attachmentNamePrefix = new SimpleDateFormat("HH-mm-ss_MM-dd-yyyy", Locale.US).format(Calendar.getInstance().getTime());

                        Attachment attachment = new Attachment(UUID.fromString(encryptedFile.getUuid()), null, encryptedFile.getTransferName(),
                                new FileLocationContainer(
                                        new File(weMessage.get().getAttachmentFolder(), attachmentNamePrefix + "-" + encryptedFile.getTransferName())),
                                null, -1L);


                        CryptoFile cryptoFile = new CryptoFile(encryptedFile.getEncryptedData(), encryptedFile.getKey(), encryptedFile.getIvParams());

                        FileDecryptionTask fileDecryptionTask = new FileDecryptionTask(cryptoFile, CryptoType.AES);
                        fileDecryptionTask.runDecryptTask();

                        byte[] decryptedBytes = fileDecryptionTask.getDecryptedBytes();

                        if (decryptedBytes.length == 1){
                            if (decryptedBytes[0] == weMessage.CRYPTO_ERROR_MEMORY) {
                                weMessage.get().getMessageManager().alertAttachmentReceiveFailure(FailReason.MEMORY);

                                cryptoFile = null;
                                encryptedFile = null;
                                continue;
                            }else {
                                attachment.getFileLocation().writeBytesToFile(decryptedBytes);
                            }
                        }else {
                            attachment.getFileLocation().writeBytesToFile(decryptedBytes);
                        }

                        fileAttachmentsMap.put(encryptedFile.getUuid(), attachment);

                        cryptoFile = null;
                        fileDecryptionTask = null;
                        decryptedBytes = null;
                    }else {
                        Attachment attachment = fileAttachmentsMap.get(encryptedFile.getUuid());

                        attachment.setTransferName(encryptedFile.getTransferName());
                        fileAttachmentsMap.put(encryptedFile.getUuid(), attachment);
                    }

                    encryptedFile = null;
                    continue;
                }

                String theObject;

                if (incomingObject instanceof EncryptedText){
                    EncryptedText encryptedText = (EncryptedText) incomingObject;
                    DecryptionTask textDecryptionTask = new DecryptionTask(new KeyTextPair(encryptedText.getEncryptedText(), encryptedText.getKey()), CryptoType.AES);

                    textDecryptionTask.runDecryptTask();
                    theObject = textDecryptionTask.getDecryptedText();
                }else {
                    theObject = (String) incomingObject;
                }

                final String incoming = theObject;

                if (incoming.startsWith(weMessage.JSON_SUCCESSFUL_CONNECTION)) {
                    isConnected.set(true);

                    weMessage.get().signOut(false);

                    Account currentAccount = new Account().setEmail(emailPlainText.toLowerCase()).setEncryptedPassword(passwordHashedText);

                    if (database.getAccountByEmail(emailPlainText) == null) {
                        currentAccount.setUuid(UUID.randomUUID());

                        database.addAccount(currentAccount);
                        weMessage.get().signIn(currentAccount);
                    } else {
                        UUID oldUUID = database.getAccountByEmail(emailPlainText).getUuid();
                        currentAccount.setUuid(oldUUID);

                        database.updateAccount(oldUUID.toString(), currentAccount);
                        weMessage.get().signIn(currentAccount);
                    }

                    String hostToSave;

                    if (port == weMessage.DEFAULT_PORT) {
                        hostToSave = ipAddress;
                    } else {
                        hostToSave = ipAddress + ":" + port;
                    }

                    SharedPreferences.Editor editor = weMessage.get().getSharedPreferences().edit();
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_HOST, hostToSave);
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, emailPlainText);
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_HASHED_PASSWORD, passwordHashedText);
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_FAILOVER_IP, failoverIpAddress);

                    editor.apply();

                    Bundle successExtras = new Bundle();
                    successExtras.putBoolean(weMessage.BUNDLE_FAST_CONNECT, fastConnect);

                    sendLocalBroadcast(weMessage.BROADCAST_LOGIN_SUCCESSFUL, successExtras);

                    synchronized (heartbeatThreadLock){
                        heartbeatThread = new HeartbeatThread();
                        heartbeatThread.start();
                    }

                } else if (incoming.startsWith(weMessage.JSON_CONNECTION_TERMINATED)) {
                    ServerMessage serverMessage = getIncomingMessage(weMessage.JSON_CONNECTION_TERMINATED, incoming);
                    DisconnectReason disconnectReason = DisconnectReason.fromCode(((Integer) serverMessage.getOutgoing(Integer.class)));

                    if (disconnectReason == null) {
                        AppLogger.error(TAG, "A null disconnect reason has caused the connection to be dropped", new NullPointerException("A null disconnect reason has caused the connection to be dropped"));
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_unknown_message));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        getParentService().endService();
                    } else {
                        switch (disconnectReason) {
                            case ALREADY_CONNECTED:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ALREADY_CONNECTED, null);
                                getParentService().endService();
                                break;
                            case INVALID_LOGIN:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_INVALID_LOGIN, null);
                                getParentService().endService();
                                break;
                            case SERVER_CLOSED:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED, null);
                                getParentService().endService();
                                break;
                            case ERROR:
                                Bundle extras = new Bundle();
                                extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_server_side_message));
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                                getParentService().endService();
                                break;
                            case FORCED:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_FORCED, null);
                                getParentService().endService();
                                break;
                            case CLIENT_DISCONNECTED:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED, null);
                                getParentService().endService();
                                break;
                            case INCORRECT_VERSION:
                                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_INCORRECT_VERSION, null);
                                getParentService().endService();
                                break;
                        }
                    }
                    return;
                } else if (incoming.startsWith(weMessage.JSON_NEW_MESSAGE)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONMessage jsonMessage = (JSONMessage) getIncomingMessage(weMessage.JSON_NEW_MESSAGE, incoming).getOutgoing(JSONMessage.class);

                                EncryptedText encryptedText = jsonMessage.getEncryptedText();
                                DecryptionTask textDecryptionTask = new DecryptionTask(new KeyTextPair(encryptedText.getEncryptedText(), encryptedText.getKey()), CryptoType.AES);
                                textDecryptionTask.runDecryptTask();

                                MessageManager messageManager = weMessage.get().getMessageManager();
                                MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    Handle handle = messageDatabase.getHandleByHandleID(s);

                                    if (handle == null) {
                                        messageManager.addHandle(new Handle(UUID.randomUUID(), s, Handle.HandleType.IMESSAGE, false, false), false);
                                    }else {
                                        messageManager.updateHandle(handle.getUuid().toString(), handle.setHandleType(Handle.HandleType.IMESSAGE), false);
                                    }
                                }

                                JSONChat jsonChat = jsonMessage.getChat();
                                runChatCheck(messageManager, jsonChat, DateUtils.getDateUsing2001(jsonMessage.getDateSent() - 1));

                                if (!jsonMessage.isFromMe()) {
                                    messageManager.setHasUnreadMessages(messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()), true, false);
                                }

                                Handle sender;

                                if (StringUtils.isEmpty(jsonMessage.getHandle())) {
                                    sender = messageDatabase.getHandleByAccount(weMessage.get().getCurrentSession().getAccount());
                                } else {
                                    sender = messageDatabase.getHandleByHandleID(jsonMessage.getHandle());
                                }

                                Chat chat = messageDatabase.getChatByMacGuid(jsonChat.getMacGuid());
                                if (!chat.isInChat()) {
                                    messageManager.updateChat(chat.getIdentifier(), chat.setIsInChat(true), false);
                                }

                                ArrayList<Attachment> attachments = new ArrayList<>();

                                for (JSONAttachment jsonAttachment : jsonMessage.getAttachments()) {
                                    Attachment attachment = fileAttachmentsMap.get(jsonAttachment.getUuid());

                                    if (attachment != null) {
                                        attachment.setMacGuid(jsonAttachment.getMacGuid());
                                        attachment.setFileType(jsonAttachment.getFileType());
                                        attachment.setTotalBytes(jsonAttachment.getTotalBytes());

                                        attachments.add(attachment);
                                    }
                                }

                                Message message = new Message(UUID.randomUUID().toString(), jsonMessage.getMacGuid(), messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()), sender, attachments,
                                        textDecryptionTask.getDecryptedText(), jsonMessage.getDateSent(), jsonMessage.getDateDelivered(), jsonMessage.getDateRead(), jsonMessage.getErrored(),
                                        jsonMessage.isSent(), jsonMessage.isDelivered(), jsonMessage.isRead(), jsonMessage.isFinished(), jsonMessage.isFromMe(), MessageEffect.from(jsonMessage.getMessageEffect()), false);

                                messageManager.addMessage(message, false);
                            } catch (Exception ex) {
                                sendLocalBroadcast(weMessage.BROADCAST_NEW_MESSAGE_ERROR, null);
                                AppLogger.error(TAG, "An error occurred while fetching a new message from the server", ex);
                            }
                        }
                    }).start();
                } else if (incoming.startsWith(weMessage.JSON_MESSAGE_UPDATED)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONMessage jsonMessage = (JSONMessage) getIncomingMessage(weMessage.JSON_MESSAGE_UPDATED, incoming).getOutgoing(JSONMessage.class);
                                MessageManager messageManager = weMessage.get().getMessageManager();
                                MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    Handle handle = messageDatabase.getHandleByHandleID(s);

                                    if (handle == null) {
                                        messageManager.addHandle(new Handle(UUID.randomUUID(), s, Handle.HandleType.IMESSAGE, false, false), false);
                                    }else {
                                        messageManager.updateHandle(handle.getUuid().toString(), handle.setHandleType(Handle.HandleType.IMESSAGE), false);
                                    }
                                }

                                JSONChat jsonChat = jsonMessage.getChat();
                                runChatCheck(messageManager, jsonChat, DateUtils.getDateUsing2001(jsonMessage.getDateSent() - 1));

                                if (messageDatabase.getMessageByMacGuid(jsonMessage.getMacGuid()) == null) {

                                    EncryptedText encryptedText = jsonMessage.getEncryptedText();
                                    DecryptionTask textDecryptionTask = new DecryptionTask(new KeyTextPair(encryptedText.getEncryptedText(), encryptedText.getKey()), CryptoType.AES);
                                    textDecryptionTask.runDecryptTask();
                                    String decryptedText = textDecryptionTask.getDecryptedText();

                                    List<Message> messageCandidates = messageDatabase.getReversedMessagesWithSearchParameters(
                                            messageDatabase.getChatByMacGuid(jsonMessage.getChat().getMacGuid()), decryptedText, jsonMessage.isFromMe(), 0L, UPDATE_MESSAGES_ATTEMPT_QUEUE);

                                    boolean updated = false;

                                    for (Message candidate : messageCandidates) {
                                        if (candidate.getMacGuid() == null) {
                                            updateMessage(messageManager, candidate, jsonMessage, true);
                                            updated = true;
                                            break;
                                        }
                                    }

                                    if (!updated) {
                                        if (!StringUtils.isEmpty(StringUtils.trimORC(decryptedText))) {
                                            if (jsonMessage.isFromMe()) {
                                                Handle sender = messageDatabase.getHandleByAccount(weMessage.get().getCurrentSession().getAccount());

                                                Message message = new Message(UUID.randomUUID().toString(), jsonMessage.getMacGuid(),
                                                        messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()), sender, new ArrayList<Attachment>(),
                                                        textDecryptionTask.getDecryptedText(), jsonMessage.getDateSent(), jsonMessage.getDateDelivered(),
                                                        jsonMessage.getDateRead(), jsonMessage.getErrored(), jsonMessage.isSent(), jsonMessage.isDelivered(),
                                                        jsonMessage.isRead(), jsonMessage.isFinished(), jsonMessage.isFromMe(), MessageEffect.from(jsonMessage.getMessageEffect()), false);
                                                messageManager.addMessage(message, false);
                                            } else {
                                                AppLogger.log(AppLogger.Level.ERROR, TAG, "An error occurred while updating a message with Mac GUID: " + jsonMessage.getMacGuid() +
                                                        "  Reason: Previous message not found on system");
                                            }
                                        }
                                    }
                                } else {
                                    updateMessage(messageManager, messageDatabase.getMessageByMacGuid(jsonMessage.getMacGuid()), jsonMessage, false);
                                }
                            } catch (Exception ex) {
                                sendLocalBroadcast(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR, null);
                                AppLogger.error("An error occurred while updating the message", ex);
                            }
                        }
                    }).start();
                } else if (incoming.startsWith(weMessage.JSON_ACTION)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MessageManager messageManager = weMessage.get().getMessageManager();
                                JSONAction jsonAction = (JSONAction) getIncomingMessage(weMessage.JSON_ACTION, incoming).getOutgoing(JSONAction.class);

                                performAction(messageManager, jsonAction);
                            } catch (Exception ex) {
                                sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                                AppLogger.error("An error occurred while performing a JSONAction", ex);
                            }
                        }
                    }).start();
                } else if (incoming.startsWith(weMessage.JSON_RETURN_RESULT)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MessageManager messageManager = weMessage.get().getMessageManager();
                                JSONResult jsonResult = (JSONResult) getIncomingMessage(weMessage.JSON_RETURN_RESULT, incoming).getOutgoing(JSONResult.class);

                                processResults(messageManager, jsonResult);
                            } catch (Exception ex) {
                                sendLocalBroadcast(weMessage.BROADCAST_RESULT_PROCESS_ERROR, null);
                                AppLogger.error(TAG, "An error occurred while trying to process a return result", ex);
                            }
                        }
                    }).start();
                } else if (incoming.startsWith(weMessage.JSON_CONTACT_SYNC)) {
                    final ServerMessage message = getIncomingMessage(weMessage.JSON_CONTACT_SYNC, incoming);

                    if (!message.isJsonOfType(ContactBatch.class)){
                        String s = (String) message.getOutgoing(String.class);

                        if (s.equals(weMessage.JSON_CONTACT_SYNC_FAILED)){
                            isSyncingContacts.set(false);
                            sendLocalBroadcast(weMessage.BROADCAST_CONTACT_SYNC_FAILED, null);
                        }
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ContactBatch contactBatch = (ContactBatch) message.getOutgoing(ContactBatch.class);
                                    MessageDatabase database = weMessage.get().getMessageDatabase();

                                    for (JSONContact jsonContact : contactBatch.getContacts()) {
                                        ArrayList<Handle> handles = new ArrayList<>();
                                        String[] numbers = jsonContact.getNumbers().split(",");
                                        String[] emails = jsonContact.getEmails().split(",");

                                        if (StringUtils.isEmpty(jsonContact.getHandleId().trim())) continue;

                                        for (String number : numbers){
                                            if (StringUtils.isEmpty(number.trim())) continue;

                                            Handle handle = database.getHandleByHandleID(number);

                                            if (handle == null){
                                                handle = new Handle(UUID.randomUUID(), number, Handle.HandleType.UNKNOWN, false, false);
                                                weMessage.get().getMessageManager().addHandleNoCallback(handle, false);
                                            }

                                            handles.add(handle);
                                        }

                                        for (String email : emails){
                                            if (StringUtils.isEmpty(email.trim())) continue;

                                            Handle handle = database.getHandleByHandleID(email);

                                            if (handle == null){
                                                handle = new Handle(UUID.randomUUID(), email, Handle.HandleType.UNKNOWN, false, false);
                                                weMessage.get().getMessageManager().addHandleNoCallback(handle, false);
                                            }

                                            handles.add(handle);
                                        }

                                        Handle handle = database.getHandleByHandleID(jsonContact.getHandleId());
                                        Contact contact = database.getContactByHandle(handle);

                                        for (Handle h : handles){
                                            if (h.findRoot() instanceof Contact){
                                                Contact c = (Contact) h.findRoot();
                                                if (!c.equals(contact)){
                                                    if (c.getHandles().size() == 1){
                                                        weMessage.get().getMessageManager().deleteContactNoCallback(c.getUuid().toString(), false);
                                                    }else {
                                                        weMessage.get().getMessageManager().updateContactNoCallback(c.getUuid().toString(), c.removeHandle(h), false);
                                                    }
                                                }
                                            }
                                        }

                                        if (contact == null){
                                            contact = new Contact().setUuid(UUID.randomUUID()).setHandles(handles).setPrimaryHandle(handle);

                                            String contactName = jsonContact.getName();

                                            if (contactName.contains(" ")) {
                                                int i = contactName.lastIndexOf(" ");
                                                String[] names = {contactName.substring(0, i), contactName.substring(i + 1)};
                                                contact.setFirstName(names[0]).setLastName(names[1]);
                                            } else {
                                                contact.setFirstName(contactName).setLastName("");
                                            }

                                            if (fileAttachmentsMap.get(jsonContact.getId()) != null) {
                                                Attachment attachment = fileAttachmentsMap.get(jsonContact.getId());

                                                File srcFile = attachment.getFileLocation().getFile();

                                                if (srcFile.length() < weMessage.MAX_CHAT_ICON_SIZE) {
                                                    File newFile = new File(weMessage.get().getChatIconsFolder(), jsonContact.getId() + srcFile.getName());
                                                    FileUtils.copy(srcFile, newFile);

                                                    contact.setContactPictureFileLocation(new FileLocationContainer(newFile));
                                                }
                                            }

                                            if (!(StringUtils.isEmpty(contact.getFirstName()) && StringUtils.isEmpty(contact.getLastName()) && contact.getContactPictureFileLocation() == null)) {
                                                weMessage.get().getMessageManager().addContactNoCallback(contact, false);
                                            }
                                        } else {
                                            String contactName = jsonContact.getName();

                                            if (contactName.contains(" ")) {
                                                int i = contactName.lastIndexOf(" ");
                                                String[] names = {contactName.substring(0, i), contactName.substring(i + 1)};
                                                contact.setFirstName(names[0]).setLastName(names[1]);
                                            } else {
                                                contact.setFirstName(contactName).setLastName("");
                                            }

                                            if (fileAttachmentsMap.get(jsonContact.getId()) != null) {
                                                Attachment attachment = fileAttachmentsMap.get(jsonContact.getId());

                                                File srcFile = attachment.getFileLocation().getFile();

                                                if (srcFile.length() < weMessage.MAX_CHAT_ICON_SIZE) {
                                                    File newFile = new File(weMessage.get().getChatIconsFolder(), jsonContact.getId() + srcFile.getName());

                                                    FileUtils.copy(srcFile, newFile);

                                                    if (contact.getContactPictureFileLocation() != null && !StringUtils.isEmpty(contact.getContactPictureFileLocation().getFileLocation())) {
                                                        contact.getContactPictureFileLocation().getFile().delete();
                                                    }
                                                    contact.setContactPictureFileLocation(new FileLocationContainer(newFile));
                                                }
                                            }
                                            weMessage.get().getMessageManager().updateContactNoCallback(contact.getUuid().toString(), contact.setHandles(handles).setPrimaryHandle(handle), false);
                                        }
                                    }
                                    isSyncingContacts.set(false);
                                    weMessage.get().getMessageManager().refreshContactList();
                                    sendLocalBroadcast(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS, null);
                                } catch (Exception ex) {
                                    isSyncingContacts.set(false);
                                    sendLocalBroadcast(weMessage.BROADCAST_CONTACT_SYNC_FAILED, null);
                                }
                            }
                        }).start();
                    }
                } else if (incoming.startsWith(weMessage.JSON_NO_ACCOUNTS_FOUND_SERVER)){
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendLocalBroadcast(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION, null);
                        }
                    }, 3 * 1000L);
                }
            }catch(EOFException ex){
                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED, null);
                getParentService().endService();
                return;
            }catch (SocketException ex){
                if (getConnectionSocket().isClosed()){
                    if (isConnected.get()){
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_socket_closed));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        getParentService().endService();
                        return;
                    }
                }else {
                    if (getStackTrace(ex).contains("Connection reset")){
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_socket_closed));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        getParentService().endService();
                        return;
                    }

                    boolean socketOpenCheck = false;

                    try {
                        if (getInputStream().read() == -1){
                            Bundle extras = new Bundle();
                            extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_socket_closed));
                            sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                            getParentService().endService();
                            socketOpenCheck = true;
                        }
                    }catch (Exception exc){ }

                    if (!socketOpenCheck) {
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_unknown_message));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        AppLogger.error(TAG, "An unknown error occurred. Dropping connection to weServer", ex);
                        getParentService().endService();
                    }
                    return;
                }
            }catch (Exception ex){
                Bundle extras = new Bundle();
                extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_unknown_message));
                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                AppLogger.error(TAG, "An unknown error occurred. Dropping connection to weServer", ex);
                getParentService().endService();
                return;
            }
        }
    }

    public void disconnect(){
        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED, null);
        getParentService().endService();
    }

    protected void endConnection(){
        if (isRunning.get()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isRunning.set(false);
                    try {
                        if (isConnected.get()) {
                            try {
                                sendOutgoingMessage(weMessage.JSON_CONNECTION_TERMINATED, DisconnectReason.CLIENT_DISCONNECTED.getCode(), Integer.class);
                            }catch (IOException ex) { }

                            isConnected.set(false);

                            try {
                                getInputStream().close();
                            }catch (IOException ex){ }

                            try {
                                getOutputStream().close();
                            }catch (IOException ex){ }
                        }

                        try {
                            getConnectionSocket().close();
                        }catch (IOException ex){ }

                        isSyncingContacts.set(false);
                        connectionMessagesMap.clear();
                        messageAndConnectionMessageMap.clear();
                        fileAttachmentsMap.clear();
                        currentOutgoingActionsMap.clear();

                        if (getHeartbeatThread() != null) {
                            getHeartbeatThread().interrupt();
                        }
                        ConnectionHandler.this.interrupt();
                    } catch (Exception ex) {
                        AppLogger.error(TAG, "An error occurred while terminating the connection to the weServer.", ex);
                        ConnectionHandler.this.interrupt();
                    }
                }
            }).start();
        }
    }

    private void sendLocalBroadcast(String action, Bundle extras){
        Intent broadcastIntent = new Intent(action);

        if (extras != null) {
            broadcastIntent.putExtras(extras);
        }
        LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(broadcastIntent);
    }

    private void newGroupChat(MessageManager messageManager, JSONChat jsonChat){
        ArrayList<Handle> handleList = new ArrayList<>();

        for (String s : jsonChat.getParticipants()) {
            handleList.add(weMessage.get().getMessageDatabase().getHandleByHandleID(s));
        }
        GroupChat newChat = new GroupChat(UUID.randomUUID().toString(), null, jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                true, true, false, jsonChat.getDisplayName(), handleList);

        messageManager.addChat(newChat, false);
    }

    private void updateGroupChat(MessageManager messageManager, GroupChat existingChat, JSONChat jsonChat, Date executionTime, boolean overrideAll){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

        ArrayList<String> existingChatParticipantList = new ArrayList<>();

        for (Handle h : existingChat.getParticipants()) {
            existingChatParticipantList.add(h.getHandleID());
        }

        for (String s : existingChatParticipantList){
            if (!jsonChat.getParticipants().contains(s)){
                messageManager.removeParticipantFromGroup(existingChat, messageDatabase.getHandleByHandleID(s), executionTime, false);
            }
        }

        for (String s : jsonChat.getParticipants()){
            if (!existingChatParticipantList.contains(s) && !s.equalsIgnoreCase(weMessage.get().getCurrentSession().getAccount().getEmail())){
                messageManager.addParticipantToGroup(existingChat, messageDatabase.getHandleByHandleID(s), executionTime, false);
            }
        }

        if (!existingChat.getDisplayName().equals(jsonChat.getDisplayName())) {
            messageManager.renameGroupChat(existingChat, jsonChat.getDisplayName(), executionTime, false);
        }

        if (overrideAll){
            GroupChat updatedChat = (GroupChat) messageDatabase.getChatByIdentifier(existingChat.getIdentifier());

            messageManager.updateChat(existingChat.getIdentifier(), new GroupChat(existingChat.getIdentifier(), null, jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                    updatedChat.isInChat(), updatedChat.hasUnreadMessages(), updatedChat.isDoNotDisturb(), updatedChat.getDisplayName(), updatedChat.getParticipants()), false);
        }
    }

    private void runChatCheck(MessageManager messageManager, JSONChat jsonChat, Date executionTime){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

        if (messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()) == null) {
            if (jsonChat.getParticipants().size() < 2) {

                PeerChat peerChat = messageDatabase.getChatByHandle(messageDatabase.getHandleByHandleID(jsonChat.getParticipants().get(0)));
                if (peerChat != null){

                    PeerChat updatedChat = new PeerChat(peerChat.getIdentifier(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                            peerChat.isInChat(), peerChat.hasUnreadMessages(), peerChat.getHandle());
                    messageManager.updateChat(peerChat.getIdentifier(), updatedChat, false);

                }else {
                    PeerChat newChat = new PeerChat(UUID.randomUUID().toString(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                            true, true, messageDatabase.getHandleByHandleID(jsonChat.getParticipants().get(0)));
                    messageManager.addChat(newChat, false);
                }

            } else {
                ArrayList<GroupChat> groupChats = new ArrayList<>(messageDatabase.getGroupChatsWithName(jsonChat.getDisplayName()));

                if (groupChats.size() == 0) {
                    newGroupChat(messageManager, jsonChat);

                } else if (groupChats.size() == 1) {

                    if (groupChats.get(0).getMacGuid() == null) {
                        updateGroupChat(messageManager, groupChats.get(0), jsonChat, executionTime, true);
                    } else {
                        newGroupChat(messageManager, jsonChat);
                    }
                } else {
                    boolean updated = false;

                    for (GroupChat groupChat : groupChats) {
                        if (groupChat.getMacGuid() == null) {
                            updateGroupChat(messageManager, groupChat, jsonChat, executionTime, true);
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        newGroupChat(messageManager, jsonChat);
                    }
                }
            }
        } else {
            Chat existingChat = messageDatabase.getChatByMacGuid(jsonChat.getMacGuid());

            if (existingChat.getChatType() == Chat.ChatType.GROUP) {
                updateGroupChat(messageManager, (GroupChat) existingChat, jsonChat, executionTime, false);
            }
        }
    }

    private void processResults(MessageManager messageManager, JSONResult jsonResult){
        if (jsonResult == null){
            sendLocalBroadcast(weMessage.BROADCAST_RESULT_PROCESS_ERROR, null);
            AppLogger.error("Could not process result because the JSON Result is null", new NullPointerException("Could not process result because the JSON Result was null"));
            return;
        }

        List<ReturnType> returnTypes = parseResults(jsonResult.getResult());
        ConnectionMessage connectionMessage = connectionMessagesMap.get(jsonResult.getCorrespondingUUID());

        if (connectionMessage == null){
            sendLocalBroadcast(weMessage.BROADCAST_RESULT_PROCESS_ERROR, null);
            AppLogger.error("Could not process result because the connection message was null", new NullPointerException("Could not process result because the connection message was null"));
            return;
        }

        if (connectionMessage instanceof ServerMessage){
            ServerMessage serverMessage = (ServerMessage) connectionMessage;
            boolean isAMessage = serverMessage.isJsonOfType(JSONMessage.class);

            if (isAMessage){
                JSONMessage jsonMessage = (JSONMessage) serverMessage.getOutgoing(JSONMessage.class);
                boolean isValid = (validateMessageReturnType(messageManager, jsonMessage, jsonResult, returnTypes.get(0)) && validateMessageReturnType(messageManager, jsonMessage, jsonResult, returnTypes.get(1)));

                if (!isValid){
                    String correspondingMessageUUID = messageAndConnectionMessageMap.get(jsonResult.getCorrespondingUUID());
                    Message message = weMessage.get().getMessageDatabase().getMessageByIdentifier(correspondingMessageUUID);

                    if (message != null && message.getChat() != null) {
                        message.setHasErrored(true);
                        messageManager.updateMessage(correspondingMessageUUID, message, false);
                    }
                }

            } else {
                currentOutgoingActionsMap.remove(jsonResult.getCorrespondingUUID());

                JSONAction jsonAction = (JSONAction) serverMessage.getOutgoing(JSONAction.class);
                boolean isValid = validateActionReturnType(messageManager, jsonAction, returnTypes.get(0));

                if (isValid) {
                    performAction(messageManager, jsonAction);
                }
            }

        }else if (connectionMessage instanceof ClientMessage){
            ClientMessage clientMessage = (ClientMessage) connectionMessage;
            boolean isAMessage = clientMessage.isJsonOfType(JSONMessage.class);

            if (isAMessage){
                JSONMessage jsonMessage = (JSONMessage) clientMessage.getIncoming(JSONMessage.class);
                boolean isValid;

                if (returnTypes.size() == 1){
                    isValid = validateMessageReturnType(messageManager, jsonMessage, jsonResult, returnTypes.get(0));
                }else {
                    isValid = (validateMessageReturnType(messageManager, jsonMessage, jsonResult, returnTypes.get(0)) && validateMessageReturnType(messageManager, jsonMessage, jsonResult, returnTypes.get(1)));
                }

                if (!isValid){
                    String correspondingMessageUUID = messageAndConnectionMessageMap.get(jsonResult.getCorrespondingUUID());
                    Message message = weMessage.get().getMessageDatabase().getMessageByIdentifier(correspondingMessageUUID);

                    if (message != null && message.getChat() != null) {
                        message.setHasErrored(true);
                        messageManager.updateMessage(correspondingMessageUUID, message, false);
                    }
                }

            } else {
                currentOutgoingActionsMap.remove(jsonResult.getCorrespondingUUID());

                JSONAction jsonAction = (JSONAction) clientMessage.getIncoming(JSONAction.class);
                boolean isValid = validateActionReturnType(messageManager, jsonAction, returnTypes.get(0));

                if (isValid) {
                    performAction(messageManager, jsonAction);
                }
            }
        }
    }

    private void performAction(MessageManager messageManager, JSONAction jsonAction){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();
        ActionType actionType = ActionType.fromCode(jsonAction.getActionType());
        String[] args = jsonAction.getArgs();

        if (actionType == null){
            sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
            AppLogger.error("The JSONAction could not be performed because ActionType was null", new NullPointerException("The JSONAction could not be performed because ActionType was null"));
            return;
        }

        switch (actionType) {
            case ADD_PARTICIPANT:
                Chat apGroupChat = messageDatabase.getChatByMacGuid(args[0]);

                if (messageDatabase.getHandleByHandleID(args[1]) == null) {
                    weMessage.get().getMessageManager().addHandle(new Handle(UUID.randomUUID(), args[1], Handle.HandleType.IMESSAGE, false, false), false);
                }

                Handle apHandle = messageDatabase.getHandleByHandleID(args[1]);

                if (apGroupChat == null || !(apGroupChat instanceof GroupChat)) {
                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE, getParentService().getString(R.string.action_perform_error_group_not_found,
                            getParentService().getString(R.string.action_add_participant)));
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, extras);
                    AppLogger.error("Could not perform JSONAction Add Participant because group chat was not found by GUID lookup",
                            new NullPointerException("Could not perform JSONAction Add Participant because group chat was not found by GUID lookup"));
                    return;
                }
                messageManager.addParticipantToGroup((GroupChat) apGroupChat, apHandle, Calendar.getInstance().getTime(), false);
                break;

            case REMOVE_PARTICIPANT:
                Chat rpGroupChat = messageDatabase.getChatByMacGuid(args[0]);
                Handle rpHandle = messageDatabase.getHandleByHandleID(args[1]);

                if (rpGroupChat == null || !(rpGroupChat instanceof GroupChat)) {
                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE, getParentService().getString(R.string.action_perform_error_group_not_found,
                            getParentService().getString(R.string.action_remove_participant)));
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, extras);
                    AppLogger.error("Could not perform JSONAction Remove Participant because group chat was not found by GUID lookup",
                            new NullPointerException("Could not perform JSONAction Remove Participant because group chat was not found by GUID lookup"));
                    return;
                }

                if (rpHandle == null) {
                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE, getParentService().getString(R.string.action_perform_error_contact_not_found,
                            getParentService().getString(R.string.action_remove_participant)));
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, extras);
                    AppLogger.error("Could not perform JSONAction Remove Participant because the Handle ID was not found.",
                            new NullPointerException("Could not perform JSONAction Remove Participant because the Handle ID was not found."));
                    return;
                }
                messageManager.removeParticipantFromGroup((GroupChat) rpGroupChat, rpHandle, Calendar.getInstance().getTime(), false);
                break;

            case RENAME_GROUP:
                Chat rnGroupChat = messageDatabase.getChatByMacGuid(args[0]);

                if (rnGroupChat == null || !(rnGroupChat instanceof GroupChat)) {
                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE, getParentService().getString(R.string.action_perform_error_group_not_found,
                            getParentService().getString(R.string.action_rename_group)));
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, extras);
                    AppLogger.error("Could not perform JSONAction Rename Group because group chat was not found by GUID lookup",
                            new NullPointerException("Could not perform JSONAction Rename Group because group chat was not found by GUID lookup"));
                    return;
                }
                messageManager.renameGroupChat((GroupChat) rnGroupChat, args[1], Calendar.getInstance().getTime(), false);
                break;

            case CREATE_GROUP:
                break;

            case LEAVE_GROUP:
                Chat lvGroupChat = messageDatabase.getChatByMacGuid(args[0]);

                if (lvGroupChat == null || !(lvGroupChat instanceof GroupChat)) {
                    Bundle extras = new Bundle();
                    extras.putString(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE, getParentService().getString(R.string.action_perform_error_group_not_found,
                            getParentService().getString(R.string.action_leave_group)));
                    sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, extras);
                    AppLogger.error("Could not perform JSONAction Rename Group because group chat was not found by GUID lookup",
                            new NullPointerException("Could not perform JSONAction Rename Group because group chat was not found by GUID lookup"));
                    return;
                }
                messageManager.leaveGroup((GroupChat) lvGroupChat, Calendar.getInstance().getTime(), false);
                break;

            default:
                sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                AppLogger.error("Could not perform JSONAction because an unsupported action type was received",
                        new NullPointerException("Could not perform JSONAction because an unsupported action type was received"));
                break;
        }
    }

    private boolean validateMessageReturnType(MessageManager messageManager, JSONMessage jsonMessage, JSONResult jsonResult, ReturnType returnType){
        String correspondingMessageUUID = messageAndConnectionMessageMap.get(jsonResult.getCorrespondingUUID());

        if (returnType == null){
            messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.UNKNOWN_ERROR);
            AppLogger.error(TAG, "No return type was found", new NullPointerException("No return type was found"));
            return false;
        }

        switch (returnType){
            case UNKNOWN_ERROR:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.UNKNOWN_ERROR);
                return false;
            case SENT:
                break;
            case INVALID_NUMBER:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.INVALID_NUMBER);
                return MmsManager.isDefaultSmsApp() && isPossibleSmsChat(jsonMessage.getChat());
            case NUMBER_NOT_IMESSAGE:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.NUMBER_NOT_IMESSAGE);
                return MmsManager.isDefaultSmsApp() && isPossibleSmsChat(jsonMessage.getChat());
            case GROUP_CHAT_NOT_FOUND:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.GROUP_CHAT_NOT_FOUND);
                return false;
            case NOT_SENT:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.NOT_SENT);
                return MmsManager.isDefaultSmsApp() && isPossibleSmsChat(jsonMessage.getChat());
            case SERVICE_NOT_AVAILABLE:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.SERVICE_NOT_AVAILABLE);
                return false;
            case FILE_NOT_FOUND:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.FILE_NOT_FOUND);
                return false;
            case NULL_MESSAGE:
                break;
            case ASSISTIVE_ACCESS_DISABLED:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.ASSISTIVE_ACCESS_DISABLED);
                return false;
            case UI_ERROR:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.UI_ERROR);
                return false;
            case ACTION_PERFORMED:
                break;
            default:
                messageManager.alertMessageSendFailure(correspondingMessageUUID, ReturnType.UNKNOWN_ERROR);
                AppLogger.error(TAG, "An unsupported ReturnType enum was found", new UnsupportedOperationException("An unsupported ReturnType enum was found"));
                return false;
        }
        return true;
    }

    private boolean validateActionReturnType(MessageManager messageManager, JSONAction jsonAction, ReturnType returnType){
        if (returnType == null){
            messageManager.alertActionPerformFailure(jsonAction, ReturnType.UNKNOWN_ERROR);
            AppLogger.error(TAG, "No return type was found", new NullPointerException("No return type was found"));
            return false;
        }

        switch (returnType){
            case UNKNOWN_ERROR:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.UNKNOWN_ERROR);
                return false;
            case INVALID_NUMBER:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.INVALID_NUMBER);
                return false;
            case NUMBER_NOT_IMESSAGE:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.NUMBER_NOT_IMESSAGE);
                return false;
            case GROUP_CHAT_NOT_FOUND:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.GROUP_CHAT_NOT_FOUND);
                return false;
            case NOT_SENT:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.NOT_SENT);
                return false;
            case SERVICE_NOT_AVAILABLE:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.SERVICE_NOT_AVAILABLE);
                return false;
            case ASSISTIVE_ACCESS_DISABLED:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.ASSISTIVE_ACCESS_DISABLED);
                return false;
            case UI_ERROR:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.UI_ERROR);
                return false;
            case ACTION_PERFORMED:
                break;
            default:
                messageManager.alertActionPerformFailure(jsonAction, ReturnType.UNKNOWN_ERROR);
                AppLogger.error(TAG, "An unsupported ReturnType enum was found", new UnsupportedOperationException("An unsupported ReturnType enum was found."));
                return false;
        }
        return true;
    }

    private void updateMessage(MessageManager messageManager, Message existingMessage, JSONMessage jsonMessage, boolean overrideAll){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();
        Message newData = new Message().setIdentifier(existingMessage.getIdentifier()).setAttachments(existingMessage.getAttachments()).setText(existingMessage.getText())
                .setDateSent(jsonMessage.getDateSent()).setDateDelivered(jsonMessage.getDateDelivered()).setDateRead(jsonMessage.getDateRead()).setHasErrored(jsonMessage.getErrored())
                .setIsSent(jsonMessage.isSent()).setDelivered(jsonMessage.isDelivered()).setRead(jsonMessage.isRead()).setFinished(jsonMessage.isFinished()).setFromMe(existingMessage.isFromMe())
                .setMessageEffect(MessageEffect.from(jsonMessage.getMessageEffect())).setEffectFinished(existingMessage.getEffectFinished());

        if (overrideAll){
            Handle sender;

            if (messageDatabase.getChatByMacGuid(jsonMessage.getChat().getMacGuid()).getChatType() == Chat.ChatType.PEER){
                if (jsonMessage.isFromMe()){
                    sender = messageDatabase.getHandleByAccount(weMessage.get().getCurrentSession().getAccount());
                }else {
                    if (StringUtils.isEmpty(jsonMessage.getHandle())) {
                        sender = messageDatabase.getHandleByHandleID(jsonMessage.getHandle());
                    }else {
                        sender = messageDatabase.getHandleByHandleID(jsonMessage.getHandle());
                    }
                }
            }else {
                if (StringUtils.isEmpty(jsonMessage.getHandle())) {
                    sender = messageDatabase.getHandleByAccount(weMessage.get().getCurrentSession().getAccount());
                } else {
                    sender = messageDatabase.getHandleByHandleID(jsonMessage.getHandle());
                }
            }

            newData.setMacGuid(jsonMessage.getMacGuid()).setChat(messageDatabase.getChatByMacGuid(jsonMessage.getChat().getMacGuid()))
                    .setSender(sender);
        }else {
            newData.setMacGuid(existingMessage.getMacGuid()).setChat(messageDatabase.getChatByIdentifier(existingMessage.getChat().getIdentifier()))
                    .setSender(existingMessage.getSender());
        }

        messageManager.updateMessage(existingMessage.getIdentifier(), newData, false);
    }

    private void handleConnectExceptions(IOException exc){
        try {
            throw exc;
        }catch (UnknownHostException | EOFException | ConnectException ex){
            if (isRunning.get()){
                sendLocalBroadcast(weMessage.BROADCAST_LOGIN_CONNECTION_ERROR, null);
                getParentService().endService();
            }
        }catch (NoRouteToHostException ex){
            if (isRunning.get()) {
                sendLocalBroadcast(weMessage.BROADCAST_LOGIN_ERROR, null);
                getParentService().endService();
            }
        }catch (SocketTimeoutException ex){
            if (isRunning.get()) {
                sendLocalBroadcast(weMessage.BROADCAST_LOGIN_TIMEOUT, null);
                getParentService().endService();
            }
        }catch (SocketException ex){
            if (isRunning.get()) {
                String stacktrace = getStackTrace(ex);

                if (stacktrace.contains("Connection reset") || stacktrace.contains("Software caused connection abort") || stacktrace.contains("Network is unreachable")) {
                    sendLocalBroadcast(weMessage.BROADCAST_LOGIN_CONNECTION_ERROR, null);
                    getParentService().endService();
                }else {
                    AppLogger.error(TAG, "An error occurred while connecting to the weServer.", ex);
                    sendLocalBroadcast(weMessage.BROADCAST_LOGIN_ERROR, null);
                    getParentService().endService();
                }
            }
        }catch(IOException ex){
            if (isRunning.get()) {
                AppLogger.error(TAG, "An error occurred while connecting to the weServer.", ex);
                sendLocalBroadcast(weMessage.BROADCAST_LOGIN_ERROR, null);
                getParentService().endService();
            }
        }
    }

    private List<ReturnType> parseResults(List<Integer> integerList){
        List<ReturnType> returnList = new ArrayList<>();

        for (Integer i : integerList){
            returnList.add(ReturnType.fromCode(i));
        }

        return returnList;
    }

    private boolean isPossibleSmsChat(JSONChat jsonChat){
        for (String s : jsonChat.getParticipants()){
            if (!PhoneNumberUtil.getInstance().isPossibleNumber(s, Resources.getSystem().getConfiguration().locale.getCountry())) return false;
        }
        return true;
    }

    private String getStackTrace(Exception ex){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        ex.printStackTrace(printStream);
        printStream.close();
        return outputStream.toString();
    }

    private class HeartbeatThread extends Thread {
        @Override
        public void run() {
            while (isConnected.get()){
                try {
                    sendHeartbeat();

                    Thread.sleep(5000);
                }catch (InterruptedException ex){
                    this.interrupt();
                }catch (Exception ex){
                    if (isConnected.get()){
                        Bundle extras = new Bundle();
                        extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_unknown_loss));
                        sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                        getParentService().endService();
                    }
                }
            }
        }
    }
}