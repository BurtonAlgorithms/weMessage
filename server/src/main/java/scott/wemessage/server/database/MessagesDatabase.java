package scott.wemessage.server.database;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.events.EventManager;
import scott.wemessage.server.events.database.MessagesDatabaseUpdateEvent;
import scott.wemessage.server.messages.Attachment;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.ChatBase;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;

@SuppressWarnings({"Duplicates", "FieldCanBeLocal", "WeakerAccess"})
public final class MessagesDatabase extends Thread {

    private final String TAG = "Messages Database Service";
    
    private final Object databaseManagerLock = new Object();
    private final Object lastDatabaseSnapshotLock = new Object();

    private MessageServer messageServer;
    private DatabaseManager databaseManager;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private DatabaseSnapshot lastDatabaseSnapshot;

    public final long MESSAGE_COUNT_LIMIT = 200;

    public final String ATTACHMENT_TABLE = "attachment";
    public final String CHAT_TABLE = "chat";
    public final String CHAT_HANDLES_TABLE = "chat_handle_join";
    public final String CHAT_MESSAGE_JOIN_TABLE = "chat_message_join";
    public final String HANDLE_TABLE = "handle";
    public final String MESSAGE_TABLE = "message";
    public final String MESSAGE_ATTACHMENT_TABLE = "message_attachment_join";

    public final String COLUMN_ATTACHMENT_ROWID = "ROWID";
    public final String COLUMN_ATTACHMENT_GUID = "guid";
    public final String COLUMN_ATTACHMENT_CREATED_DATE = "created_date";
    public final String COLUMN_ATTACHMENT_FILENAME = "filename";
    public final String COLUMN_ATTACHMENT_FILETYPE = "mime_type";
    public final String COLUMN_ATTACHMENT_TRANSFER_NAME = "transfer_name";
    public final String COLUMN_ATTACHMENT_BYTES = "total_bytes";

    public final String COLUMN_MESSAGE_ATTACHMENT_MESSAGE_ID = "message_id";
    public final String COLUMN_MESSAGE_ATTACHMENT_ATTACHMENT_ID = "attachment_id";

    public final String COLUMN_HANDLE_ROWID = "ROWID";
    public final String COLUMN_HANDLE_HANDLE_ID = "id";
    public final String COLUMN_HANDLE_COUNTRY = "country";

    public final String COLUMN_CHAT_ROWID = "ROWID";
    public final String COLUMN_CHAT_GUID = "guid";
    public final String COLUMN_CHAT_IDENTIFIER = "chat_identifier";
    public final String COLUMN_CHAT_ROOMNAME = "room_name";
    public final String COLUMN_CHAT_DISPLAY_NAME = "display_name";
    public final String COLUMN_CHAT_GROUP_ID = "group_id";
    public final String COLUMN_CHAT_ACCOUNT = "account_login";

    public final String COLUMN_CHAT_HANDLE_CHAT_ID = "chat_id";
    public final String COLUMN_CHAT_HANDLE_HANDLE_ID = "handle_id";

    public final String COLUMN_MESSAGE_ROWID = "ROWID";
    public final String COLUMN_MESSAGE_GUID = "guid";
    public final String COLUMN_MESSAGE_TEXT = "text";
    public final String COLUMN_MESSAGE_HANDLE_ID = "handle_id";
    public final String COLUMN_MESSAGE_DATE_SENT = "date";
    public final String COLUMN_MESSAGE_DATE_DELIVERED = "date_delivered";
    public final String COLUMN_MESSAGE_DATE_READ = "date_read";
    public final String COLUMN_MESSAGE_ERROR = "error";
    public final String COLUMN_MESSAGE_IS_SENT = "is_sent";
    public final String COLUMN_MESSAGE_IS_DELIVERED = "is_delivered";
    public final String COLUMN_MESSAGE_IS_READ = "is_read";
    public final String COLUMN_MESSAGE_IS_FINISHED = "is_finished";
    public final String COLUMN_MESSAGE_IS_FROM_ME = "is_from_me";
    public final String COLUMN_MESSAGE_ACCOUNT = "account";
    public final String COLUMN_MESSAGE_STYLE_ID = "expressive_send_style_id";

    public final String COLUMN_CHAT_MESSAGE_CHAT_ID = "chat_id";
    public final String COLUMN_CHAT_MESSAGE_MESSAGE_ID = "message_id";

    public MessagesDatabase(MessageServer messageServer, DatabaseManager databaseManager){
        this.messageServer = messageServer;
        this.databaseManager = databaseManager;
    }
    
    public DatabaseManager getDatabaseManager(){
        synchronized (databaseManagerLock){
            return databaseManager;
        }
    }

    public void run(){
        ServerLogger.log(ServerLogger.Level.INFO, TAG, "Connecting to Messages Database");

        try {
            isRunning.set(true);

            synchronized (lastDatabaseSnapshotLock) {
                lastDatabaseSnapshot = new DatabaseSnapshot(getMessagesByAmount(MESSAGE_COUNT_LIMIT));
            }

            final List<String> accounts = getAccounts();
            final String account = messageServer.getConfiguration().getAccountEmail().toLowerCase();

            if (!accounts.contains(account)){
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ServerLogger.emptyLine();
                        ServerLogger.log("There are no recent messages that are associated with iMessage account: " + account);
                        ServerLogger.log("Are you sure you entered the right e-mail address? If not, you will not be able to receive messages.");
                        ServerLogger.emptyLine();
                        ServerLogger.log("Recently Used iMessage Accounts:");
                        ServerLogger.emptyLine();

                        int i = 0;

                        for (String s : accounts){
                            i++;
                            ServerLogger.log("Account " + i + ". " + s);
                        }
                        ServerLogger.emptyLine();
                    }
                }, 1000L);
            }

        }catch(Exception ex){
            ServerLogger.error(TAG, "An error occurred while connecting to the Messages Database. Shutting down", ex);
            messageServer.shutdown(-1, false);
        }

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = FileSystems.getDefault().getPath(getDatabaseManager().getChatDatabaseDirectory()).register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
            while (isRunning.get()) {
                final WatchKey wk = watchService.take();

                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    EventManager eventManager = messageServer.getEventManager();
                    eventManager.callEvent(new MessagesDatabaseUpdateEvent(eventManager, this));
                }

                boolean valid = wk.reset();
                if (!valid) {
                    ServerLogger.log(ServerLogger.Level.INFO, TAG, "The watcher key has been unregistered");
                }
            }
        }catch(Exception ex){
            if (isRunning.get()) {
                ServerLogger.error(TAG, "An error occurred while watching the Messages database. Shutting down!", ex);
                messageServer.shutdown(-1, false);
            }
        }
    }

    public synchronized void stopService() {
        if (isRunning.get()) {
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "Terminating connection to Messages Database");

            isRunning.set(false);
        }
    }

    public DatabaseSnapshot getLastDatabaseSnapshot(){
        synchronized (lastDatabaseSnapshotLock) {
            return lastDatabaseSnapshot;
        }
    }

    public void setLastDatabaseSnapshot(DatabaseSnapshot databaseSnapshot){
        synchronized (lastDatabaseSnapshotLock){
            lastDatabaseSnapshot = databaseSnapshot;
        }
    }

    public long getChatRowPositionByRowId(long rowId) throws SQLException {
        Long rowIdReturn = null;

        String selectChatStatementString = "SELECT " + COLUMN_CHAT_MESSAGE_CHAT_ID + ", MAX(" + COLUMN_CHAT_MESSAGE_MESSAGE_ID + ") FROM " + CHAT_MESSAGE_JOIN_TABLE
                + " INNER JOIN " + MESSAGE_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_ROWID
                + " WHERE " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_TEXT + " IS NOT NULL "
                + " GROUP BY " + COLUMN_CHAT_MESSAGE_CHAT_ID
                + " ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC";

        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);

        boolean isResultSet = selectChatStatement.execute();
        long i = 0L;

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_MESSAGE_CHAT_ID);

                    if (rowId == resultInt){
                        rowIdReturn = i;
                        break;
                    }
                    i++;
                }
                resultSet.close();
            } else {
                if(selectChatStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatStatement.getMoreResults();
        }
        selectChatStatement.close();

        return rowIdReturn;
    }


    public long getChatRowPositionByGuid(String guid) throws SQLException {
        Long rowIdReturn = null;

        String selectChatStatementString = "SELECT " + CHAT_TABLE + "." + COLUMN_CHAT_GUID + ", " + COLUMN_CHAT_MESSAGE_CHAT_ID + ", MAX(" + COLUMN_CHAT_MESSAGE_MESSAGE_ID + ")"
                + " FROM " + CHAT_MESSAGE_JOIN_TABLE
                + " INNER JOIN " + CHAT_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_CHAT_ID + " = " + CHAT_TABLE + "." + COLUMN_CHAT_ROWID
                + " INNER JOIN " + MESSAGE_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_ROWID
                + " WHERE " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_TEXT + " IS NOT NULL "
                + " GROUP BY " + COLUMN_CHAT_MESSAGE_CHAT_ID
                + " ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC";

        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);

        boolean isResultSet = selectChatStatement.execute();
        long i = 0L;

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatStatement.getResultSet();
                while(resultSet.next()) {
                    String resultString = resultSet.getString(COLUMN_CHAT_GUID);

                    if (resultString.equals(guid)){
                        rowIdReturn = i;
                        break;
                    }
                    i++;
                }
                resultSet.close();
            } else {
                if(selectChatStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatStatement.getMoreResults();
        }
        selectChatStatement.close();

        return rowIdReturn;
    }

    public HashMap<Long, Long> getSortedChatsWithRow() throws SQLException {
        HashMap<Long, Long> positionRowIdPair = new HashMap<>();

        String selectChatStatementString = "SELECT " + COLUMN_CHAT_MESSAGE_CHAT_ID + ", MAX(" + COLUMN_CHAT_MESSAGE_MESSAGE_ID + ") FROM " + CHAT_MESSAGE_JOIN_TABLE
                + " INNER JOIN " + MESSAGE_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_ROWID
                + " WHERE " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_TEXT + " IS NOT NULL "
                + " GROUP BY " + COLUMN_CHAT_MESSAGE_CHAT_ID
                + " ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC";

        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);

        boolean isResultSet = selectChatStatement.execute();
        long i = 0L;

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_MESSAGE_CHAT_ID);
                    positionRowIdPair.put(i, resultInt);
                    i++;
                }
                resultSet.close();
            } else {
                if(selectChatStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatStatement.getMoreResults();
        }
        selectChatStatement.close();

        return positionRowIdPair;
    }

    public HashMap<Long, String> getSortedChatsWithGuid() throws SQLException {
        HashMap<Long, String> chatRowGuidPair = new HashMap<>();

        String selectChatStatementString = "SELECT " + CHAT_TABLE + "." + COLUMN_CHAT_GUID + ", " + COLUMN_CHAT_MESSAGE_CHAT_ID + ", MAX(" + COLUMN_CHAT_MESSAGE_MESSAGE_ID + ")"
                + " FROM " + CHAT_MESSAGE_JOIN_TABLE
                + " INNER JOIN " + CHAT_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_CHAT_ID + " = " + CHAT_TABLE + "." + COLUMN_CHAT_ROWID
                + " INNER JOIN " + MESSAGE_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_ROWID
                + " WHERE " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_TEXT + " IS NOT NULL "
                + " GROUP BY " + COLUMN_CHAT_MESSAGE_CHAT_ID
                + " ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC";

        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);

        boolean isResultSet = selectChatStatement.execute();
        long i = 0L;

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatStatement.getResultSet();
                while(resultSet.next()) {
                    String resultString = resultSet.getString(COLUMN_CHAT_GUID);
                    chatRowGuidPair.put(i, resultString);
                    i++;
                }
                resultSet.close();
            } else {
                if(selectChatStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatStatement.getMoreResults();
        }
        selectChatStatement.close();

        return chatRowGuidPair;
    }

    public List<String> getAccounts() throws SQLException {
        List<String> accounts = new ArrayList<>();

        String selectAccountsStatementString = "SELECT " + COLUMN_MESSAGE_ACCOUNT + ", " + COLUMN_MESSAGE_ROWID + " FROM (SELECT " + COLUMN_MESSAGE_ACCOUNT + ", " + COLUMN_MESSAGE_ROWID + " FROM " + MESSAGE_TABLE + " ORDER BY " + COLUMN_MESSAGE_ROWID + " DESC LIMIT 500) ORDER BY " + COLUMN_MESSAGE_ROWID + " ASC";
        Statement selectAccounts = getDatabaseManager().getChatDatabaseConnection().createStatement();

        boolean isResultSet = selectAccounts.execute(selectAccountsStatementString);

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectAccounts.getResultSet();
                while(resultSet.next()) {
                    String accountLogin = resultSet.getString(COLUMN_MESSAGE_ACCOUNT);
                    String account;
                    boolean useContains = false;

                    try {
                        account = accountLogin.split(":")[1];
                    }catch (Exception ex){
                        useContains = true;
                        try {
                            account = accountLogin.split(":")[0];
                        }catch (Exception exc){
                            account = accountLogin;
                        }
                    }

                    if (!StringUtils.isEmpty(account) && !accounts.contains(account.toLowerCase())){
                        accounts.add(account.toLowerCase());
                    }
                }
                resultSet.close();
            } else {
                if(selectAccounts.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectAccounts.getMoreResults();
        }
        selectAccounts.close();

        return accounts;
    }

    public Message getLastMessageSent() throws SQLException {
        String selectStatementString = "SELECT * FROM " + MESSAGE_TABLE + " ORDER BY " + COLUMN_MESSAGE_ROWID + " DESC LIMIT 1";
        Statement selectStatement = getDatabaseManager().getChatDatabaseConnection().createStatement();
        ResultSet resultSet = selectStatement.executeQuery(selectStatementString);
        long rowID = resultSet.getLong(COLUMN_MESSAGE_ROWID);

        resultSet.close();
        selectStatement.close();

        return getMessageByRow(rowID);
    }

    public Message getLastMessageFromChat(ChatBase chat) throws SQLException {
        String selectStatementString = "SELECT * FROM " + CHAT_MESSAGE_JOIN_TABLE + " WHERE " + COLUMN_CHAT_MESSAGE_CHAT_ID + " = ? ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC LIMIT 1";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setLong(1, chat.getRowID());

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        long rowID = resultSet.getLong(COLUMN_CHAT_MESSAGE_MESSAGE_ID);
        Message message = getMessageByRow(rowID);

        resultSet.close();
        selectStatement.close();

        return message;
    }

    public Message getLastNotNullMessageFromChat(ChatBase chat) throws SQLException {
        String selectStatementString = "SELECT * FROM " + CHAT_MESSAGE_JOIN_TABLE
                + " INNER JOIN " + MESSAGE_TABLE + " ON " + CHAT_MESSAGE_JOIN_TABLE + "." + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_ROWID
                + " WHERE " + COLUMN_CHAT_MESSAGE_CHAT_ID + " = ? "
                + " AND " + MESSAGE_TABLE + "." + COLUMN_MESSAGE_TEXT + " IS NOT NULL "
                + " ORDER BY " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " DESC LIMIT 1";

        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setLong(1, chat.getRowID());

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        long rowID = resultSet.getLong(COLUMN_CHAT_MESSAGE_MESSAGE_ID);
        Message message = getMessageByRow(rowID);

        resultSet.close();
        selectStatement.close();

        return message;
    }

    public List<Message> getMessagesByAmount(long amount) throws SQLException {
        List<Message> messages = new ArrayList<>();

        String selectMessagesStatementString = "SELECT * FROM (SELECT * FROM " + MESSAGE_TABLE + " ORDER BY " + COLUMN_MESSAGE_ROWID + " DESC LIMIT ?) ORDER BY " + COLUMN_MESSAGE_ROWID + " ASC";
        PreparedStatement selectMessagesStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectMessagesStatementString);
        selectMessagesStatement.setLong(1, amount);

        boolean isResultSet = selectMessagesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectMessagesStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_MESSAGE_ROWID);
                    Message message = getMessageByRow(resultInt);

                    if (message != null) {
                        messages.add(message);
                    }
                }
                resultSet.close();
            } else {
                if(selectMessagesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectMessagesStatement.getMoreResults();
        }
        selectMessagesStatement.close();

        return messages;
    }

    public List<Message> getMessagesByStartRow(long startRowID) throws SQLException {
        List<Message> messages = new ArrayList<>();

        String selectMessagesStatementString = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_MESSAGE_ROWID + " >= ?";
        PreparedStatement selectMessagesStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectMessagesStatementString);
        selectMessagesStatement.setLong(1, startRowID);

        boolean isResultSet = selectMessagesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectMessagesStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_MESSAGE_ROWID);
                    Message message = getMessageByRow(resultInt);

                    if(message != null) {
                        messages.add(message);
                    }
                }
                resultSet.close();
            } else {
                if(selectMessagesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectMessagesStatement.getMoreResults();
        }
        selectMessagesStatement.close();

        return messages;
    }

    public Handle getLastHandle() throws SQLException {
        String selectStatementString = "SELECT * FROM " + HANDLE_TABLE + " ORDER BY " + COLUMN_HANDLE_ROWID + " DESC LIMIT 1";
        Statement selectStatement = getDatabaseManager().getChatDatabaseConnection().createStatement();
        ResultSet resultSet = selectStatement.executeQuery(selectStatementString);
        long rowID = resultSet.getLong(COLUMN_HANDLE_ROWID);

        resultSet.close();
        selectStatement.close();

        return getHandleByRow(rowID);
    }

    public Handle getHandleByAccount(String account) throws SQLException {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber numberOne = phoneNumberUtil.parse(account, Locale.getDefault().getCountry());
            String numberStringOne = phoneNumberUtil.format(numberOne, PhoneNumberUtil.PhoneNumberFormat.E164);

            Handle handle = getHandleById(numberStringOne);

            if (handle == null){
                Phonenumber.PhoneNumber numberTwo = numberOne.clearCountryCode();
                String numberStringTwo = phoneNumberUtil.format(numberTwo, PhoneNumberUtil.PhoneNumberFormat.E164).substring(2);

                return getHandleById(numberStringTwo);
            }else {
                return handle;
            }
        }catch(Exception ex){
            return getHandleById(account);
        }
    }

    public List<Handle> getHandlesByAmount(long amount) throws SQLException {
        List<Handle> handles = new ArrayList<>();

        String selectHandlesStatementString = "SELECT * FROM (SELECT * FROM " + HANDLE_TABLE + " ORDER BY " + COLUMN_HANDLE_ROWID + " DESC LIMIT ?) ORDER BY " + COLUMN_HANDLE_ROWID + " ASC";
        PreparedStatement selectHandlesStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectHandlesStatementString);
        selectHandlesStatement.setLong(1, amount);

        boolean isResultSet = selectHandlesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectHandlesStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_HANDLE_ROWID);
                    Handle handle = getHandleByRow(resultInt);

                    if(handle != null) {
                        handles.add(handle);
                    }
                }
                resultSet.close();
            } else {
                if(selectHandlesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectHandlesStatement.getMoreResults();
        }
        selectHandlesStatement.close();

        return handles;
    }

    public List<Handle> getHandlesByStartRow(long startRowID) throws SQLException {
        List<Handle> handles = new ArrayList<>();

        String selectHandlesStatementString = "SELECT * FROM " + HANDLE_TABLE + " WHERE " + COLUMN_HANDLE_ROWID + " >= ?";
        PreparedStatement selectHandlesStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectHandlesStatementString);
        selectHandlesStatement.setLong(1, startRowID);

        boolean isResultSet = selectHandlesStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectHandlesStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_HANDLE_ROWID);
                    Handle handle = getHandleByRow(resultInt);

                    if (handle != null) {
                        handles.add(handle);
                    }
                }
                resultSet.close();
            } else {
                if(selectHandlesStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectHandlesStatement.getMoreResults();
        }
        selectHandlesStatement.close();

        return handles;
    }

    public ChatBase getLastChat() throws SQLException {
        String selectStatementString = "SELECT * FROM " + CHAT_TABLE + " ORDER BY " + COLUMN_CHAT_ROWID + " DESC LIMIT 1";
        Statement selectStatement = getDatabaseManager().getChatDatabaseConnection().createStatement();
        ResultSet resultSet = selectStatement.executeQuery(selectStatementString);
        long rowID = resultSet.getLong(COLUMN_CHAT_ROWID);

        resultSet.close();
        selectStatement.close();

        return getChatByRow(rowID);
    }

    public PeerChat getChatByAccount(String handle) throws SQLException {
        ChatBase chatBase;

        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumberOne = phoneNumberUtil.parse(handle, Locale.getDefault().getCountry());

            String phoneNumberString = phoneNumberUtil.format(phoneNumberOne, PhoneNumberUtil.PhoneNumberFormat.E164);
            ChatBase attemptOne = getChatByIdentifier(phoneNumberString);

            if (attemptOne == null){
                Phonenumber.PhoneNumber phoneNumberTwo = phoneNumberOne.clearCountryCode();
                String phoneNumberStringTwo = phoneNumberUtil.format(phoneNumberTwo, PhoneNumberUtil.PhoneNumberFormat.E164).substring(2);
                ChatBase attemptTwo = getChatByIdentifier(phoneNumberStringTwo);

                if (attemptTwo == null){
                    return null;
                }else {
                    chatBase = attemptTwo;
                }
            }else {
                chatBase = attemptOne;
            }
        }catch(Exception ex){
            chatBase = getChatByIdentifier(handle);
        }

        if (chatBase == null) return null;
        if (chatBase instanceof GroupChat) return null;

        return (PeerChat) chatBase;
    }

    public List<GroupChat> getGroupChatsByName(String groupName) throws SQLException {
        List<GroupChat> chats = new ArrayList<>();
        String selectChatStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_DISPLAY_NAME + " = ?";
        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);
        selectChatStatement.setString(1, groupName);

        boolean isResultSet = selectChatStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatStatement.getResultSet();
                while(resultSet.next()) {
                    String resultString = resultSet.getString(COLUMN_CHAT_GUID);
                    ChatBase chat = getChatByGuid(resultString);

                    if (chat != null && chat instanceof GroupChat) {
                        chats.add((GroupChat) chat);
                    }
                }
                resultSet.close();
            } else {
                if(selectChatStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatStatement.getMoreResults();
        }
        selectChatStatement.close();

        return chats;
    }

    public List<ChatBase> getChatsByAmount(long amount) throws SQLException {
        List<ChatBase> chats = new ArrayList<>();

        String selectChatsStatementString = "SELECT * FROM (SELECT * FROM " + CHAT_TABLE + " ORDER BY " + COLUMN_CHAT_ROWID + " DESC LIMIT ?) ORDER BY " + COLUMN_CHAT_ROWID + " ASC";
        PreparedStatement selectChatsStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatsStatementString);
        selectChatsStatement.setLong(1, amount);

        boolean isResultSet = selectChatsStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatsStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_ROWID);
                    ChatBase chat = getChatByRow(resultInt);

                    if (chat != null) {
                        chats.add(chat);
                    }
                }
                resultSet.close();
            } else {
                if(selectChatsStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatsStatement.getMoreResults();
        }
        selectChatsStatement.close();

        return chats;
    }

    public List<ChatBase> getChatsByStartRow(long startRowID) throws SQLException {
        List<ChatBase> chats = new ArrayList<>();

        String selectChatsStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_ROWID + " >= ?";
        PreparedStatement selectChatsStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatsStatementString);
        selectChatsStatement.setLong(1, startRowID);

        boolean isResultSet = selectChatsStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatsStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_ROWID);
                    ChatBase chat = getChatByRow(resultInt);

                    if (chat != null) {
                        chats.add(chat);
                    }
                }
                resultSet.close();
            } else {
                if(selectChatsStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatsStatement.getMoreResults();
        }
        selectChatsStatement.close();

        return chats;
    }

    public Attachment buildAttachment(ResultSet resultSet) throws SQLException {
        Attachment attachment = new Attachment().setGuid(resultSet.getString(COLUMN_ATTACHMENT_GUID)).setRowID(resultSet.getLong(COLUMN_ATTACHMENT_ROWID))
                .setCreatedDate(processTime(resultSet.getDouble(COLUMN_ATTACHMENT_CREATED_DATE))).setFileLocation(resultSet.getString(COLUMN_ATTACHMENT_FILENAME))
                .setTransferName(resultSet.getString(COLUMN_ATTACHMENT_TRANSFER_NAME)).setFileType(resultSet.getString(COLUMN_ATTACHMENT_FILETYPE))
                .setTotalBytes(resultSet.getLong(COLUMN_ATTACHMENT_BYTES));
        return attachment;
    }

    public Attachment getAttachmentByRow(long rowID) throws SQLException {
        String selectStatementString = "SELECT * FROM " + ATTACHMENT_TABLE + " WHERE " + COLUMN_ATTACHMENT_ROWID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setLong(1, rowID);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Attachment attachment = buildAttachment(resultSet);

        resultSet.close();
        selectStatement.close();
        return attachment;
    }

    public Attachment getAttachmentByGuid(String guid) throws SQLException {
        String selectStatementString = "SELECT * FROM " + ATTACHMENT_TABLE + " WHERE " + COLUMN_ATTACHMENT_GUID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setString(1, guid);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Attachment attachment = buildAttachment(resultSet);

        resultSet.close();
        selectStatement.close();
        return attachment;
    }

    public Handle buildHandle(ResultSet resultSet) throws SQLException {
        Handle handle = new Handle().setRowID(resultSet.getLong(COLUMN_HANDLE_ROWID)).setHandleID(resultSet.getString(COLUMN_HANDLE_HANDLE_ID))
                .setCountry(resultSet.getString(COLUMN_HANDLE_COUNTRY));
        return handle;
    }

    public Handle getHandleByRow(long rowID) throws SQLException {
        String selectStatementString = "SELECT * FROM " + HANDLE_TABLE + " WHERE " + COLUMN_HANDLE_ROWID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setLong(1, rowID);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Handle handle = buildHandle(resultSet);

        resultSet.close();
        selectStatement.close();
        return handle;
    }

    public Handle getHandleById(String handleID) throws SQLException {
        String selectStatementString = "SELECT * FROM " + HANDLE_TABLE + " WHERE " + COLUMN_HANDLE_HANDLE_ID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setString(1, handleID);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Handle handle = buildHandle(resultSet);

        resultSet.close();
        selectStatement.close();
        return handle;
    }

    public ChatBase buildChat(ResultSet chatResultSet, List<Handle> handles) throws SQLException {
        String accountLogin = chatResultSet.getString(COLUMN_CHAT_ACCOUNT);
        String accountEmail;
        boolean useContains = false;

        try {
            accountEmail = accountLogin.split(":")[1];
        }catch (Exception ex){
            useContains = true;
            try {
                accountEmail = accountLogin.split(":")[0];
            }catch (Exception exc){
                accountEmail = accountLogin;
            }
        }

        try {
            if (!accountEmail.equalsIgnoreCase(messageServer.getConfiguration().getConfigJSON().getConfig().getAccountInfo().getEmail())){
                if (useContains){
                    if (!accountEmail.toLowerCase().contains(messageServer.getConfiguration().getConfigJSON().getConfig().getAccountInfo().getEmail().toLowerCase())){
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } catch(IOException ex){
            ServerLogger.error(TAG, "An error occurred while fetching the server configuration", ex);
            return null;
        }

        ChatBase chat;

        if (chatResultSet.getString(COLUMN_CHAT_ROOMNAME) == null){
            chat = new PeerChat().setPeer(handles.get(0))
                    .setGuid(chatResultSet.getString(COLUMN_CHAT_GUID)).setRowID(chatResultSet.getLong(COLUMN_CHAT_ROWID))
                    .setGroupID(chatResultSet.getString(COLUMN_CHAT_GROUP_ID)).setChatIdentifier(chatResultSet.getString(COLUMN_CHAT_IDENTIFIER));
        }else {
            chat = new GroupChat().setParticipants(handles).setDisplayName(chatResultSet.getString(COLUMN_CHAT_DISPLAY_NAME))
                    .setGuid(chatResultSet.getString(COLUMN_CHAT_GUID)).setRowID(chatResultSet.getLong(COLUMN_CHAT_ROWID))
                    .setGroupID(chatResultSet.getString(COLUMN_CHAT_GROUP_ID)).setChatIdentifier(chatResultSet.getString(COLUMN_CHAT_IDENTIFIER));
        }
        return chat;
    }

    public ChatBase getChatByRow(long rowID) throws SQLException {
        String selectChatStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_ROWID + " = ?";
        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);
        selectChatStatement.setLong(1, rowID);

        ResultSet chatResultSet = selectChatStatement.executeQuery();

        if (!chatResultSet.isBeforeFirst()){
            chatResultSet.close();
            selectChatStatement.close();
            return null;
        }

        List<Handle> handles = new ArrayList<>();
        String selectChatHandleStatementString = "SELECT * FROM " + CHAT_HANDLES_TABLE + " WHERE " + COLUMN_CHAT_HANDLE_CHAT_ID + " = ?";
        PreparedStatement selectChatHandleStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatHandleStatementString);
        selectChatHandleStatement.setLong(1, rowID);

        boolean isResultSet = selectChatHandleStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatHandleStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_HANDLE_HANDLE_ID);
                    handles.add(getHandleByRow(resultInt));
                }
                resultSet.close();
            } else {
                if(selectChatHandleStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatHandleStatement.getMoreResults();
        }

        ChatBase chat = buildChat(chatResultSet, handles);

        chatResultSet.close();
        selectChatStatement.close();
        selectChatHandleStatement.close();

        return chat;
    }

    public ChatBase getChatByGuid(String guid) throws SQLException {
        String selectChatStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_GUID + " = ?";
        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);
        selectChatStatement.setString(1, guid);

        ResultSet chatResultSet = selectChatStatement.executeQuery();

        if (!chatResultSet.isBeforeFirst()){
            chatResultSet.close();
            selectChatStatement.close();
            return null;
        }

        List<Handle> handles = new ArrayList<>();
        String selectChatHandleStatementString = "SELECT * FROM " + CHAT_HANDLES_TABLE + " WHERE " + COLUMN_CHAT_HANDLE_CHAT_ID + " = ?";
        PreparedStatement selectChatHandleStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatHandleStatementString);
        selectChatHandleStatement.setLong(1, chatResultSet.getLong(COLUMN_CHAT_ROWID));

        boolean isResultSet = selectChatHandleStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatHandleStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_HANDLE_HANDLE_ID);
                    handles.add(getHandleByRow(resultInt));
                }
                resultSet.close();
            } else {
                if(selectChatHandleStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatHandleStatement.getMoreResults();
        }

        ChatBase chat = buildChat(chatResultSet, handles);

        chatResultSet.close();
        selectChatStatement.close();
        selectChatHandleStatement.close();

        return chat;
    }

    public ChatBase getChatByGroupID(String groupID) throws SQLException {
        String selectChatStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_GROUP_ID + " = ?";
        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);
        selectChatStatement.setString(1, groupID);

        ResultSet chatResultSet = selectChatStatement.executeQuery();

        if (!chatResultSet.isBeforeFirst()){
            chatResultSet.close();
            selectChatStatement.close();
            return null;
        }

        List<Handle> handles = new ArrayList<>();
        String selectChatHandleStatementString = "SELECT * FROM " + CHAT_HANDLES_TABLE + " WHERE " + COLUMN_CHAT_HANDLE_CHAT_ID + " = ?";
        PreparedStatement selectChatHandleStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatHandleStatementString);
        selectChatHandleStatement.setLong(1, chatResultSet.getLong(COLUMN_CHAT_ROWID));

        boolean isResultSet = selectChatHandleStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatHandleStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_HANDLE_HANDLE_ID);
                    handles.add(getHandleByRow(resultInt));
                }
                resultSet.close();
            } else {
                if(selectChatHandleStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatHandleStatement.getMoreResults();
        }

        ChatBase chat = buildChat(chatResultSet, handles);

        chatResultSet.close();
        selectChatStatement.close();
        selectChatHandleStatement.close();

        return chat;
    }

    public ChatBase getChatByIdentifier(String chatIdentifier) throws SQLException {
        String selectChatStatementString = "SELECT * FROM " + CHAT_TABLE + " WHERE " + COLUMN_CHAT_IDENTIFIER + " = ?";
        PreparedStatement selectChatStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatStatementString);
        selectChatStatement.setString(1, chatIdentifier);

        ResultSet chatResultSet = selectChatStatement.executeQuery();

        if (!chatResultSet.isBeforeFirst()){
            chatResultSet.close();
            selectChatStatement.close();
            return null;
        }

        List<Handle> handles = new ArrayList<>();
        String selectChatHandleStatementString = "SELECT * FROM " + CHAT_HANDLES_TABLE + " WHERE " + COLUMN_CHAT_HANDLE_CHAT_ID + " = ?";
        PreparedStatement selectChatHandleStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatHandleStatementString);
        selectChatHandleStatement.setLong(1, chatResultSet.getLong(COLUMN_CHAT_ROWID));

        boolean isResultSet = selectChatHandleStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet resultSet = selectChatHandleStatement.getResultSet();
                while(resultSet.next()) {
                    long resultInt = resultSet.getLong(COLUMN_CHAT_HANDLE_HANDLE_ID);
                    handles.add(getHandleByRow(resultInt));
                }
                resultSet.close();
            } else {
                if(selectChatHandleStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectChatHandleStatement.getMoreResults();
        }

        ChatBase chat = buildChat(chatResultSet, handles);

        chatResultSet.close();
        selectChatStatement.close();
        selectChatHandleStatement.close();

        return chat;
    }

    public Message buildMessage(ResultSet resultSet, ChatBase chat, Handle handle, List<Attachment> attachments) throws SQLException {
        String accountLogin = resultSet.getString(COLUMN_MESSAGE_ACCOUNT);
        String accountEmail;
        boolean useContains = false;

        if (accountLogin == null){
            accountEmail = "nullLogin";
        }else {
            try {
                accountEmail = accountLogin.split(":")[1];
            }catch (Exception ex){
                useContains = true;
                try {
                    accountEmail = accountLogin.split(":")[0];
                }catch (Exception exc){
                    accountEmail = accountLogin;
                }
            }
        }

        try {
            if (!accountEmail.equalsIgnoreCase(messageServer.getConfiguration().getConfigJSON().getConfig().getAccountInfo().getEmail())){
                if (useContains){
                    if (!accountEmail.toLowerCase().contains(messageServer.getConfiguration().getConfigJSON().getConfig().getAccountInfo().getEmail().toLowerCase())){
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }catch(IOException ex){
            ServerLogger.error(TAG, "An error occurred while fetching the server configuration", ex);
            return null;
        }

        Message message = new Message();

        message.setChat(chat).setHandle(handle).setAttachments(attachments);

        boolean isErrored = resultSet.getInt(COLUMN_MESSAGE_ERROR) != 0;
        boolean isSent = resultSet.getInt(COLUMN_MESSAGE_IS_SENT) == 1;
        boolean isDelivered = resultSet.getInt(COLUMN_MESSAGE_IS_DELIVERED) == 1;
        boolean isRead = resultSet.getInt(COLUMN_MESSAGE_IS_READ) == 1;
        boolean isFinished = resultSet.getInt(COLUMN_MESSAGE_IS_FINISHED) == 1;
        boolean isFromMe = resultSet.getInt(COLUMN_MESSAGE_IS_FROM_ME) == 1;
        Long dateSent = processTime(resultSet.getDouble(COLUMN_MESSAGE_DATE_SENT));
        Long dateDelivered = processTime(resultSet.getDouble(COLUMN_MESSAGE_DATE_DELIVERED));
        Long dateRead = processTime(resultSet.getDouble(COLUMN_MESSAGE_DATE_READ));

        if (dateSent == 0){
            dateSent = null;
        }
        if (dateDelivered == 0){
            dateDelivered = null;
        }
        if (dateRead == 0){
            dateRead = null;
        }

        try {
            message.setMessageEffect(MessageEffect.from(resultSet.getString(COLUMN_MESSAGE_STYLE_ID)));
        }catch (Exception ex){
            message.setMessageEffect(MessageEffect.NONE);
        }

        message.setGuid(resultSet.getString(COLUMN_MESSAGE_GUID)).setRowID(resultSet.getLong(COLUMN_MESSAGE_ROWID))
                .setText(resultSet.getString(COLUMN_MESSAGE_TEXT)).setDateSent(dateSent).setDateDelivered(dateDelivered)
                .setDateRead(dateRead).setErrored(isErrored).setSent(isSent).setDelivered(isDelivered).setRead(isRead)
                .setFinished(isFinished).setFromMe(isFromMe);

        return message;
    }

    public Message getMessageByRow(long rowID) throws SQLException {
        String selectStatementString = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_MESSAGE_ROWID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setLong(1, rowID);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Handle handle = getHandleByRow(resultSet.getLong(COLUMN_MESSAGE_HANDLE_ID));
        long messageRow = resultSet.getLong(COLUMN_MESSAGE_ROWID);

        String selectChatMessageStatementString = "SELECT * FROM " + CHAT_MESSAGE_JOIN_TABLE + " WHERE " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = ?";
        PreparedStatement selectChatMessageStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatMessageStatementString);
        selectChatMessageStatement.setLong(1, messageRow);

        ResultSet resultChatMessageSet = selectChatMessageStatement.executeQuery();

        ChatBase chat;
        try {
            long theResultInt = resultChatMessageSet.getLong(COLUMN_CHAT_MESSAGE_CHAT_ID);
            chat = getChatByRow(theResultInt);
        }catch(Exception ex){
            return null;
        }

        List<Attachment> attachments = new ArrayList<>();
        String selectMessageAttachmentString = "SELECT * FROM " + MESSAGE_ATTACHMENT_TABLE + " WHERE " + COLUMN_MESSAGE_ATTACHMENT_MESSAGE_ID + " = ?";
        PreparedStatement selectMessageAttachmentStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectMessageAttachmentString);
        selectMessageAttachmentStatement.setLong(1, messageRow);

        boolean isResultSet = selectMessageAttachmentStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet theResultSet = selectMessageAttachmentStatement.getResultSet();
                while(theResultSet.next()) {
                    long resultInt = theResultSet.getLong(COLUMN_MESSAGE_ATTACHMENT_ATTACHMENT_ID);
                    attachments.add(getAttachmentByRow(resultInt));
                }
                theResultSet.close();
            } else {
                if(selectMessageAttachmentStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectMessageAttachmentStatement.getMoreResults();
        }

        Message message = buildMessage(resultSet, chat, handle, attachments);

        resultSet.close();
        resultChatMessageSet.close();
        selectStatement.close();
        selectChatMessageStatement.close();
        selectMessageAttachmentStatement.close();

        return message;
    }

    public Message getMessageByGuid(String guid) throws SQLException {
        String selectStatementString = "SELECT * FROM " + MESSAGE_TABLE + " WHERE " + COLUMN_MESSAGE_GUID + " = ?";
        PreparedStatement selectStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectStatementString);
        selectStatement.setString(1, guid);

        ResultSet resultSet = selectStatement.executeQuery();

        if (!resultSet.isBeforeFirst()){
            resultSet.close();
            selectStatement.close();
            return null;
        }
        Handle handle = getHandleByRow(resultSet.getLong(COLUMN_MESSAGE_HANDLE_ID));
        long messageRow = resultSet.getLong(COLUMN_MESSAGE_ROWID);

        String selectChatMessageStatementString = "SELECT * FROM " + CHAT_MESSAGE_JOIN_TABLE + " WHERE " + COLUMN_CHAT_MESSAGE_MESSAGE_ID + " = ?";
        PreparedStatement selectChatMessageStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectChatMessageStatementString);
        selectChatMessageStatement.setLong(1, messageRow);

        ResultSet resultChatMessageSet = selectChatMessageStatement.executeQuery();

        ChatBase chat;
        try {
            long theResultInt = resultChatMessageSet.getLong(COLUMN_CHAT_MESSAGE_CHAT_ID);
            chat = getChatByRow(theResultInt);
        }catch(Exception ex){
            return null;
        }

        List<Attachment> attachments = new ArrayList<>();
        String selectMessageAttachmentString = "SELECT * FROM " + MESSAGE_ATTACHMENT_TABLE + " WHERE " + COLUMN_MESSAGE_ATTACHMENT_MESSAGE_ID + " = ?";
        PreparedStatement selectMessageAttachmentStatement = getDatabaseManager().getChatDatabaseConnection().prepareStatement(selectMessageAttachmentString);
        selectMessageAttachmentStatement.setLong(1, messageRow);

        boolean isResultSet = selectMessageAttachmentStatement.execute();

        while(true) {
            if(isResultSet) {
                ResultSet theResultSet = selectMessageAttachmentStatement.getResultSet();
                while(theResultSet.next()) {
                    long resultInt = theResultSet.getLong(COLUMN_MESSAGE_ATTACHMENT_ATTACHMENT_ID);
                    attachments.add(getAttachmentByRow(resultInt));
                }
                theResultSet.close();
            } else {
                if(selectMessageAttachmentStatement.getUpdateCount() == -1) {
                    break;
                }
            }
            isResultSet = selectMessageAttachmentStatement.getMoreResults();
        }

        Message message = buildMessage(resultSet, chat, handle, attachments);

        resultSet.close();
        resultChatMessageSet.close();
        selectStatement.close();
        selectChatMessageStatement.close();
        selectMessageAttachmentStatement.close();

        return message;
    }

    private static long processTime(double time){
        long unpacked = (long) Math.floor(time / Math.pow(10, 9));

        if (unpacked > 0){
            return unpacked;
        }

        return (long) time;
    }
}