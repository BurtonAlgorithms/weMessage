package scott.wemessage.app.connection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.database.DatabaseManager;
import scott.wemessage.app.database.MessageDatabase;
import scott.wemessage.app.database.objects.Account;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Handle;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chat.Chat;
import scott.wemessage.app.messages.objects.chat.GroupChat;
import scott.wemessage.app.messages.objects.chat.PeerChat;
import scott.wemessage.app.security.CryptoFile;
import scott.wemessage.app.security.CryptoType;
import scott.wemessage.app.security.DecryptionTask;
import scott.wemessage.app.security.EncryptionTask;
import scott.wemessage.app.security.FileDecryptionTask;
import scott.wemessage.app.security.KeyTextPair;
import scott.wemessage.app.security.util.AndroidBase64Wrapper;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.connection.ClientMessage;
import scott.wemessage.commons.json.connection.InitConnect;
import scott.wemessage.commons.json.connection.ServerMessage;
import scott.wemessage.commons.json.message.JSONAttachment;
import scott.wemessage.commons.json.message.JSONChat;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.json.message.security.JSONEncryptedFile;
import scott.wemessage.commons.json.message.security.JSONEncryptedText;
import scott.wemessage.commons.types.DeviceType;
import scott.wemessage.commons.types.DisconnectReason;
import scott.wemessage.commons.utils.ByteArrayAdapter;
import scott.wemessage.commons.utils.StringUtils;

public class ConnectionThread extends Thread {

    private final String TAG = ConnectionService.TAG;
    private final int UPDATE_MESSAGES_ATTEMPT_QUEUE = 20;

    private final Object serviceLock = new Object();
    private final Object socketLock = new Object();
    private final Object inputStreamLock = new Object();
    private final Object outputStreamLock = new Object();

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean hasTriedAuthenticating = new AtomicBoolean(false);
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private ConcurrentHashMap<String, ClientMessage>clientMessagesMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ServerMessage>serverMessagesMap = new ConcurrentHashMap<>();

    private ConnectionService service;
    private Socket connectionSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final String ipAddress;
    private final int port;
    private String emailPlainText;
    private String passwordPlainText, passwordHashedText;

    protected ConnectionThread(ConnectionService service, String ipAddress, int port, String emailPlainText, String password, boolean alreadyHashed){
        this.service = service;
        this.ipAddress = ipAddress;
        this.port = port;
        this.emailPlainText = emailPlainText;

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

    public ConnectionService getParentService(){
        synchronized (serviceLock){
            return service;
        }
    }

    public Socket getConnectionSocket(){
        synchronized (socketLock){
            return connectionSocket;
        }
    }

    public ObjectInputStream getInputStream(){
        synchronized (inputStreamLock){
            return inputStream;
        }
    }

    public ObjectOutputStream getOutputStream(){
        synchronized (outputStreamLock){
            return outputStream;
        }
    }

    public ServerMessage getIncomingMessage(String prefix, Object incomingStream ){
        String data = ((String) incomingStream).split(prefix)[1];
        ServerMessage serverMessage = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayAdapter(new AndroidBase64Wrapper())).create().fromJson(data, ServerMessage.class);

        serverMessagesMap.put(serverMessage.getMessageUuid(), serverMessage);
        return serverMessage;
    }

    public void sendOutgoingMessage(String prefix, Object outgoingData, Class<?> dataClass) throws IOException {
        Type type = TypeToken.get(dataClass).getType();
        String outgoingDataJson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayAdapter(new AndroidBase64Wrapper())).create().toJson(outgoingData, type);
        ClientMessage clientMessage = new ClientMessage(UUID.randomUUID().toString(), outgoingDataJson);
        String outgoingJson = new Gson().toJson(clientMessage);

        getOutputStream().writeObject(prefix + outgoingJson);
        getOutputStream().flush();

        clientMessagesMap.put(clientMessage.getMessageUuid(), clientMessage);
    }

    public void sendOutgoingMessage(){

    }

    public void sendOutgoingAction(){
        //TODO: When doing the args for the action type, make sure they are all specified <--- TODO: Date Util thing weServer does, so date is not null
    }

    public void run(){
        final ByteArrayAdapter byteArrayAdapter = new ByteArrayAdapter(new AndroidBase64Wrapper());

        isRunning.set(true);

        synchronized (socketLock) {
            connectionSocket = new Socket();
        }

        try {
            Thread.sleep(2000);
        }catch(Exception ex){
            AppLogger.error(TAG, "An error occurred while trying to make a thread sleep", ex);
        }

        try {
            getConnectionSocket().connect(new InetSocketAddress(ipAddress, port), weMessage.CONNECTION_TIMEOUT_WAIT * 1000);

            synchronized (outputStreamLock) {
                outputStream = new ObjectOutputStream(getConnectionSocket().getOutputStream());
            }

            synchronized (inputStreamLock) {
                inputStream = new ObjectInputStream(getConnectionSocket().getInputStream());
            }
        }catch(SocketTimeoutException ex){
            if (isRunning.get()) {
                sendLocalBroadcast(weMessage.INTENT_LOGIN_TIMEOUT, null);
                getParentService().endService();
            }
            return;
        }catch(IOException ex){
            if (isRunning.get()) {
                AppLogger.error(TAG, "An error occurred while connecting to the weServer.", ex);
                sendLocalBroadcast(weMessage.INTENT_LOGIN_ERROR, null);
                getParentService().endService();
            }
            return;
        }

        while (isRunning.get() && !hasTriedAuthenticating.get()){
            try {
                String incoming = (String) getInputStream().readObject();
                if (incoming.startsWith(weMessage.JSON_VERIFY_PASSWORD_SECRET)){
                    ServerMessage message = getIncomingMessage(weMessage.JSON_VERIFY_PASSWORD_SECRET, incoming);
                    JSONEncryptedText secretEncrypted = (JSONEncryptedText) message.getOutgoing(JSONEncryptedText.class, byteArrayAdapter);

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
                            KeyTextPair.toEncryptedJSON(encryptedEmail),
                            KeyTextPair.toEncryptedJSON(encryptedHashedPassword),
                            DeviceType.ANDROID.getTypeName()
                    );

                    sendOutgoingMessage(weMessage.JSON_INIT_CONNECT, initConnect, InitConnect.class);
                    hasTriedAuthenticating.set(true);
                }
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
                MessageDatabase database = MessageManager.getInstance(getParentService()).getMessageDatabase();
                final String incoming = (String) getInputStream().readObject();

                if (incoming.startsWith(weMessage.JSON_SUCCESSFUL_CONNECTION)){
                    isConnected.set(true);
                    Account currentAccount = new Account().setEmail(emailPlainText).setEncryptedPassword(passwordHashedText);

                    if (database.getAccountByEmail(emailPlainText) == null){
                        currentAccount.setUuid(UUID.randomUUID());

                        database.setCurrentAccount(currentAccount);
                        database.addAccount(currentAccount);
                    }else {
                        UUID oldUUID = database.getAccountByEmail(emailPlainText).getUuid();
                        currentAccount.setUuid(oldUUID);

                        database.setCurrentAccount(currentAccount);
                        database.updateAccount(oldUUID.toString(), currentAccount);
                    }

                    String hostToSave;

                    if (port == weMessage.DEFAULT_PORT){
                        hostToSave = ipAddress;
                    }else {
                        hostToSave = ipAddress + ":" + port;
                    }

                    SharedPreferences.Editor editor = getParentService().getSharedPreferences(weMessage.APP_IDENTIFIER, Context.MODE_PRIVATE).edit();
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_HOST, hostToSave);
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, emailPlainText);
                    editor.putString(weMessage.SHARED_PREFERENCES_LAST_HASHED_PASSWORD, passwordHashedText);

                    editor.apply();

                    sendLocalBroadcast(weMessage.BROADCAST_LOGIN_SUCCESSFUL, null);
                } else if (incoming.startsWith(weMessage.JSON_CONNECTION_TERMINATED)) {
                    ServerMessage serverMessage = getIncomingMessage(weMessage.JSON_CONNECTION_TERMINATED, incoming);
                    DisconnectReason disconnectReason = DisconnectReason.fromCode(((Integer) serverMessage.getOutgoing(Integer.class, byteArrayAdapter)));

                    if (disconnectReason == null) {
                        AppLogger.error(TAG, "A null disconnect reason has caused the connection to be dropped", new NullPointerException());
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
                }else if (incoming.startsWith(weMessage.JSON_NEW_MESSAGE)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONMessage jsonMessage = (JSONMessage) getIncomingMessage(weMessage.JSON_NEW_MESSAGE, incoming).getOutgoing(JSONMessage.class, byteArrayAdapter);

                                JSONEncryptedText encryptedText = jsonMessage.getEncryptedText();
                                DecryptionTask textDecryptionTask = new DecryptionTask(new KeyTextPair(encryptedText.getEncryptedText(), encryptedText.getKey()), CryptoType.AES);
                                textDecryptionTask.runDecryptTask();

                                MessageManager messageManager = MessageManager.getInstance(getParentService());
                                MessageDatabase messageDatabase = messageManager.getMessageDatabase();

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    if (messageDatabase.getHandleByHandleID(s) == null) {
                                        messageDatabase.addHandle(new Handle(UUID.randomUUID(), s, Handle.HandleType.IMESSAGE));
                                    }
                                }

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    if (messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(s)) == null) {
                                        messageManager.addContact(new Contact(UUID.randomUUID(), null, null, messageDatabase.getHandleByHandleID(s), null), false);
                                    }
                                }

                                JSONChat jsonChat = jsonMessage.getChat();
                                runChatCheck(messageManager, jsonChat);

                                messageManager.setHasUnreadMessages(messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()), true, false);

                                Contact sender;

                                if (StringUtils.isEmpty(jsonMessage.getHandle())) {
                                    Handle meHandle = messageDatabase.getHandleByAccount(messageDatabase.getCurrentAccount());

                                    if (messageDatabase.getContactByHandle(meHandle) == null) {
                                        messageManager.addContact(new Contact(UUID.randomUUID(), null, null, meHandle, null), false);
                                    }
                                    sender = messageDatabase.getContactByHandle(meHandle);
                                } else {
                                    sender = messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(jsonMessage.getHandle()));
                                }


                                String attachmentNamePrefix = new SimpleDateFormat("MM-dd-yy", Locale.US).format(Calendar.getInstance().getTime());
                                ArrayList<Attachment> attachments = new ArrayList<>();

                                for (JSONAttachment jsonAttachment : jsonMessage.getAttachments()) {
                                    Attachment attachment = new Attachment(UUID.randomUUID(), jsonAttachment.getMacGuid(), jsonAttachment.getTransferName(),
                                            new FileLocationContainer(
                                                    new File(DatabaseManager.getInstance(getParentService()).getAttachmentFolder(), attachmentNamePrefix + "-" + jsonAttachment.getTransferName())),
                                            jsonAttachment.getFileType(), jsonAttachment.getTotalBytes());

                                    JSONEncryptedFile jsonEncryptedFile = jsonAttachment.getFileData();
                                    FileDecryptionTask fileDecryptionTask = new FileDecryptionTask(new CryptoFile(jsonEncryptedFile.getEncryptedData(), jsonEncryptedFile.getKey(),
                                            jsonEncryptedFile.getIvParams()), CryptoType.AES);
                                    fileDecryptionTask.runDecryptTask();

                                    attachment.getFileLocation().writeBytesToFile(fileDecryptionTask.getDecryptedBytes());
                                    attachments.add(attachment);
                                }

                                Message message = new Message(UUID.randomUUID(), jsonMessage.getMacGuid(), messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()), sender, attachments,
                                        textDecryptionTask.getDecryptedText(), jsonMessage.getDateSent(), jsonMessage.getDateDelivered(), jsonMessage.getDateRead(), jsonMessage.getErrored(),
                                        jsonMessage.isSent(), jsonMessage.isDelivered(), jsonMessage.isRead(), jsonMessage.isFinished(), jsonMessage.isFromMe());

                                messageManager.addMessage(message, false);
                            }catch(Exception ex){
                                sendLocalBroadcast(weMessage.BROADCAST_NEW_MESSAGE_ERROR, null);
                                AppLogger.error(TAG, "An error occurred while fetching a new message from the server", ex);
                            }
                        }
                    }).start();
                }else if (incoming.startsWith(weMessage.JSON_MESSAGE_UPDATED)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONMessage jsonMessage = (JSONMessage) getIncomingMessage(weMessage.JSON_MESSAGE_UPDATED, incoming).getOutgoing(JSONMessage.class, byteArrayAdapter);
                                MessageManager messageManager = MessageManager.getInstance(getParentService());
                                MessageDatabase messageDatabase = messageManager.getMessageDatabase();

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    if (messageDatabase.getHandleByHandleID(s) == null) {
                                        messageDatabase.addHandle(new Handle(UUID.randomUUID(), s, Handle.HandleType.IMESSAGE));
                                    }
                                }

                                for (String s : jsonMessage.getChat().getParticipants()) {
                                    if (messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(s)) == null) {
                                        messageManager.addContact(new Contact(UUID.randomUUID(), null, null, messageDatabase.getHandleByHandleID(s), null), false);
                                    }
                                }

                                if (StringUtils.isEmpty(jsonMessage.getHandle())) {
                                    Handle meHandle = messageDatabase.getHandleByAccount(messageDatabase.getCurrentAccount());

                                    if (messageDatabase.getContactByHandle(meHandle) == null) {
                                        messageManager.addContact(new Contact(UUID.randomUUID(), null, null, meHandle, null), false);
                                    }
                                }

                                JSONChat jsonChat = jsonMessage.getChat();
                                runChatCheck(messageManager, jsonChat);

                                if (messageDatabase.getMessageByMacGuid(jsonMessage.getMacGuid()) == null){

                                    JSONEncryptedText encryptedText = jsonMessage.getEncryptedText();
                                    DecryptionTask textDecryptionTask = new DecryptionTask(new KeyTextPair(encryptedText.getEncryptedText(), encryptedText.getKey()), CryptoType.AES);
                                    textDecryptionTask.runDecryptTask();
                                    String decryptedText = textDecryptionTask.getDecryptedText();

                                    List<Message> messageCandidates = messageDatabase.getReversedMessagesWithSearchParameters(
                                            messageDatabase.getChatByMacGuid(jsonMessage.getChat().getMacGuid()), decryptedText, jsonMessage.isFromMe(), 0, UPDATE_MESSAGES_ATTEMPT_QUEUE);

                                    boolean updated = false;

                                    for (Message candidate : messageCandidates){
                                        if (candidate.getMacGuid() == null){
                                            updateMessage(messageManager, candidate, jsonMessage, true);
                                            updated = true;
                                            break;
                                        }
                                    }

                                    if (!updated){
                                        sendLocalBroadcast(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR, null);
                                        AppLogger.log(AppLogger.Level.ERROR, TAG, "An error occurred while updating a message with Mac GUID: " + jsonMessage.getMacGuid() +
                                                "  Reason: Previous message not found on system");
                                    }
                                }else {
                                    updateMessage(messageManager, messageDatabase.getMessageByMacGuid(jsonMessage.getMacGuid()), jsonMessage, false);
                                }
                            }catch(Exception ex){
                                sendLocalBroadcast(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR, null);
                                AppLogger.error("An error occurred while updating the message", ex);
                            }
                        }
                    }).start();
                }else if (incoming.startsWith(weMessage.JSON_ACTION)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                //TODO: DO stuff

                            }catch(Exception ex){
                                sendLocalBroadcast(weMessage.BROADCAST_ACTION_PERFORM_ERROR, null);
                                AppLogger.error("An error occurred while performing a JSONAction", ex);
                            }
                        }
                    }).start();
                }else if (incoming.startsWith(weMessage.JSON_RETURN_RESULT)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //TODO: More stuff

                        }
                    }).start();
                }
            }catch(Exception ex){
                Bundle extras = new Bundle();
                extras.putString(weMessage.BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE, getParentService().getString(R.string.connection_error_unknown_message));
                sendLocalBroadcast(weMessage.BROADCAST_DISCONNECT_REASON_ERROR, extras);
                AppLogger.error(TAG, "An unknown error occurred. Dropping connection to weServer", ex);
                getParentService().endService();
            }
        }
    }

    protected void endConnection(){
        if (isRunning.get()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isRunning.set(false);
                    try {
                        sendOutgoingMessage(weMessage.JSON_CONNECTION_TERMINATED, DisconnectReason.CLIENT_DISCONNECTED.getCode(), Integer.class);
                    }catch(Exception ex){
                        AppLogger.error(TAG, "An error occurred while sending disconnect message to the server.", ex);
                    }
                    try {
                        if (isConnected.get()) {
                            isConnected.set(false);
                            getInputStream().close();
                            getOutputStream().close();
                        }
                        getConnectionSocket().close();
                        ConnectionThread.this.interrupt();
                    } catch (Exception ex) {
                        AppLogger.error(TAG, "An error occurred while terminating the connection to the weServer.", ex);
                        ConnectionThread.this.interrupt();
                    }
                }
            }).start();
        }
    }

    private void sendLocalBroadcast(String action, Bundle extras){
        Intent timeoutIntent = new Intent(action);

        if (extras != null) {
            timeoutIntent.putExtras(extras);
        }
        LocalBroadcastManager.getInstance(getParentService()).sendBroadcast(timeoutIntent);
    }

    private void newGroupChat(MessageManager messageManager, JSONChat jsonChat){
        ArrayList<Contact> contactList = new ArrayList<>();

        for (String s : jsonChat.getParticipants()) {
            contactList.add(messageManager.getMessageDatabase().getContactByHandle(messageManager.getMessageDatabase().getHandleByHandleID(s)));
        }
        GroupChat newChat = new GroupChat(UUID.randomUUID(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                true, true, jsonChat.getDisplayName(), contactList);

        messageManager.addChat(newChat, false);
    }

    private void updateGroupChat(MessageManager messageManager, GroupChat existingChat, JSONChat jsonChat, boolean overrideAll){
        MessageDatabase messageDatabase = messageManager.getMessageDatabase();

        ArrayList<String> existingChatParticipantList = new ArrayList<>();
        ArrayList<String> newChatParticipantList = new ArrayList<>(jsonChat.getParticipants());

        for (Contact c : existingChat.getParticipants()) {
            existingChatParticipantList.add(c.getHandle().getHandleID());
        }

        List<String> removedParticipantsList = new ArrayList<>(existingChatParticipantList);
        List<String> addedParticipantsList = new ArrayList<>(newChatParticipantList);

        removedParticipantsList.removeAll(addedParticipantsList);
        addedParticipantsList.removeAll(removedParticipantsList);

        for (String s : addedParticipantsList) {
            messageManager.addParticipantToGroup(existingChat, messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(s)), false);
        }
        for (String s : removedParticipantsList) {
            messageManager.removeParticipantFromGroup(existingChat, messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(s)), false);
        }

        if (!existingChat.getDisplayName().equals(jsonChat.getDisplayName())) {
            messageManager.renameGroupChat(existingChat, jsonChat.getDisplayName(), false);
        }

        if (overrideAll){
            GroupChat updatedChat = (GroupChat) messageDatabase.getChatByUuid(existingChat.getUuid().toString());

            messageManager.updateChat(existingChat.getUuid().toString(), new GroupChat(existingChat.getUuid(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                    updatedChat.isInChat(), updatedChat.hasUnreadMessages(), updatedChat.getDisplayName(), updatedChat.getParticipants()), false);
        }
    }

    private void runChatCheck(MessageManager messageManager, JSONChat jsonChat){
        MessageDatabase messageDatabase = messageManager.getMessageDatabase();

        if (messageDatabase.getChatByMacGuid(jsonChat.getMacGuid()) == null) {
            if (jsonChat.getDisplayName() == null && jsonChat.getParticipants().size() == 1) {

                PeerChat peerChat = messageDatabase.getChatByHandle(messageDatabase.getHandleByHandleID(jsonChat.getParticipants().get(0)));
                if (peerChat != null){

                    PeerChat updatedChat = new PeerChat(peerChat.getUuid(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                            peerChat.isInChat(), peerChat.hasUnreadMessages(), peerChat.getContact());
                    messageManager.updateChat(peerChat.getUuid().toString(), updatedChat, false);

                }else {
                    PeerChat newChat = new PeerChat(UUID.randomUUID(), jsonChat.getMacGuid(), jsonChat.getMacGroupID(), jsonChat.getMacChatIdentifier(),
                            true, true, messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(jsonChat.getParticipants().get(0))));
                    messageManager.addChat(newChat, false);
                }

            } else {
                ArrayList<GroupChat> groupChats = new ArrayList<>(messageDatabase.getGroupChatsWithName(jsonChat.getDisplayName()));

                if (groupChats.size() == 0) {
                    newGroupChat(messageManager, jsonChat);

                } else if (groupChats.size() == 1) {

                    if (groupChats.get(0).getMacGuid() == null) {
                        updateGroupChat(messageManager, groupChats.get(0), jsonChat, true);
                    } else {
                        newGroupChat(messageManager, jsonChat);
                    }
                } else {
                    boolean updated = false;

                    for (GroupChat groupChat : groupChats) {
                        if (groupChat.getMacGuid() == null) {
                            updateGroupChat(messageManager, groupChat, jsonChat, true);
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
                updateGroupChat(messageManager, (GroupChat) existingChat, jsonChat, false);
            }
        }
    }

    private void updateMessage(MessageManager messageManager, Message existingMessage, JSONMessage jsonMessage, boolean overrideAll){
        MessageDatabase messageDatabase = messageManager.getMessageDatabase();
        Message newData = new Message().setUuid(existingMessage.getUuid()).setAttachments(existingMessage.getAttachments()).setText(existingMessage.getText())
                .setDateSent(jsonMessage.getDateSent()).setDateDelivered(jsonMessage.getDateDelivered()).setDateRead(jsonMessage.getDateRead()).setHasErrored(jsonMessage.getErrored())
                .setIsSent(jsonMessage.isSent()).setDelivered(jsonMessage.isDelivered()).setRead(jsonMessage.isRead()).setFinished(jsonMessage.isFinished()).setFromMe(existingMessage.isFromMe());

        if (overrideAll){
            newData.setMacGuid(jsonMessage.getMacGuid()).setChat(messageDatabase.getChatByMacGuid(jsonMessage.getChat().getMacGuid()))
                    .setSender(messageDatabase.getContactByHandle(messageDatabase.getHandleByHandleID(jsonMessage.getHandle())));
        }else {
            newData.setMacGuid(existingMessage.getMacGuid()).setChat(messageDatabase.getChatByUuid(existingMessage.getChat().getUuid().toString()))
                    .setSender(messageDatabase.getContactByHandle(existingMessage.getSender().getHandle()));
        }

        messageManager.updateMessage(existingMessage.getUuid().toString(), newData, false);
    }
}