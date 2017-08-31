package scott.wemessage.server.database;

import com.google.gson.GsonBuilder;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.utils.ByteArrayAdapter;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.events.EventManager;
import scott.wemessage.server.events.database.ServerDatabaseUpdateEvent;
import scott.wemessage.server.security.ServerBase64Wrapper;
import scott.wemessage.server.weMessage;

@SuppressWarnings("Duplicates")
public final class DatabaseManager extends Thread {

    private final String TAG = "weServer Database Service";

    public final String serverDatabaseFileName = weMessage.SERVER_DATABASE_FILE_NAME;

    public final String TABLE_PROPERTIES = "properties";
    public final String TABLE_DEVICES = "devices";
    public final String TABLE_ERRORS = "errors";
    public final String TABLE_QUEUE = "queue";
    public final String TABLE_ACTION_QUEUE = "action_queue";
    public final String TABLE_REGISTRATION_TOKENS = "registration_tokens";

    public final String COLUMN_PROPERTIES_ROWID = "id";
    public final String COLUMN_PROPERTIES_VERSION = "version";

    public final String COLUMN_DEVICE_ROWID = "id";
    public final String COLUMN_DEVICE_ID = "device_id";
    public final String COLUMN_DEVICE_NAME = "device_name";
    public final String COLUMN_DEVICE_ADDRESS = "address";
    
    public final String COLUMN_ERROR_ROWID = "id";
    public final String COLUMN_ERROR_MESSAGE = "errormessage";
    public final String COLUMN_ERRORED_SCRIPT = "script";

    public final String COLUMN_QUEUE_MESSAGE_ROWID = "id";
    public final String COLUMN_QUEUE_MESSAGE_GUID = "guid";
    public final String COLUMN_QUEUE_MESSAGE_DEVICES_WAITING = "devices_waiting";
    public final String COLUMN_QUEUE_MESSAGE_UPDATE = "is_update";

    public final String COLUMN_QUEUE_ACTION_ROWID = "id";
    public final String COLUMN_QUEUE_ACTION_JSON = "action_json";
    public final String COLUMN_QUEUE_ACTION_DEVICES_WAITING = "devices_waiting";

    public final String COLUMN_REGISTRATION_TOKEN_ROWID = "id";
    public final String COLUMN_REGISTRATION_TOKEN_DEVICE_ID = "device_id";
    public final String COLUMN_REGISTRATION_TOKEN_TOKEN = "token";

    private final Object serverDatabaseConnectionLock = new Object();
    private final Object chatDatabaseConnectionLock = new Object();

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isChatDbConnectionOpen = new AtomicBoolean(false);
    private MessageServer messageServer;
    private ServerConfiguration serverConfiguration;
    private Connection serverDatabaseConnection;
    private Connection chatDatabaseConnection;
    private File serverDatabaseFile;
    private String chatDatabaseDirectory;

    public DatabaseManager(MessageServer messageServer, ServerConfiguration serverConfiguration) throws IOException {
        this.messageServer = messageServer;
        this.serverConfiguration = serverConfiguration;

        File dbFile = new File(serverConfiguration.getParentDirectory(), serverDatabaseFileName);

        if(!dbFile.exists()){
            dbFile.createNewFile();
        }
        this.serverDatabaseFile = dbFile;

        try {
            Class.forName("org.sqlite.JDBC");
            synchronized (serverDatabaseConnectionLock) {
                serverDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + serverConfiguration.getParentDirectoryPath() + "/" + serverDatabaseFileName);

                if (serverDatabaseConnection == null) {
                    throw new NullPointerException("The database could not be found.");
                }

                String createPropertiesStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_PROPERTIES
                        + " (" + COLUMN_PROPERTIES_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_PROPERTIES_VERSION + " text );";

                Statement createPropertiesStatement = serverDatabaseConnection.createStatement();
                createPropertiesStatement.execute(createPropertiesStatementString);
                createPropertiesStatement.close();

                String createDevicesStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES
                        + " (" + COLUMN_DEVICE_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_DEVICE_ID + " text, "
                        + COLUMN_DEVICE_ADDRESS + " text, "
                        + COLUMN_DEVICE_NAME + " text );";

                Statement createDevicesStatement = serverDatabaseConnection.createStatement();
                createDevicesStatement.execute(createDevicesStatementString);
                createDevicesStatement.close();

                String createErrorStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_ERRORS
                        + " (" + COLUMN_ERROR_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_ERRORED_SCRIPT + " text, "
                        + COLUMN_ERROR_MESSAGE + " text );";

                Statement createErrorStatement = serverDatabaseConnection.createStatement();
                createErrorStatement.execute(createErrorStatementString);
                createErrorStatement.close();

                String deletePresentErrorsString = "DELETE FROM " + TABLE_ERRORS + " WHERE " + COLUMN_ERROR_ROWID + " > -1;";
                Statement deletePresentErrors = getServerDatabaseConnection().createStatement();

                deletePresentErrors.execute(deletePresentErrorsString);
                deletePresentErrors.close();

                String createQueueStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_QUEUE
                        + " (" + COLUMN_QUEUE_MESSAGE_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_QUEUE_MESSAGE_GUID + " text, "
                        + COLUMN_QUEUE_MESSAGE_UPDATE + " text, "
                        + COLUMN_QUEUE_MESSAGE_DEVICES_WAITING + " text );";

                Statement createQueueStatement = serverDatabaseConnection.createStatement();
                createQueueStatement.execute(createQueueStatementString);
                createQueueStatement.close();

                String createActionQueueStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_ACTION_QUEUE
                        + " (" + COLUMN_QUEUE_ACTION_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_QUEUE_ACTION_JSON + " text, "
                        + COLUMN_QUEUE_ACTION_DEVICES_WAITING + " text );";

                Statement createActionQueueStatement = serverDatabaseConnection.createStatement();
                createActionQueueStatement.execute(createActionQueueStatementString);
                createActionQueueStatement.close();

                String createRegistrationTokensStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_REGISTRATION_TOKENS
                        + " (" + COLUMN_REGISTRATION_TOKEN_ROWID + " integer PRIMARY KEY,  "
                        + COLUMN_REGISTRATION_TOKEN_DEVICE_ID + " text, "
                        + COLUMN_REGISTRATION_TOKEN_TOKEN + " text );";

                Statement createRegistrationTokenStatement = serverDatabaseConnection.createStatement();
                createRegistrationTokenStatement.execute(createRegistrationTokensStatementString);
                createRegistrationTokenStatement.close();
            }

            if (getDatabaseVersion() == -1){
                String insertStatementString = "INSERT INTO " + TABLE_PROPERTIES+ "(" + COLUMN_PROPERTIES_VERSION + ") VALUES (?)";
                PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
                insertStatement.setInt(1, weMessage.WEMESSAGE_DATABASE_VERSION);

                insertStatement.executeUpdate();
                insertStatement.close();
            }

            int localDbVersion = getDatabaseVersion();
            int serverDbVersion = weMessage.WEMESSAGE_DATABASE_VERSION;

            if (localDbVersion != serverDbVersion){
                if (localDbVersion < serverDbVersion) {
                    onUpgrade(serverDbVersion, localDbVersion);
                } else if (localDbVersion > serverDbVersion) {
                    onDowngrade(serverDbVersion, localDbVersion);
                }
            }

        }catch(Exception ex){
            ServerLogger.error(TAG, "An error occurred while connecting to the weServer database. Shutting down!", ex);
            messageServer.shutdown(-1, false);
            return;
        }

        try {
            synchronized (chatDatabaseConnectionLock) {
                this.chatDatabaseDirectory = System.getProperty("user.home") + "/Library/Messages/";
                chatDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + chatDatabaseDirectory + "chat.db");

                if (chatDatabaseConnection == null) {
                    throw new NullPointerException("The database could not be found.");
                }
                isChatDbConnectionOpen.set(true);
            }
        }catch(Exception ex){
            ServerLogger.error(TAG, "An error occurred while connecting to the Messages Chat database. Shutting down!", ex);
            messageServer.shutdown(-1, false);
        }
    }

    public final String getChatDatabaseDirectory(){
        return chatDatabaseDirectory;
    }

    public Connection getServerDatabaseConnection(){
        synchronized (serverDatabaseConnectionLock) {
            return serverDatabaseConnection;
        }
    }

    public Connection getChatDatabaseConnection(){
        boolean loop = true;

        while (loop){
            if (isChatDbConnectionOpen.get()){
                loop = false;
            }
        }
        synchronized (chatDatabaseConnectionLock) {
            return chatDatabaseConnection;
        }
    }

    public List<String> getAllExistingDevices() throws SQLException {
        List<String> devices = new ArrayList<>();
        String selectDevicesStatementString = "SELECT * FROM " + TABLE_DEVICES;
        Statement selectDevicesStatement = getServerDatabaseConnection().createStatement();

        boolean isResultSet = selectDevicesStatement.execute(selectDevicesStatementString);

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectDevicesStatement.getResultSet();
                while(resultSet.next()) {
                    String deviceId = resultSet.getString(COLUMN_DEVICE_ID);
                    devices.add(deviceId);
                }
                resultSet.close();
            } else {
                if(selectDevicesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectDevicesStatement.getMoreResults();
        }
        selectDevicesStatement.close();

        return devices;
    }

    public List<String> getAllRegistrationTokens() throws SQLException {
        List<String> tokens = new ArrayList<>();
        String selectTokensStatementString = "SELECT * FROM " + TABLE_REGISTRATION_TOKENS;
        Statement selectTokensStatement = getServerDatabaseConnection().createStatement();

        boolean isResultSet = selectTokensStatement.execute(selectTokensStatementString);

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectTokensStatement.getResultSet();
                while(resultSet.next()) {
                    String deviceId = resultSet.getString(COLUMN_REGISTRATION_TOKEN_TOKEN);
                    tokens.add(deviceId);
                }
                resultSet.close();
            } else {
                if(selectTokensStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectTokensStatement.getMoreResults();
        }
        selectTokensStatement.close();

        return tokens;
    }

    public String getAddressByDeviceId(String deviceId) throws SQLException {
        String selectStatementString = "SELECT * FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICE_ID + " = ?";
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectStatementString);
        findStatement.setString(1, deviceId);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return null;
        }
        String deviceAddress = resultSet.getString(COLUMN_DEVICE_ADDRESS);

        resultSet.close();
        findStatement.close();

        return deviceAddress;
    }

    public String getNameByDeviceId(String deviceId) throws SQLException {
        String selectStatementString = "SELECT * FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICE_ID + " = ?";
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectStatementString);
        findStatement.setString(1, deviceId);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return null;
        }
        String deviceName = resultSet.getString(COLUMN_DEVICE_NAME);

        resultSet.close();
        findStatement.close();

        return deviceName;
    }

    public String getDeviceIdByName(String deviceName) throws SQLException {
        String selectStatementString = "SELECT * FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICE_NAME + " = ?";
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectStatementString);
        findStatement.setString(1, deviceName);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return null;
        }
        String deviceId = resultSet.getString(COLUMN_DEVICE_ID);

        resultSet.close();
        findStatement.close();

        return deviceId;
    }

    public HashMap<String, Boolean> getQueuedMessages(String deviceId) throws SQLException {
        HashMap<String, Boolean> queue = new HashMap<>();
        String selectAwaitingDevicesStatementString = "SELECT * FROM " + TABLE_QUEUE + " WHERE " + COLUMN_QUEUE_MESSAGE_DEVICES_WAITING + " LIKE ?";
        PreparedStatement selectAwaitingDevicesStatement = getServerDatabaseConnection().prepareStatement(selectAwaitingDevicesStatementString);
        selectAwaitingDevicesStatement.setString(1, "%" + deviceId + "%");

        boolean isResultSet = selectAwaitingDevicesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectAwaitingDevicesStatement.getResultSet();
                while(resultSet.next()) {
                    String messageGuid = resultSet.getString(COLUMN_QUEUE_MESSAGE_GUID);
                    boolean isUpdate = Boolean.parseBoolean(resultSet.getString(COLUMN_QUEUE_MESSAGE_UPDATE));
                    queue.put(messageGuid, isUpdate);
                }
                resultSet.close();
            } else {
                if(selectAwaitingDevicesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectAwaitingDevicesStatement.getMoreResults();
        }
        selectAwaitingDevicesStatement.close();

        return queue;
    }

    public void queueMessage(String messageGuid, boolean update) throws SQLException {
        if (getDisconnectedDevices().isEmpty()) return;

        String insertStatementString = "INSERT INTO " + TABLE_QUEUE + "(" + COLUMN_QUEUE_MESSAGE_GUID + ", " + COLUMN_QUEUE_MESSAGE_UPDATE + ", " + COLUMN_QUEUE_MESSAGE_DEVICES_WAITING + ") VALUES (?, ?, ?)";
        PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
        insertStatement.setString(1, messageGuid);
        insertStatement.setString(2, Boolean.toString(update));
        insertStatement.setString(3, StringUtils.join(getDisconnectedDevices(), ", ", 2));

        insertStatement.executeUpdate();
        insertStatement.close();
    }

    public void unQueueMessage(String messageGuid, String deviceId) throws SQLException {
        String selectQuery = "SELECT * FROM " + TABLE_QUEUE + " WHERE " + COLUMN_QUEUE_MESSAGE_GUID + " = ?";
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectQuery);
        findStatement.setString(1, messageGuid);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return;
        }
        String devicesString = resultSet.getString(COLUMN_QUEUE_MESSAGE_DEVICES_WAITING);
        ArrayList<String> devices = new ArrayList<>(Arrays.asList(devicesString.split(", ")));

        devices.remove(deviceId);

        if (devices.isEmpty()){
            String deleteQueueStatementString = "DELETE FROM " + TABLE_QUEUE + " WHERE " + COLUMN_QUEUE_MESSAGE_GUID + " = ?";
            PreparedStatement deleteQueueStatement = getServerDatabaseConnection().prepareStatement(deleteQueueStatementString);

            deleteQueueStatement.setString(1, messageGuid);
            deleteQueueStatement.executeUpdate();
            deleteQueueStatement.close();
        }else {
            String insertStatementString = "UPDATE " + TABLE_QUEUE + " SET " + COLUMN_QUEUE_MESSAGE_DEVICES_WAITING + " = ? WHERE " + COLUMN_QUEUE_MESSAGE_GUID + " = ?";
            PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
            insertStatement.setString(1, StringUtils.join(devices, ", ", 2));
            insertStatement.setString(2, messageGuid);

            insertStatement.executeUpdate();
            insertStatement.close();
        }

        resultSet.close();
        findStatement.close();
    }

    public List<JSONAction> getQueuedActions(String deviceId) throws SQLException {
        List<JSONAction> actionQueue = new ArrayList<>();
        String selectAwaitingDevicesStatementString = "SELECT * FROM " + TABLE_ACTION_QUEUE + " WHERE " + COLUMN_QUEUE_ACTION_DEVICES_WAITING + " LIKE ?";
        PreparedStatement selectAwaitingDevicesStatement = getServerDatabaseConnection().prepareStatement(selectAwaitingDevicesStatementString);
        selectAwaitingDevicesStatement.setString(1, "%" + deviceId + "%");

        boolean isResultSet = selectAwaitingDevicesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectAwaitingDevicesStatement.getResultSet();
                while(resultSet.next()) {
                    String actionJSON = resultSet.getString(COLUMN_QUEUE_ACTION_JSON);
                    JSONAction jsonAction = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayAdapter(new ServerBase64Wrapper())).create().fromJson(actionJSON, JSONAction.class);
                    actionQueue.add(jsonAction);
                }
                resultSet.close();
            } else {
                if(selectAwaitingDevicesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectAwaitingDevicesStatement.getMoreResults();
        }
        selectAwaitingDevicesStatement.close();

        return actionQueue;
    }

    public void queueAction(JSONAction jsonAction) throws SQLException {
        if (getDisconnectedDevices().isEmpty()) return;

        String insertStatementString = "INSERT INTO " + TABLE_ACTION_QUEUE + "(" + COLUMN_QUEUE_ACTION_JSON + ", " + COLUMN_QUEUE_ACTION_DEVICES_WAITING + ") VALUES (?, ?)";
        PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
        insertStatement.setString(1, new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayAdapter(new ServerBase64Wrapper())).create().toJson(jsonAction));
        insertStatement.setString(2, StringUtils.join(getDisconnectedDevices(), ", ", 2));

        insertStatement.executeUpdate();
        insertStatement.close();
    }

    public void unQueueAction(JSONAction jsonAction, String deviceId) throws SQLException {
        String selectQuery = "SELECT * FROM " + TABLE_ACTION_QUEUE + " WHERE " + COLUMN_QUEUE_ACTION_JSON + " = ?";
        String actionJson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayAdapter(new ServerBase64Wrapper())).create().toJson(jsonAction);
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectQuery);
        findStatement.setString(1, actionJson);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return;
        }
        String devicesString = resultSet.getString(COLUMN_QUEUE_ACTION_DEVICES_WAITING);
        ArrayList<String> devices = new ArrayList<>(Arrays.asList(devicesString.split(", ")));

        devices.remove(deviceId);

        if (devices.isEmpty()){
            String deleteQueueStatementString = "DELETE FROM " + TABLE_ACTION_QUEUE + " WHERE " + COLUMN_QUEUE_ACTION_JSON + " = ?";
            PreparedStatement deleteQueueStatement = getServerDatabaseConnection().prepareStatement(deleteQueueStatementString);

            deleteQueueStatement.setString(1, actionJson);
            deleteQueueStatement.executeUpdate();
            deleteQueueStatement.close();
        }else {
            String insertStatementString = "UPDATE " + TABLE_ACTION_QUEUE + " SET " + COLUMN_QUEUE_ACTION_DEVICES_WAITING + " = ? WHERE " + COLUMN_QUEUE_ACTION_JSON + " = ?";
            PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
            insertStatement.setString(1, StringUtils.join(devices, ", ", 2));
            insertStatement.setString(2, actionJson);

            insertStatement.executeUpdate();
            insertStatement.close();
        }

        resultSet.close();
        findStatement.close();
    }

    public void setRegistrationToken(String deviceId, String token) throws SQLException {
        String selectQuery = "SELECT * FROM " + TABLE_REGISTRATION_TOKENS + " WHERE " + COLUMN_REGISTRATION_TOKEN_DEVICE_ID + " = ?";
        PreparedStatement findStatement = getServerDatabaseConnection().prepareStatement(selectQuery);
        findStatement.setString(1, deviceId);
        ResultSet resultSet = findStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            String insertStatementString = "INSERT INTO " + TABLE_REGISTRATION_TOKENS + "(" + COLUMN_REGISTRATION_TOKEN_DEVICE_ID + ", " + COLUMN_REGISTRATION_TOKEN_TOKEN + ") VALUES (?, ?)";
            PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
            insertStatement.setString(1, deviceId);
            insertStatement.setString(2, token);

            insertStatement.executeUpdate();
            insertStatement.close();
        }else {
            String insertStatementString = "UPDATE " + TABLE_REGISTRATION_TOKENS + " SET " + COLUMN_REGISTRATION_TOKEN_TOKEN + " = ? WHERE " + COLUMN_REGISTRATION_TOKEN_DEVICE_ID + " = ?";
            PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
            insertStatement.setString(1, token);
            insertStatement.setString(2, deviceId);

            insertStatement.executeUpdate();
            insertStatement.close();
        }
    }

    public void deleteDevice(String deviceId) throws SQLException {
        for (String guid : getQueuedMessages(deviceId).keySet()){
            unQueueMessage(guid, deviceId);
        }

        for (JSONAction action : getQueuedActions(deviceId)){
            unQueueAction(action, deviceId);
        }

        String deleteRegistrationKeyStatementString = "DELETE FROM " + TABLE_REGISTRATION_TOKENS + " WHERE " + COLUMN_REGISTRATION_TOKEN_DEVICE_ID + " = ?";
        PreparedStatement deleteRegistrationKeyStatement = getServerDatabaseConnection().prepareStatement(deleteRegistrationKeyStatementString);

        deleteRegistrationKeyStatement.setString(1, deviceId);
        deleteRegistrationKeyStatement.executeUpdate();
        deleteRegistrationKeyStatement.close();

        String deleteDeviceStatementString = "DELETE FROM " + TABLE_DEVICES + " WHERE " + COLUMN_DEVICE_ID + " = ?";
        PreparedStatement deleteDeviceStatement = getServerDatabaseConnection().prepareStatement(deleteDeviceStatementString);

        deleteDeviceStatement.setString(1, deviceId);
        deleteDeviceStatement.executeUpdate();
        deleteDeviceStatement.close();
    }

    public void reloadChatDatabaseConnection() throws SQLException {
        isChatDbConnectionOpen.set(false);

        synchronized (chatDatabaseConnectionLock){
            chatDatabaseConnection.close();
            chatDatabaseConnection = null;
            chatDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + chatDatabaseDirectory + "chat.db");

            if (chatDatabaseConnection == null) {
                throw new NullPointerException("The database could not be found.");
            }
            isChatDbConnectionOpen.set(true);
        }
    }

    public void run() {
        isRunning.set(true);

        ServerLogger.log(ServerLogger.Level.INFO, TAG, "Database Service has started");

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = FileSystems.getDefault().getPath(serverConfiguration.getParentDirectoryPath()).register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
            while (isRunning.get()) {
                final WatchKey wk = watchService.take();

                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();

                    if (changed.endsWith(serverDatabaseFileName)) {
                        EventManager eventManager = messageServer.getEventManager();
                        eventManager.callEvent(new ServerDatabaseUpdateEvent(eventManager, this));
                    }
                }
                boolean valid = wk.reset();
                if (!valid) {
                    ServerLogger.log(ServerLogger.Level.INFO, TAG, "The watcher key has been unregistered");
                }
            }
        }catch(Exception ex){
            if (isRunning.get()) {
                ServerLogger.error(TAG, "An error occurred while watching the weServer database. Shutting down!", ex);
                messageServer.shutdown(-1, false);
            }
        }
    }

    public void stopService(){
        if (isRunning.get()){
            isRunning.set(false);
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "Database Manager is shutting down");

            try {
                getServerDatabaseConnection().close();
                getChatDatabaseConnection().close();
                isChatDbConnectionOpen.set(false);
            }catch(Exception ex){
                ServerLogger.error(TAG, "An error occurred while shutting down the database manager", ex);
            }
        }
    }

    private List<String> getDisconnectedDevices() throws SQLException {
        List<String>allDevices = getAllExistingDevices();
        List<String> disconnectedDevices = new ArrayList<>();

        for (String deviceId : allDevices){
            if (messageServer.getDeviceManager().getDeviceById(deviceId) == null){
                disconnectedDevices.add(deviceId);
            }
        }
        return disconnectedDevices;
    }

    private int getDatabaseVersion() throws SQLException {
        String selectStatementString = "SELECT * FROM " + TABLE_PROPERTIES;
        Statement findStatement = getServerDatabaseConnection().createStatement();
        ResultSet resultSet = findStatement.executeQuery(selectStatementString);

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            findStatement.close();
            return -1;
        }
        int version = resultSet.getInt(COLUMN_PROPERTIES_VERSION);

        resultSet.close();
        findStatement.close();

        return version;
    }

    private void onUpgrade(int newVersion, int oldVersion) throws SQLException {
        refreshProperties(newVersion);

        if (oldVersion == 1){
            //Note: Check ServerConfiguration for more info
        }

        if (oldVersion == 2){

        }
    }

    private void onDowngrade(int newVersion, int oldVersion) throws SQLException {
        refreshProperties(newVersion);

        if (newVersion == 1){

        }

        if (newVersion == 2){

        }
    }

    private void refreshProperties(int newVersion) throws SQLException {
        String dropStatementString = "DROP TABLE IF EXISTS " + TABLE_PROPERTIES;
        Statement dropStatement = getServerDatabaseConnection().createStatement();
        dropStatement.executeUpdate(dropStatementString);
        dropStatement.close();

        String createPropertiesStatementString = "CREATE TABLE IF NOT EXISTS " + TABLE_PROPERTIES
                + " (" + COLUMN_PROPERTIES_ROWID + " integer PRIMARY KEY,  "
                + COLUMN_PROPERTIES_VERSION + " text );";

        Statement createPropertiesStatement = serverDatabaseConnection.createStatement();
        createPropertiesStatement.execute(createPropertiesStatementString);
        createPropertiesStatement.close();

        String insertStatementString = "INSERT INTO " + TABLE_PROPERTIES+ "(" + COLUMN_PROPERTIES_VERSION + ") VALUES (?)";
        PreparedStatement insertStatement = getServerDatabaseConnection().prepareStatement(insertStatementString);
        insertStatement.setInt(1, newVersion);

        insertStatement.executeUpdate();
        insertStatement.close();
    }
}