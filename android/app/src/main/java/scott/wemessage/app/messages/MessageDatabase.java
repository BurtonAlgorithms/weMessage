package scott.wemessage.app.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.models.ActionMessage;
import scott.wemessage.app.messages.models.Attachment;
import scott.wemessage.app.messages.models.Message;
import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.chats.Chat.ChatType;
import scott.wemessage.app.messages.models.chats.GroupChat;
import scott.wemessage.app.messages.models.chats.PeerChat;
import scott.wemessage.app.messages.models.users.Account;
import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.utils.StringUtils;

@SuppressWarnings("WeakerAccess")
public final class MessageDatabase extends SQLiteOpenHelper {

    public MessageDatabase(Context context) {
        super(context, weMessage.DATABASE_NAME, null, weMessage.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccountTable = "CREATE TABLE " + AccountTable.TABLE_NAME + " ("
                + AccountTable._ID + " INTEGER PRIMARY KEY, "
                + AccountTable.UUID + " TEXT, "
                + AccountTable.ACCOUNT_EMAIL + " TEXT, "
                + AccountTable.ACCOUNT_PASSWORD_CRYPTO + " TEXT );";

        String createActionMessageTable = "CREATE TABLE " + ActionMessageTable.TABLE_NAME + " ("
                + ActionMessageTable._ID + " INTEGER PRIMARY KEY, "
                + ActionMessageTable.UUID + " TEXT, "
                + ActionMessageTable.ACCOUNT_UUID + " TEXT, "
                + ActionMessageTable.CHAT_UUID + " TEXT, "
                + ActionMessageTable.ACTION_TEXT + " TEXT, "
                + ActionMessageTable.DATE + " TEXT );";

        String createAttachmentTable = "CREATE TABLE " + AttachmentTable.TABLE_NAME + " ("
                + AttachmentTable._ID + " INTEGER PRIMARY KEY, "
                + AttachmentTable.UUID + " TEXT, "
                + AttachmentTable.ACCOUNT_UUID + " TEXT, "
                + AttachmentTable.MAC_GUID + " TEXT, "
                + AttachmentTable.TRANSFER_NAME + " TEXT, "
                + AttachmentTable.FILE_LOCATION + " TEXT, "
                + AttachmentTable.FILE_TYPE + " TEXT, "
                + AttachmentTable.TOTAL_BYTES + " INTEGER );";

        String createContactTable = "CREATE TABLE " + ContactTable.TABLE_NAME + " ("
                + ContactTable._ID + " INTEGER PRIMARY KEY, "
                + ContactTable.UUID + " TEXT, "
                + ContactTable.FIRST_NAME + " TEXT, "
                + ContactTable.LAST_NAME + " TEXT, "
                + ContactTable.HANDLES + " TEXT, "
                + ContactTable.PRIMARY_HANDLE + "TEXT, "
                + ContactTable.CONTACT_PICTURE_FILE_LOCATION + " TEXT );";

        String createChatTable = "CREATE TABLE " + ChatTable.TABLE_NAME + " ("
                + ChatTable._ID + " INTEGER PRIMARY KEY, "
                + ChatTable.UUID + " TEXT, "
                + ChatTable.ACCOUNT_UUID + " TEXT, "
                + ChatTable.CHAT_TYPE + " TEXT, "
                + ChatTable.MAC_GUID + " TEXT, "
                + ChatTable.MAC_GROUP_ID + " TEXT, "
                + ChatTable.MAC_CHAT_IDENTIFIER + " TEXT, "
                + ChatTable.IS_IN_CHAT + " INTEGER, "
                + ChatTable.IS_DO_NOT_DISTURB + " INTEGER, "
                + ChatTable.HAS_UNREAD_MESSAGES + " INTEGER, "
                + ChatTable.DISPLAY_NAME + " TEXT, "
                + ChatTable.PARTICIPANTS + " TEXT, "
                + ChatTable.CHAT_PICTURE_FILE_LOCATION + " TEXT );";

        String createHandleTable = "CREATE TABLE " + HandleTable.TABLE_NAME + " ("
                + HandleTable._ID + " INTEGER PRIMARY KEY, "
                + HandleTable.UUID + " TEXT, "
                + HandleTable.HANDLE_ID + " TEXT, "
                + HandleTable.HANDLE_TYPE + " TEXT, "
                + HandleTable.IS_DO_NOT_DISTURB + " INTEGER, "
                + HandleTable.IS_BLOCKED + " INTEGER );";

        String createMessageTable = "CREATE TABLE " + MessageTable.TABLE_NAME + " ("
                + MessageTable._ID + " INTEGER PRIMARY KEY, "
                + MessageTable.UUID + " TEXT, "
                + MessageTable.ACCOUNT_UUID + " TEXT, "
                + MessageTable.MAC_GUID + " TEXT, "
                + MessageTable.CHAT_UUID + " TEXT, "
                + MessageTable.SENDER_UUID + " TEXT, "
                + MessageTable.ATTACHMENTS + " TEXT, "
                + MessageTable.TEXT + " TEXT, "
                + MessageTable.DATE_SENT + " INTEGER, "
                + MessageTable.DATE_DELIVERED + " INTEGER, "
                + MessageTable.DATE_READ + " INTEGER, "
                + MessageTable.ERRORED + " INTEGER, "
                + MessageTable.IS_SENT + " INTEGER, "
                + MessageTable.IS_DELIVERED + " INTEGER, "
                + MessageTable.IS_READ + " INTEGER, "
                + MessageTable.IS_FINISHED + " INTEGER, "
                + MessageTable.IS_FROM_ME + " INTEGER, "
                + MessageTable.MESSAGE_EFFECT + " TEXT, "
                + MessageTable.EFFECT_PERFORMED + " INTEGER );";

        db.execSQL(createAccountTable);
        db.execSQL(createActionMessageTable);
        db.execSQL(createAttachmentTable);
        db.execSQL(createContactTable);
        db.execSQL(createChatTable);
        db.execSQL(createHandleTable);
        db.execSQL(createMessageTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            String alterAddMessageEffects = "ALTER TABLE " + MessageTable.TABLE_NAME + " ADD COLUMN " + MessageTable.MESSAGE_EFFECT + " TEXT DEFAULT \"" + MessageEffect.NONE.getEffectName() + "\";";
            String alterAddMessageEffectPerformed = "ALTER TABLE " + MessageTable.TABLE_NAME + " ADD COLUMN " + MessageTable.EFFECT_PERFORMED + " INTEGER DEFAULT 1";
            db.execSQL(alterAddMessageEffects);
            db.execSQL(alterAddMessageEffectPerformed);
        }

        if (oldVersion < 3){
            Cursor cursorParseHandle = db.rawQuery("SELECT " + HandleTable.HANDLE_ID + " FROM " + HandleTable.TABLE_NAME, null);

            if (cursorParseHandle.moveToFirst()) {
                while (!cursorParseHandle.isAfterLast()) {
                    String rawHandle = cursorParseHandle.getString(cursorParseHandle.getColumnIndex(HandleTable.HANDLE_ID));
                    String updateMessage = "UPDATE " + HandleTable.TABLE_NAME + " SET " + HandleTable.HANDLE_ID
                            + " = '" + Handle.parseHandleId(rawHandle) + "' WHERE " + HandleTable.HANDLE_ID + " = '" + rawHandle + "'";

                    db.execSQL(updateMessage);
                    cursorParseHandle.moveToNext();
                }
            }
            cursorParseHandle.close();

            String handleValues = HandleTable._ID + ", " + HandleTable.UUID + ", " + HandleTable.HANDLE_ID + ", " + HandleTable.HANDLE_TYPE;

            String createHandlesTempTable = "CREATE TABLE handleTempTable (" + handleValues + ");";
            String insertIntoTemp = "INSERT INTO handleTempTable SELECT " + handleValues + " FROM " + HandleTable.TABLE_NAME + ";";
            String dropHandleTable = "DROP TABLE " + HandleTable.TABLE_NAME + ";";

            String createHandleTable = "CREATE TABLE " + HandleTable.TABLE_NAME + " ("
                    + HandleTable._ID + " INTEGER PRIMARY KEY, "
                    + HandleTable.UUID + " TEXT, "
                    + HandleTable.HANDLE_ID + " TEXT, "
                    + HandleTable.HANDLE_TYPE + " TEXT );";

            String insertIntoPerm = "INSERT INTO " + HandleTable.TABLE_NAME + " SELECT " + handleValues + " FROM handleTempTable;";
            String dropTempTable = "DROP TABLE handleTempTable;";

            db.execSQL(createHandlesTempTable);
            db.execSQL(insertIntoTemp);
            db.execSQL(dropHandleTable);
            db.execSQL(createHandleTable);
            db.execSQL(insertIntoPerm);
            db.execSQL(dropTempTable);

            LinkedHashMap<String, ContactV2> contactsV2Map = new LinkedHashMap<>();
            LinkedHashMap<String, String> contactHandleMap = new LinkedHashMap<>();
            List<ChatV2> chatsV2List = new ArrayList<>();

            String selectContactQuery = "SELECT * FROM " + ContactTable.TABLE_NAME;
            String selectQueryChat = "SELECT * FROM " + ChatTable.TABLE_NAME;
            String selectQueryHandle = "SELECT " + HandleTable.HANDLE_ID + " FROM " + HandleTable.TABLE_NAME + " WHERE " + HandleTable.UUID + " = ?";

            Cursor contactCursor = db.rawQuery(selectContactQuery, null);

            if (contactCursor.moveToFirst()) {
                while (!contactCursor.isAfterLast()) {
                    ContactV2 contact = new ContactV2();

                    contact.uuid = contactCursor.getString(contactCursor.getColumnIndex("uuid"));
                    contact.firstName = contactCursor.getString(contactCursor.getColumnIndex("first_name"));
                    contact.lastName = contactCursor.getString(contactCursor.getColumnIndex("last_name"));
                    contact.handleUuid = contactCursor.getString(contactCursor.getColumnIndex("handle_uuid"));
                    contact.contactPictureFileLocation = contactCursor.getString(contactCursor.getColumnIndex("contact_picture_file_location"));
                    contact.isBlocked = contactCursor.getInt(contactCursor.getColumnIndex("is_blocked"));
                    contact.isDoNotDisturb = contactCursor.getInt(contactCursor.getColumnIndex("is_do_not_disturb"));

                    Cursor cursorHandle = db.rawQuery(selectQueryHandle, new String[] { contact.handleUuid });

                    if (cursorHandle.getCount() > 0){
                        cursorHandle.moveToFirst();
                        contact.handleText = cursorHandle.getString(cursorHandle.getColumnIndex(HandleTable.HANDLE_ID));
                    }

                    cursorHandle.close();

                    if (!contactHandleMap.containsValue(contact.handleText)) {
                        contactsV2Map.put(contact.uuid, contact);
                        contactHandleMap.put(contact.uuid, contact.handleText);
                    }
                    contactCursor.moveToNext();
                }
            }
            contactCursor.close();

            Cursor chatCursor = db.rawQuery(selectQueryChat, null);

            if (chatCursor.moveToFirst()) {
                while (!chatCursor.isAfterLast()) {
                    ChatV2 chatV2 = new ChatV2();

                    chatV2.chatType = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.CHAT_TYPE));
                    chatV2.uuid = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.UUID));
                    chatV2.accountUuid = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.ACCOUNT_UUID));
                    chatV2.macGuid = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.MAC_GUID));
                    chatV2.macGroupId = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.MAC_GROUP_ID));
                    chatV2.macChatIdentifier = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.MAC_CHAT_IDENTIFIER));
                    chatV2.isInChat = chatCursor.getInt(chatCursor.getColumnIndex(ChatTable.IS_IN_CHAT));
                    chatV2.hasUnreadMessages = chatCursor.getInt(chatCursor.getColumnIndex(ChatTable.HAS_UNREAD_MESSAGES));
                    chatV2.chatPictureFileLocation = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.CHAT_PICTURE_FILE_LOCATION));

                    ChatType chatType = ChatType.stringToChatType(chatCursor.getString(chatCursor.getColumnIndex(ChatTable.CHAT_TYPE)));

                    if (chatType == ChatType.PEER){
                        String contactUuid = chatCursor.getString(chatCursor.getColumnIndex("contact_uuid"));

                        chatV2.participants.add(contactsV2Map.get(contactUuid).handleText);
                    }else {
                        chatV2.displayName = chatCursor.getString(chatCursor.getColumnIndex(ChatTable.DISPLAY_NAME));
                        chatV2.isDoNotDisturb = chatCursor.getInt(chatCursor.getColumnIndex(ChatTable.IS_DO_NOT_DISTURB));
                        chatV2.participants.addAll(Arrays.asList(chatCursor.getString(chatCursor.getColumnIndex(ChatTable.PARTICIPANTS)).split(", ")));
                    }
                    chatsV2List.add(chatV2);
                    chatCursor.moveToNext();
                }
            }
            chatCursor.close();

            String alterHandleDoNotDisturb = "ALTER TABLE " + HandleTable.TABLE_NAME + " ADD COLUMN " + HandleTable.IS_DO_NOT_DISTURB + " INTEGER DEFAULT 0";
            String alterHandleBlocked = "ALTER TABLE " + HandleTable.TABLE_NAME + " ADD COLUMN " + HandleTable.IS_BLOCKED + " INTEGER DEFAULT 0";
            String alterHandleType = "UPDATE " + HandleTable.TABLE_NAME + " SET " +
                    HandleTable.HANDLE_TYPE + " = '" + Handle.HandleType.UNKNOWN.getTypeName() + "' WHERE " + HandleTable.HANDLE_TYPE + " = '" + Handle.HandleType.IMESSAGE.getTypeName() + "'";

            String dropContactsTable = "DROP TABLE " + ContactTable.TABLE_NAME + ";";

            String createContactsTable = "CREATE TABLE " + ContactTable.TABLE_NAME + " ("
                    + ContactTable._ID + " INTEGER PRIMARY KEY, "
                    + ContactTable.UUID + " TEXT, "
                    + ContactTable.FIRST_NAME + " TEXT, "
                    + ContactTable.LAST_NAME + " TEXT, "
                    + ContactTable.HANDLES + " TEXT, "
                    + ContactTable.PRIMARY_HANDLE + " TEXT, "
                    + ContactTable.CONTACT_PICTURE_FILE_LOCATION + " TEXT );";

            String dropChatsTable = "DROP TABLE " + ChatTable.TABLE_NAME + ";";

            String createChatTable = "CREATE TABLE " + ChatTable.TABLE_NAME + " ("
                    + ChatTable._ID + " INTEGER PRIMARY KEY, "
                    + ChatTable.UUID + " TEXT, "
                    + ChatTable.ACCOUNT_UUID + " TEXT, "
                    + ChatTable.CHAT_TYPE + " TEXT, "
                    + ChatTable.MAC_GUID + " TEXT, "
                    + ChatTable.MAC_GROUP_ID + " TEXT, "
                    + ChatTable.MAC_CHAT_IDENTIFIER + " TEXT, "
                    + ChatTable.IS_IN_CHAT + " INTEGER, "
                    + ChatTable.IS_DO_NOT_DISTURB + " INTEGER, "
                    + ChatTable.HAS_UNREAD_MESSAGES + " INTEGER, "
                    + ChatTable.DISPLAY_NAME + " TEXT, "
                    + ChatTable.PARTICIPANTS + " TEXT, "
                    + ChatTable.CHAT_PICTURE_FILE_LOCATION + " TEXT );";

            db.execSQL(alterHandleDoNotDisturb);
            db.execSQL(alterHandleBlocked);
            db.execSQL(alterHandleType);
            db.execSQL(dropContactsTable);
            db.execSQL(createContactsTable);
            db.execSQL(dropChatsTable);
            db.execSQL(createChatTable);

            for (ContactV2 contact : contactsV2Map.values()){
                ContentValues values = new ContentValues();
                String firstName = "";
                String lastName = "";

                if (!StringUtils.isEmpty(contact.firstName)){
                    firstName = contact.firstName;
                }

                if (!StringUtils.isEmpty(contact.lastName)){
                    lastName = contact.lastName;
                }

                values.put(ContactTable.UUID, contact.uuid);
                values.put(ContactTable.FIRST_NAME, firstName);
                values.put(ContactTable.LAST_NAME, lastName);
                values.put(ContactTable.HANDLES, contact.handleText);
                values.put(ContactTable.PRIMARY_HANDLE, contact.handleText);

                if (contact.contactPictureFileLocation != null) {
                    values.put(ContactTable.CONTACT_PICTURE_FILE_LOCATION, contact.contactPictureFileLocation);
                }else {
                    values.putNull(ContactTable.CONTACT_PICTURE_FILE_LOCATION);
                }

                db.insert(ContactTable.TABLE_NAME, null, values);
            }

            for (ChatV2 chat : chatsV2List){
                ContentValues values = new ContentValues();
                ChatType chatType = ChatType.stringToChatType(chat.chatType);

                if (chatType == ChatType.PEER){
                    values.putNull(ChatTable.DISPLAY_NAME);
                    values.putNull(ChatTable.IS_DO_NOT_DISTURB);
                }else if (chatType == ChatType.GROUP){
                    values.put(ChatTable.DISPLAY_NAME, chat.displayName);
                    values.put(ChatTable.IS_DO_NOT_DISTURB, chat.isDoNotDisturb);
                }

                values.put(ChatTable.UUID, chat.uuid);
                values.put(ChatTable.ACCOUNT_UUID, chat.accountUuid);
                values.put(ChatTable.CHAT_TYPE, chatType.getTypeName());
                values.put(ChatTable.MAC_GUID, chat.macGuid);
                values.put(ChatTable.MAC_GROUP_ID, chat.macGroupId);
                values.put(ChatTable.MAC_CHAT_IDENTIFIER, chat.macChatIdentifier);
                values.put(ChatTable.IS_IN_CHAT, chat.isInChat);
                values.put(ChatTable.HAS_UNREAD_MESSAGES, chat.hasUnreadMessages);
                values.put(ChatTable.PARTICIPANTS, StringUtils.join(chat.participants, ", ", 2));

                if (chat.chatPictureFileLocation != null) {
                    values.put(ChatTable.CHAT_PICTURE_FILE_LOCATION, chat.chatPictureFileLocation);
                }else {
                    values.putNull(ChatTable.CHAT_PICTURE_FILE_LOCATION);
                }

                db.insert(ChatTable.TABLE_NAME, null, values);
            }

            String selectQueryMessage = "SELECT " + MessageTable.UUID + ", " + MessageTable.SENDER_UUID + " FROM " + MessageTable.TABLE_NAME;
            Cursor cursorMessage = db.rawQuery(selectQueryMessage, null);

            if (cursorMessage.moveToFirst()) {
                while (!cursorMessage.isAfterLast()) {
                    String contactSender = cursorMessage.getString(cursorMessage.getColumnIndex(MessageTable.SENDER_UUID));
                    String updateMessage = "UPDATE " + MessageTable.TABLE_NAME + " SET " + MessageTable.SENDER_UUID
                            + " = '" + contactHandleMap.get(contactSender) + "' WHERE " + MessageTable.SENDER_UUID + " = '" + contactSender + "'";

                    db.execSQL(updateMessage);
                    cursorMessage.moveToNext();
                }
            }
            cursorMessage.close();

            db.execSQL("DELETE FROM " + HandleTable.TABLE_NAME + " WHERE " + HandleTable._ID + " NOT IN (SELECT MIN(" + HandleTable._ID + ") FROM " + HandleTable.TABLE_NAME + " GROUP BY " + HandleTable.HANDLE_ID + ")");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > 3){
            db.execSQL("DROP TABLE " + AccountTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + ActionMessageTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + AttachmentTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + ContactTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + ChatTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + HandleTable.TABLE_NAME);
            db.execSQL("DROP TABLE " + MessageTable.TABLE_NAME);

            onCreate(db);
        }

        if (newVersion > 2){
            String values = MessageTable._ID + ", " + MessageTable.UUID + ", " + MessageTable.ACCOUNT_UUID + ", "
                    + MessageTable.MAC_GUID + ", " + MessageTable.CHAT_UUID + ", " + MessageTable.SENDER_UUID + ", " + MessageTable.ATTACHMENTS + ", " + MessageTable.TEXT + ", "
                    + MessageTable.DATE_SENT + ", " + MessageTable.DATE_DELIVERED + ", " + MessageTable.DATE_READ + ", " + MessageTable.ERRORED + ", " + MessageTable.IS_SENT + ", "
                    + MessageTable.IS_DELIVERED + ", " + MessageTable.IS_READ + ", " + MessageTable.IS_FINISHED + ", " + MessageTable.IS_FROM_ME;

            String createMessageTempTable = "CREATE TABLE messageTempTable (" + values + ");";
            String insertIntoTemp = "INSERT INTO messageTempTable SELECT " + values + " FROM " + MessageTable.TABLE_NAME + ";";
            String dropTable = "DROP TABLE " + MessageTable.TABLE_NAME + ";";

            String createMessageTable = "CREATE TABLE " + MessageTable.TABLE_NAME + " (" + MessageTable._ID + " INTEGER PRIMARY KEY, " + MessageTable.UUID + " TEXT, "
                    + MessageTable.ACCOUNT_UUID + " TEXT, " + MessageTable.MAC_GUID + " TEXT, " + MessageTable.CHAT_UUID + " TEXT, " + MessageTable.SENDER_UUID + " TEXT, "
                    + MessageTable.ATTACHMENTS + " TEXT, " + MessageTable.TEXT + " TEXT, " + MessageTable.DATE_SENT + " INTEGER, " + MessageTable.DATE_DELIVERED + " INTEGER, "
                    + MessageTable.DATE_READ + " INTEGER, " + MessageTable.ERRORED + " INTEGER, " + MessageTable.IS_SENT + " INTEGER, " + MessageTable.IS_DELIVERED + " INTEGER, "
                    + MessageTable.IS_READ + " INTEGER, " + MessageTable.IS_FINISHED + " INTEGER, " + MessageTable.IS_FROM_ME + " INTEGER );";

            String insertIntoPerm = "INSERT INTO " + MessageTable.TABLE_NAME + " SELECT " + values + " FROM messageTempTable;";
            String dropTempTable = "DROP TABLE messageTempTable;";

            db.execSQL(createMessageTempTable);
            db.execSQL(insertIntoTemp);
            db.execSQL(dropTable);
            db.execSQL(createMessageTable);
            db.execSQL(insertIntoPerm);
            db.execSQL(dropTempTable);
        }
    }

    public List<Account> getAccounts(){
        List<Account> accounts = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AccountTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Account account = buildAccount(cursor);

                if (account != null) {
                    accounts.add(account);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return accounts;
    }

    public List<Contact> getContacts(){
        List<Contact> contacts = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ContactTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Contact contact = buildContact(cursor);

                if (contact != null) {
                    contacts.add(contact);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return contacts;
    }

    public List<Handle> getHandles(){
        List<Handle> handles = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + HandleTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Handle handle = buildHandle(cursor);

                if (handle != null) {
                    handles.add(handle);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return handles;
    }

    public List<Chat> getChats(){
        List<Chat> chats = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Chat chat = buildChat(cursor);

                if (chat != null) {
                    chats.add(chat);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return chats;
    }

    public List<GroupChat> getGroupChatsWithName(String displayName){
        List<GroupChat> chats = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;

        if (displayName == null) {
            String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.DISPLAY_NAME + " IS NULL";
            cursor = db.rawQuery(selectQuery, null);
        }else {
            String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.DISPLAY_NAME + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{displayName});
        }

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Chat chat = buildChat(cursor);

                if (chat != null) {
                    chats.add((GroupChat) chat);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return chats;
    }

    public List<GroupChat> getGroupChatsWithLikeName(String displayName){
        List<GroupChat> chats = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;

        if (displayName == null) {
            String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.DISPLAY_NAME + " IS NULL";
            cursor = db.rawQuery(selectQuery, null);
        }else {
            String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.DISPLAY_NAME + " LIKE ?";
            cursor = db.rawQuery(selectQuery, new String[]{ displayName + "%" });
        }

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Chat chat = buildChat(cursor);

                if (chat != null) {
                    chats.add((GroupChat) chat);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return chats;
    }

    public PeerChat getChatByHandle(Handle handle){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.PARTICIPANTS + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{ Handle.parseHandleId(handle.getHandleID()) });
        PeerChat chat = null;

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            chat = (PeerChat) buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    public List<ActionMessage> getReversedActionMessages(Chat chat, long startIndex, long numberToFetch){
        List<ActionMessage> actionMessages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(ActionMessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + ActionMessageTable.TABLE_NAME + " WHERE " + ActionMessageTable._ID + " <= ? AND "
                + ActionMessageTable.CHAT_UUID + " = ? ORDER BY " + ActionMessageTable._ID + " DESC LIMIT " + numberToFetch;
        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chat.getUuid().toString()} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ActionMessage message = buildActionMessage(cursor);

                if (message != null) {
                    actionMessages.add(message);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return actionMessages;
    }

    public List<ActionMessage> getReversedActionMessagesByTime(Chat chat, long startIndex, long numberToFetch){
        List<ActionMessage> actionMessages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(ActionMessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + ActionMessageTable.TABLE_NAME + " WHERE " + ActionMessageTable._ID + " <= ? AND "
                + ActionMessageTable.CHAT_UUID + " = ? ORDER BY " + ActionMessageTable.DATE + " DESC LIMIT " + numberToFetch;
        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chat.getUuid().toString()} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ActionMessage message = buildActionMessage(cursor);

                if (message != null) {
                    actionMessages.add(message);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return actionMessages;
    }

    public List<Attachment> getReversedAttachmentsInChat(String chatUuid, long startIndex, long numberToFetch){
        List<Attachment> attachments = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable._ID + " <= ? AND "
                + MessageTable.CHAT_UUID + " = ? ORDER BY " + MessageTable._ID + " DESC LIMIT " + numberToFetch;
        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chatUuid} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex(MessageTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString())){
                    attachments.addAll(stringListToAttachments(Arrays.asList(cursor.getString(cursor.getColumnIndex(MessageTable.ATTACHMENTS)).split(", "))));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return attachments;
    }

    public List<Message> getReversedMessages(Chat chat, long startIndex, long numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable._ID + " <= ? AND "
                + MessageTable.CHAT_UUID + " = ? ORDER BY " + MessageTable._ID + " DESC LIMIT " + numberToFetch;
        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chat.getUuid().toString()} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Message message = buildMessage(cursor);

                if (message != null) {
                    messages.add(message);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return messages;
    }

    public List<Message> getReversedMessagesByTime(Chat chat, long startIndex, long numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable._ID + " <= ? AND "
                + MessageTable.CHAT_UUID + " = ? ORDER BY " + MessageTable.DATE_SENT + " DESC LIMIT " + numberToFetch;
        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chat.getUuid().toString()} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Message message = buildMessage(cursor);

                if (message != null) {
                    messages.add(message);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return messages;
    }

    public List<Message> getReversedMessagesWithSearchParameters(Chat chat, String matchingText, boolean isFromMe, long startIndex, long numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        long finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
        long start = finalRow - startIndex;

        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable._ID + " <= ? AND "
                + MessageTable.CHAT_UUID + " = ? AND " + MessageTable.TEXT + " = ? AND " + MessageTable.IS_FROM_ME + " = ? ORDER BY "
                + MessageTable._ID + " DESC LIMIT " + numberToFetch;

        Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(start), chat.getUuid().toString(), matchingText, String.valueOf(booleanToInteger(isFromMe))} );

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Message message = buildMessage(cursor);

                if (message != null) {
                    messages.add(message);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return messages;
    }

    public Message getLastMessageFromChat(Chat chat){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable.CHAT_UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {chat.getUuid().toString()});
        Message message = null;

        if (cursor.getCount() > 0){
            cursor.moveToLast();
            message = buildMessage(cursor);
        }
        cursor.close();
        return message;
    }

    public Account buildAccount(Cursor cursor){
        Account account = new Account().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(AccountTable.UUID))))
                .setEmail(cursor.getString(cursor.getColumnIndex(AccountTable.ACCOUNT_EMAIL)))
                .setEncryptedPassword(cursor.getString(cursor.getColumnIndex(AccountTable.ACCOUNT_PASSWORD_CRYPTO)));
        return account;
    }

    public ContentValues accountToContentValues(Account account){
        ContentValues values = new ContentValues();

        values.put(AccountTable.UUID, account.getUuid().toString());
        values.put(AccountTable.ACCOUNT_EMAIL, account.getEmail().toLowerCase());
        values.put(AccountTable.ACCOUNT_PASSWORD_CRYPTO, account.getEncryptedPassword());

        return values;
    }

    public Account getAccountByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        Account account = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            account = buildAccount(cursor);
        }
        cursor.close();
        return account;
    }

    public Account getAccountByEmail(String email){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.ACCOUNT_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {email.toLowerCase()});
        Account account = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            account = buildAccount(cursor);
        }
        cursor.close();
        return account;
    }

    public Account getAccountByHandle(Handle handle){
        if (handle.getHandleType() != Handle.HandleType.ME) return null;

        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.ACCOUNT_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { Handle.parseHandleId(handle.getHandleID()) });
        Account account = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            account = buildAccount(cursor);
        }
        cursor.close();
        return account;
    }

    public void addAccount(Account account){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(AccountTable.TABLE_NAME, null, accountToContentValues(account));

        if (getHandleByAccount(account) == null){
            addHandle(new Handle(UUID.randomUUID(), account.getEmail(), Handle.HandleType.ME, false, false));
        }
    }

    public void updateAccount(String uuid, Account newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = accountToContentValues(newData);
        String selection = AccountTable.UUID + " = ?";

        UUID oldHandleUuid = getHandleByAccount(getAccountByUuid(uuid)).getUuid();

        db.update(AccountTable.TABLE_NAME, values, selection, new String[]{ uuid });
        updateHandle(oldHandleUuid.toString(), new Handle(oldHandleUuid, newData.getEmail(), Handle.HandleType.ME, false, false));
    }

    public ActionMessage buildActionMessage(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(ActionMessageTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        ActionMessage actionMessage = new ActionMessage().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ActionMessageTable.UUID))))
                .setChat(getChatByUuid(cursor.getString(cursor.getColumnIndex(ActionMessageTable.CHAT_UUID)))).setActionText(cursor.getString(cursor.getColumnIndex(ActionMessageTable.ACTION_TEXT)))
                .setDate(cursor.getLong(cursor.getColumnIndex(ActionMessageTable.DATE)));

        return actionMessage;
    }

    public ContentValues actionMessageToContentValues(ActionMessage actionMessage){
        ContentValues values = new ContentValues();

        values.put(ActionMessageTable.UUID, actionMessage.getUuid().toString());
        values.put(ActionMessageTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(ActionMessageTable.CHAT_UUID, actionMessage.getChat().getUuid().toString());
        values.put(ActionMessageTable.ACTION_TEXT, actionMessage.getActionText());
        values.put(ActionMessageTable.DATE, actionMessage.getDate());

        return values;
    }

    public ActionMessage getActionMessageByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ActionMessageTable.TABLE_NAME + " WHERE " + ActionMessageTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        ActionMessage actionMessage = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            actionMessage = buildActionMessage(cursor);
        }
        cursor.close();
        return actionMessage;
    }

    public void addActionMessage(ActionMessage actionMessage){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(ActionMessageTable.TABLE_NAME, null, actionMessageToContentValues(actionMessage));
    }

    public void updateActionMessage(String uuid, ActionMessage newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = actionMessageToContentValues(newData);
        String selection = ActionMessageTable.UUID + " = ?";

        db.update(ActionMessageTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    private void deleteActionMessage(String uuid){
        String whereClause = ActionMessageTable.UUID + " = ?";
        getWritableDatabase().delete(ActionMessageTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    public Attachment buildAttachment(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(AttachmentTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Attachment attachment = new Attachment().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(AttachmentTable.UUID))))
                .setMacGuid(cursor.getString(cursor.getColumnIndex(AttachmentTable.MAC_GUID))).setTransferName(cursor.getString(cursor.getColumnIndex(AttachmentTable.TRANSFER_NAME)))
                .setFileLocation(new FileLocationContainer(cursor.getString(cursor.getColumnIndex(AttachmentTable.FILE_LOCATION))))
                .setFileType(cursor.getString(cursor.getColumnIndex(AttachmentTable.FILE_TYPE))).setTotalBytes(cursor.getLong(cursor.getColumnIndex(AttachmentTable.TOTAL_BYTES)));
        return attachment;
    }

    public ContentValues attachmentToContentValues(Attachment attachment){
        ContentValues values = new ContentValues();

        values.put(AttachmentTable.UUID, attachment.getUuid().toString());
        values.put(AttachmentTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(AttachmentTable.MAC_GUID, attachment.getMacGuid());
        values.put(AttachmentTable.TRANSFER_NAME, attachment.getTransferName());
        values.put(AttachmentTable.FILE_LOCATION, attachment.getFileLocation().getFileLocation());
        values.put(AttachmentTable.FILE_TYPE, attachment.getFileType());
        values.put(AttachmentTable.TOTAL_BYTES, attachment.getTotalBytes());

        return values;
    }

    public Attachment getAttachmentByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AttachmentTable.TABLE_NAME + " WHERE " + AttachmentTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        Attachment attachment = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            attachment = buildAttachment(cursor);
        }
        cursor.close();
        return attachment;
    }

    public Attachment getAttachmentByMacGuid(String macGuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + AttachmentTable.TABLE_NAME + " WHERE " + AttachmentTable.MAC_GUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {macGuid});
        Attachment attachment = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            attachment = buildAttachment(cursor);
        }
        cursor.close();
        return attachment;
    }

    public void addAttachment(Attachment attachment){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(AttachmentTable.TABLE_NAME, null, attachmentToContentValues(attachment));
    }

    public void updateAttachment(String uuid, Attachment newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = attachmentToContentValues(newData);
        String selection = AttachmentTable.UUID + " = ?";

        db.update(AttachmentTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    public void updateAttachmentByMacGuid(String macGuid, Attachment newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = attachmentToContentValues(newData);
        String selection = AttachmentTable.MAC_GUID + " = ?";

        db.update(AttachmentTable.TABLE_NAME, values, selection, new String[]{ macGuid });
    }

    protected void deleteAttachmentByUuid(String uuid){
        String whereClause = AttachmentTable.UUID + " = ?";
        getWritableDatabase().delete(AttachmentTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    private void deleteAttachmentByMacGuid(String macGuid){
        String whereClause = AttachmentTable.MAC_GUID + " = ?";
        getWritableDatabase().delete(AttachmentTable.TABLE_NAME, whereClause, new String[] { macGuid });
    }

    public Contact buildContact(Cursor cursor){
        Contact contact = new Contact().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ContactTable.UUID))))
                .setFirstName(cursor.getString(cursor.getColumnIndex(ContactTable.FIRST_NAME))).setLastName(cursor.getString(cursor.getColumnIndex(ContactTable.LAST_NAME)))
                .setHandles(stringListToHandles(Arrays.asList(cursor.getString(cursor.getColumnIndex(ContactTable.HANDLES)).split(", "))))
                .setPrimaryHandle(getHandleByHandleID(cursor.getString(cursor.getColumnIndex(ContactTable.PRIMARY_HANDLE))))
                .setContactPictureFileLocation(new FileLocationContainer(cursor.getString(cursor.getColumnIndex(ContactTable.CONTACT_PICTURE_FILE_LOCATION))));
        return contact;
    }

    public ContentValues contactToContentValues(Contact contact){
        ContentValues values = new ContentValues();

        values.put(ContactTable.UUID, contact.getUuid().toString());
        values.put(ContactTable.FIRST_NAME, contact.getFirstName());
        values.put(ContactTable.LAST_NAME, contact.getLastName());
        values.put(ContactTable.HANDLES, StringUtils.join(handlesToStringList(contact.getHandles()), ", ", 2));
        values.put(ContactTable.PRIMARY_HANDLE, Handle.parseHandleId(contact.getPrimaryHandle().getHandleID()));

        if (contact.getContactPictureFileLocation() != null) {
            values.put(ContactTable.CONTACT_PICTURE_FILE_LOCATION, contact.getContactPictureFileLocation().getFileLocation());
        }else {
            values.putNull(ContactTable.CONTACT_PICTURE_FILE_LOCATION);
        }
        return values;
    }

    public Contact getContactByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ContactTable.TABLE_NAME + " WHERE " + ContactTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        Contact contact = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            contact = buildContact(cursor);
        }
        cursor.close();
        return contact;
    }

    public Contact getContactByHandle(Handle handle){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ContactTable.TABLE_NAME + " WHERE " + ContactTable.HANDLES + " LIKE ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { "%" + Handle.parseHandleId(handle.getHandleID()) + "%" });
        Contact contact = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            contact = buildContact(cursor);
        }
        cursor.close();
        return contact;
    }

    protected void addContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(ContactTable.TABLE_NAME, null, contactToContentValues(contact));
    }

    protected void updateContact(String uuid, Contact newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = contactToContentValues(newData);
        String selection = ContactTable.UUID + " = ?";

        db.update(ContactTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    protected void deleteContactByUuid(String uuid){
        String whereClause = ContactTable.UUID + " = ?";
        getWritableDatabase().delete(ContactTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    public Handle buildHandle(Cursor cursor){
        Handle handle = new Handle(
                UUID.fromString(cursor.getString(cursor.getColumnIndex(HandleTable.UUID))),
                cursor.getString(cursor.getColumnIndex(HandleTable.HANDLE_ID)),
                Handle.HandleType.stringToHandleType(cursor.getString(cursor.getColumnIndex(HandleTable.HANDLE_TYPE))),
                integerToBoolean(cursor.getInt(cursor.getColumnIndex(HandleTable.IS_DO_NOT_DISTURB))),
                integerToBoolean(cursor.getInt(cursor.getColumnIndex(HandleTable.IS_BLOCKED)))
        );
        return handle;
    }

    public ContentValues handleToContentValues(Handle handle){
        ContentValues values = new ContentValues();

        values.put(HandleTable.UUID, handle.getUuid().toString());
        values.put(HandleTable.HANDLE_ID, Handle.parseHandleId(handle.getHandleID()));
        values.put(HandleTable.HANDLE_TYPE, handle.getHandleType().getTypeName());
        values.put(HandleTable.IS_DO_NOT_DISTURB, booleanToInteger(handle.isDoNotDisturb()));
        values.put(HandleTable.IS_BLOCKED, booleanToInteger(handle.isBlocked()));

        return values;
    }

    public Handle getHandleByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + HandleTable.TABLE_NAME + " WHERE " + HandleTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { uuid });
        Handle handle = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            handle = buildHandle(cursor);
        }
        cursor.close();
        return handle;
    }

    public Handle getHandleByHandleID(String handleID){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + HandleTable.TABLE_NAME + " WHERE " + HandleTable.HANDLE_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { Handle.parseHandleId(handleID) });
        Handle handle = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            handle = buildHandle(cursor);
        }
        cursor.close();
        return handle;
    }

    public Handle getHandleByAccount(Account account){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + HandleTable.TABLE_NAME + " WHERE " + HandleTable.HANDLE_ID + " = ? AND " + HandleTable.HANDLE_TYPE + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { account.getEmail(), Handle.HandleType.ME.getTypeName() });
        Handle handle = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            handle = buildHandle(cursor);
        }
        cursor.close();
        return handle;
    }

    protected void addHandle(Handle handle){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(HandleTable.TABLE_NAME, null, handleToContentValues(handle));
    }

    protected void updateHandle(String uuid, Handle newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = handleToContentValues(newData);
        String selection = HandleTable.UUID + " = ?";

        db.update(HandleTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    protected void deleteHandleByUuid(String uuid){
        String whereClause = HandleTable.UUID + " = ?";
        getWritableDatabase().delete(HandleTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    public Chat buildChat(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(ChatTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Chat chat;
        ChatType chatType = ChatType.stringToChatType(cursor.getString(cursor.getColumnIndex(ChatTable.CHAT_TYPE)));

        if (chatType == ChatType.PEER){
            chat = new PeerChat().setHandle(getHandleByHandleID(cursor.getString(cursor.getColumnIndex(ChatTable.PARTICIPANTS))))
                    .setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ChatTable.UUID))))
                    .setMacGuid(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GUID))).setMacGroupID(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GROUP_ID)))
                    .setMacChatIdentifier(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_CHAT_IDENTIFIER)))
                    .setIsInChat(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.IS_IN_CHAT))))
                    .setHasUnreadMessages(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.HAS_UNREAD_MESSAGES))));
        }else {
            chat = new GroupChat().setDisplayName(cursor.getString(cursor.getColumnIndex(ChatTable.DISPLAY_NAME)))
                    .setParticipants(stringListToHandles(Arrays.asList(cursor.getString(cursor.getColumnIndex(ChatTable.PARTICIPANTS)).split(", "))))
                    .setDoNotDisturb(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.IS_DO_NOT_DISTURB))))
                    .setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ChatTable.UUID)))).setMacGuid(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GUID)))
                    .setMacGroupID(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GROUP_ID))).setMacChatIdentifier(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_CHAT_IDENTIFIER)))
                    .setIsInChat(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.IS_IN_CHAT))))
                    .setHasUnreadMessages(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.HAS_UNREAD_MESSAGES))));
        }

        if (StringUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ChatTable.CHAT_PICTURE_FILE_LOCATION)))) {
            chat.setChatPictureFileLocation(null);
        }else {
            chat.setChatPictureFileLocation(new FileLocationContainer(cursor.getString(cursor.getColumnIndex(ChatTable.CHAT_PICTURE_FILE_LOCATION))));
        }
        return chat;
    }

    public ContentValues chatToContentValues(Chat chat){
        ContentValues values = new ContentValues();

        if (chat instanceof PeerChat){
            values.put(ChatTable.PARTICIPANTS, Handle.parseHandleId(((PeerChat) chat).getHandle().getHandleID()));
            values.putNull(ChatTable.DISPLAY_NAME);
            values.putNull(ChatTable.IS_DO_NOT_DISTURB);
        }else if (chat instanceof GroupChat){
            values.put(ChatTable.DISPLAY_NAME, ((GroupChat) chat).getDisplayName());
            values.put(ChatTable.PARTICIPANTS, StringUtils.join(handlesToStringList(((GroupChat) chat).getParticipants()), ", ", 2));
            values.put(ChatTable.IS_DO_NOT_DISTURB, booleanToInteger(((GroupChat) chat).isDoNotDisturb()));
        }

        values.put(ChatTable.UUID, chat.getUuid().toString());
        values.put(ChatTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(ChatTable.CHAT_TYPE, chat.getChatType().getTypeName());
        values.put(ChatTable.MAC_GUID, chat.getMacGuid());
        values.put(ChatTable.MAC_GROUP_ID, chat.getMacGroupID());
        values.put(ChatTable.MAC_CHAT_IDENTIFIER, chat.getMacChatIdentifier());
        values.put(ChatTable.IS_IN_CHAT, booleanToInteger(chat.isInChat()));
        values.put(ChatTable.HAS_UNREAD_MESSAGES, booleanToInteger(chat.hasUnreadMessages()));

        if (chat.getChatPictureFileLocation() != null) {
            values.put(ChatTable.CHAT_PICTURE_FILE_LOCATION, chat.getChatPictureFileLocation().getFileLocation());
        }else {
            values.putNull(ChatTable.CHAT_PICTURE_FILE_LOCATION);
        }

        return values;
    }

    public Chat getChatByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        Chat chat = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            chat = buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    public Chat getChatByMacGuid(String macGuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.MAC_GUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {macGuid});
        Chat chat = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            chat = buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    public Chat getChatByMacGroupId(String macGroupId){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.MAC_GROUP_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {macGroupId});
        Chat chat = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            chat = buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    public Chat getChatByMacChatIdentifier(String macChatIdentifier){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.MAC_CHAT_IDENTIFIER + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {macChatIdentifier});
        Chat chat = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            chat = buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    protected void addChat(Chat chat){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(ChatTable.TABLE_NAME, null, chatToContentValues(chat));
    }

    protected void updateChat(String uuid, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.UUID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    protected void updateChatByMacGuid(String macGuid, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_GUID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macGuid });
    }

    protected void updateChatByMacGroupID(String macGroupID, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_GROUP_ID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macGroupID });
    }

    protected void updateChatByMacChatIdentifier(String macChatIdentifier, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_CHAT_IDENTIFIER + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macChatIdentifier });
    }

    protected void deleteChatByUuid(String uuid){
        String whereClause = ChatTable.UUID + " = ?";
        final Chat chat = getChatByUuid(uuid);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();

        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    protected void deleteChatByMacGuid(String macGuid){
        String whereClause = ChatTable.MAC_GUID + " = ?";
        final Chat chat = getChatByMacGuid(macGuid);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();

        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { macGuid });
    }

    protected void deleteChatByMacGroupID(String macGroupID){
        String whereClause = ChatTable.MAC_GROUP_ID + " = ?";
        final Chat chat = getChatByMacGroupId(macGroupID);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();
        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { macGroupID });
    }

    protected void deleteChatByMacChatIdentifier(String macChatIdentifier){
        String whereClause = ChatTable.MAC_CHAT_IDENTIFIER + " = ?";
        final Chat chat = getChatByMacChatIdentifier(macChatIdentifier);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0L, Long.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();
        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { macChatIdentifier });
    }

    public Message buildMessage(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(MessageTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Message message = new Message().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(MessageTable.UUID))))
                .setMacGuid(cursor.getString(cursor.getColumnIndex(MessageTable.MAC_GUID))).setChat(getChatByUuid(cursor.getString(cursor.getColumnIndex(MessageTable.CHAT_UUID))))
                .setSender(getHandleByHandleID(cursor.getString(cursor.getColumnIndex(MessageTable.SENDER_UUID))))
                .setAttachments(stringListToAttachments(Arrays.asList(cursor.getString(cursor.getColumnIndex(MessageTable.ATTACHMENTS)).split(", "))))
                .setText(cursor.getString(cursor.getColumnIndex(MessageTable.TEXT))).setDateSent(cursor.getLong(cursor.getColumnIndex(MessageTable.DATE_SENT)))
                .setDateDelivered(cursor.getLong(cursor.getColumnIndex(MessageTable.DATE_DELIVERED))).setDateRead(cursor.getLong(cursor.getColumnIndex(MessageTable.DATE_READ)))
                .setHasErrored(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.ERRORED)))).setIsSent(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_SENT))))
                .setDelivered(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_DELIVERED)))).setRead(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_READ))))
                .setFinished(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_FINISHED)))).setFromMe(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_FROM_ME))))
                .setMessageEffect(MessageEffect.from(cursor.getString(cursor.getColumnIndex(MessageTable.MESSAGE_EFFECT)))).setEffectFinished(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.EFFECT_PERFORMED))));
        return message;
    }

    public ContentValues messageToContentValues(Message message){
        ContentValues values = new ContentValues();

        values.put(MessageTable.UUID, message.getUuid().toString());
        values.put(MessageTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(MessageTable.MAC_GUID, message.getMacGuid());
        values.put(MessageTable.CHAT_UUID, message.getChat().getUuid().toString());
        values.put(MessageTable.SENDER_UUID, Handle.parseHandleId(message.getSender().getHandleID()));
        values.put(MessageTable.ATTACHMENTS, StringUtils.join(attachmentsToStringList(message.getAttachments()), ", ", 2));
        values.put(MessageTable.TEXT, message.getText());
        values.put(MessageTable.DATE_SENT, message.getDateSent());
        values.put(MessageTable.DATE_DELIVERED, message.getDateDelivered());
        values.put(MessageTable.DATE_READ, message.getDateRead());
        values.put(MessageTable.ERRORED, booleanToInteger(message.hasErrored()));
        values.put(MessageTable.IS_SENT, booleanToInteger(message.isSent()));
        values.put(MessageTable.IS_DELIVERED, booleanToInteger(message.isDelivered()));
        values.put(MessageTable.IS_READ, booleanToInteger(message.isRead()));
        values.put(MessageTable.IS_FINISHED, booleanToInteger(message.isFinished()));
        values.put(MessageTable.IS_FROM_ME, booleanToInteger(message.isFromMe()));
        values.put(MessageTable.MESSAGE_EFFECT, message.getMessageEffect().getEffectName());
        values.put(MessageTable.EFFECT_PERFORMED, booleanToInteger(message.getEffectFinished()));

        return values;
    }

    public Message getMessageByUuid(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable.UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {uuid});
        Message message = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            message = buildMessage(cursor);
        }
        cursor.close();
        return message;
    }

    public Message getMessageByMacGuid(String macGuid){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT * FROM " + MessageTable.TABLE_NAME + " WHERE " + MessageTable.MAC_GUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {macGuid});
        Message message = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            message = buildMessage(cursor);
        }
        cursor.close();
        return message;
    }

    protected void addMessage(Message message){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(MessageTable.TABLE_NAME, null, messageToContentValues(message));
    }

    protected void updateMessage(String uuid, Message newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = messageToContentValues(newData);
        String selection = MessageTable.UUID + " = ?";

        db.update(MessageTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    protected void updateMessageByMacGuid(String macGuid, Message newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = messageToContentValues(newData);
        String selection = MessageTable.MAC_GUID + " = ?";

        db.update(MessageTable.TABLE_NAME, values, selection, new String[]{ macGuid });
    }

    protected void deleteMessageByUuid(String uuid){
        if (getMessageByUuid(uuid) == null) return;

        String whereClause = MessageTable.UUID + " = ?";

        List<Attachment> attachments = getMessageByUuid(uuid).getAttachments();

        for (Attachment a : attachments){
            if (a != null) {
                a.getFileLocation().getFile().delete();
                deleteAttachmentByUuid(a.getUuid().toString());
            }
        }
        getWritableDatabase().delete(MessageTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    protected void deleteMessageByMacGuid(String macGuid){
        if (getMessageByUuid(macGuid) == null) return;

        String whereClause = MessageTable.MAC_GUID + " = ?";

        for (Attachment a : getMessageByMacGuid(macGuid).getAttachments()){
            if (a != null) {
                a.getFileLocation().getFile().delete();
                deleteAttachmentByUuid(a.getUuid().toString());
            }
        }
        getWritableDatabase().delete(MessageTable.TABLE_NAME, whereClause, new String[] { macGuid });
    }

    private List<String> handlesToStringList(List<Handle> handles){
        List<String> stringList = new ArrayList<>();

        for (Handle handle : handles){
            stringList.add(Handle.parseHandleId(handle.getHandleID()));
        }
        return stringList;
    }

    private List<Handle> stringListToHandles(List<String> stringList){
        List<Handle> handles = new ArrayList<>();

        for (String s : stringList){
            handles.add(getHandleByHandleID(s));
        }
        return handles;
    }

    private List<String> attachmentsToStringList(List<Attachment> attachments){
        List<String> stringList = new ArrayList<>();

        for (Attachment a : attachments) {
            if (a != null) {
                stringList.add(a.getUuid().toString());
            }
        }
        return stringList;
    }

    private List<Attachment> stringListToAttachments(List<String> stringList){
        List<Attachment> attachments = new ArrayList<>();

        for (String s : stringList){
            Attachment a = getAttachmentByUuid(s);

            if (a != null) {
                attachments.add(a);
            }
        }
        return attachments;
    }

    private Long getMaxIdFromTable(String tableName, String idRow){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT MAX(" + idRow + ") FROM " + tableName;

        Cursor cursor = db.rawQuery(selectQuery, null);

        Long result = null;
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            result = cursor.getLong(0);
        }
        cursor.close();
        return result;
    }

    private boolean integerToBoolean(Integer integer){
        if (integer > 1 || integer < 0) throw new ArrayIndexOutOfBoundsException("Parsing a boolean from an int must be either 0 or 1. Found: " + integer);
        return integer == 1;
    }

    private int booleanToInteger(Boolean bool){
        if (bool == null) return 0;
        if (bool) return 1;
        else return 0;
    }

    public static class AccountTable {
        public static final String TABLE_NAME = "accounts";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_EMAIL = "account_email";
        public static final String ACCOUNT_PASSWORD_CRYPTO = "account_password_crypto";
    }

    public static class ActionMessageTable {
        public static final String TABLE_NAME = "action_messages";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String CHAT_UUID = "chat_uuid";
        public static final String ACTION_TEXT = "action_text";
        public static final String DATE = "date";
    }

    public static class AttachmentTable {
        public static final String TABLE_NAME = "attachments";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String MAC_GUID = "mac_guid";
        public static final String TRANSFER_NAME = "transfer_name";
        public static final String FILE_LOCATION = "file_location";
        public static final String FILE_TYPE = "file_type";
        public static final String TOTAL_BYTES = "total_bytes";
    }

    public static class ContactTable {
        public static final String TABLE_NAME = "contacts";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String HANDLES = "handles";
        public static final String PRIMARY_HANDLE = "primary_handle";
        public static final String CONTACT_PICTURE_FILE_LOCATION = "contact_picture_file_location";
    }

    public static class ChatTable {
        public static final String TABLE_NAME = "chats";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String CHAT_TYPE = "chat_type";
        public static final String MAC_GUID = "mac_guid";
        public static final String MAC_GROUP_ID = "mac_group_id";
        public static final String MAC_CHAT_IDENTIFIER = "mac_chat_identifier";
        public static final String IS_IN_CHAT = "is_in_chat";
        public static final String IS_DO_NOT_DISTURB = "is_do_not_disturb";
        public static final String HAS_UNREAD_MESSAGES = "has_unread_messages";
        public static final String DISPLAY_NAME = "display_name";
        public static final String PARTICIPANTS = "participants";
        public static final String CHAT_PICTURE_FILE_LOCATION = "chat_picture_file_location";
    }

    public static class HandleTable {
        public static final String TABLE_NAME = "handles";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String HANDLE_ID = "handle_id";
        public static final String HANDLE_TYPE = "handle_type";
        public static final String IS_DO_NOT_DISTURB = "is_do_not_disturb";
        public static final String IS_BLOCKED = "is_blocked";
    }

    public static class MessageTable {
        public static final String TABLE_NAME = "messages";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String MAC_GUID = "mac_guid";
        public static final String CHAT_UUID = "chat_uuid";
        public static final String SENDER_UUID = "sender_uuid";
        public static final String ATTACHMENTS = "attachments";
        public static final String TEXT = "text";
        public static final String DATE_SENT = "date_sent";
        public static final String DATE_DELIVERED = "date_delivered";
        public static final String DATE_READ = "date_read";
        public static final String ERRORED = "errored";
        public static final String IS_SENT = "is_sent";
        public static final String IS_DELIVERED = "is_delivered";
        public static final String IS_READ = "is_read";
        public static final String IS_FINISHED = "is_finished";
        public static final String IS_FROM_ME = "is_from_me";
        public static final String MESSAGE_EFFECT = "message_effect";
        public static final String EFFECT_PERFORMED = "effect_performed";
    }

    public static class AccountNotLoggedInException extends NullPointerException { }

    @Deprecated
    private static class ContactV2 {
        public String uuid;
        public String firstName;
        public String lastName;
        public String handleUuid;
        public String handleText;
        public String contactPictureFileLocation;
        public Integer isDoNotDisturb;
        public Integer isBlocked;
    }

    @Deprecated
    private static class ChatV2 {
        public String uuid;
        public String accountUuid;
        public String chatType;
        public String macGuid;
        public String macGroupId;
        public String macChatIdentifier;
        public Integer isInChat;
        public Integer isDoNotDisturb;
        public Integer hasUnreadMessages;
        public String displayName;
        public ArrayList<String> participants = new ArrayList<>();
        public String chatPictureFileLocation;
    }
}