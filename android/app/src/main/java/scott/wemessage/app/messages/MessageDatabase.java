package scott.wemessage.app.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.objects.Account;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Handle;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.Chat.ChatType;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
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
                + ContactTable.ACCOUNT_UUID + " TEXT, "
                + ContactTable.FIRST_NAME + " TEXT, "
                + ContactTable.LAST_NAME + " TEXT, "
                + ContactTable.HANDLE_UUID + " TEXT, "
                + ContactTable.IS_DO_NOT_DISTURB + " INTEGER, "
                + ContactTable.IS_BLOCKED + " INTEGER, "
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
                + ChatTable.CONTACT_UUID + " TEXT, "
                + ChatTable.DISPLAY_NAME + " TEXT, "
                + ChatTable.PARTICIPANTS + " TEXT, "
                + ChatTable.CHAT_PICTURE_FILE_LOCATION + " TEXT );";

        String createHandleTable = "CREATE TABLE " + HandleTable.TABLE_NAME + " ("
                + HandleTable._ID + " INTEGER PRIMARY KEY, "
                + HandleTable.UUID + " TEXT, "
                + HandleTable.ACCOUNT_UUID + " TEXT, "
                + HandleTable.HANDLE_ID + " TEXT, "
                + HandleTable.HANDLE_TYPE + " TEXT );";

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
                + MessageTable.IS_FROM_ME + " INTEGER );";

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

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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
        String selectQuery = "SELECT * FROM " + ChatTable.TABLE_NAME + " WHERE " + ChatTable.CONTACT_UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{getContactByHandle(handle).getUuid().toString()});
        PeerChat chat = null;

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            chat = (PeerChat) buildChat(cursor);
        }
        cursor.close();
        return chat;
    }

    public GroupChat getGroupChatByName(String displayName, String lastMessage){
        List<GroupChat> chats = getGroupChatsWithName(displayName);

        if (chats.isEmpty()) return null;

        if (chats.size() == 1) {
            return chats.get(0);
        } else {
            if (lastMessage == null) return null;

            for (Chat chat : chats) {
                Message chatLastMessage = getLastMessageFromChat(chat);

                if (chatLastMessage == null) return null;
                if (lastMessage.equals(chatLastMessage.getText())) {
                    return (GroupChat) chat;
                }
            }
        }
        return null;
    }

    public List<ActionMessage> getReversedActionMessages(Chat chat, int startIndex, int numberToFetch){
        List<ActionMessage> actionMessages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(ActionMessageTable.TABLE_NAME, MessageTable._ID);
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

    public List<ActionMessage> getReversedActionMessagesByTime(Chat chat, int startIndex, int numberToFetch){
        List<ActionMessage> actionMessages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(ActionMessageTable.TABLE_NAME, MessageTable._ID);
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

    public List<Attachment> getReversedAttachmentsInChat(String chatUuid, int startIndex, int numberToFetch){
        List<Attachment> attachments = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
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

    public List<Message> getReversedMessages(Chat chat, int startIndex, int numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
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

    public List<Message> getReversedMessagesByTime(Chat chat, int startIndex, int numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
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

    public List<Message> getReversedMessagesWithSearchParameters(Chat chat, String matchingText, boolean isFromMe, int startIndex, int numberToFetch){
        List<Message> messages = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        int finalRow = getMaxIdFromTable(MessageTable.TABLE_NAME, MessageTable._ID);
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
        values.put(AccountTable.ACCOUNT_EMAIL, account.getEmail());
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

        Cursor cursor = db.rawQuery(selectQuery, new String[] {email});
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

        Cursor cursor = db.rawQuery(selectQuery, new String[] {handle.getHandleID()});
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
            addHandle(new Handle(UUID.randomUUID(), account.getEmail(), Handle.HandleType.ME));
        }
    }

    public void updateAccount(String uuid, Account newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = accountToContentValues(newData);
        String selection = AccountTable.UUID + " = ?";

        UUID oldHandleUuid = getHandleByAccount(getAccountByUuid(uuid)).getUuid();

        db.update(AccountTable.TABLE_NAME, values, selection, new String[]{ uuid });
        updateHandle(oldHandleUuid.toString(), new Handle(oldHandleUuid, newData.getEmail(), Handle.HandleType.ME));
    }

    public void updateAccountByEmail(String email, Account newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = accountToContentValues(newData);
        String selection = AccountTable.ACCOUNT_EMAIL + " = ?";

        UUID oldHandleUuid = getHandleByAccount(getAccountByEmail(email)).getUuid();

        db.update(AccountTable.TABLE_NAME, values, selection, new String[]{ email });
        updateHandle(oldHandleUuid.toString(), new Handle(oldHandleUuid, newData.getEmail(), Handle.HandleType.ME));
    }

    public ActionMessage buildActionMessage(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(ActionMessageTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        ActionMessage actionMessage = new ActionMessage().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ActionMessageTable.UUID))))
                .setChat(getChatByUuid(cursor.getString(cursor.getColumnIndex(ActionMessageTable.CHAT_UUID)))).setActionText(cursor.getString(cursor.getColumnIndex(ActionMessageTable.ACTION_TEXT)))
                .setDate(cursor.getInt(cursor.getColumnIndex(ActionMessageTable.DATE)));

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
                .setFileType(cursor.getString(cursor.getColumnIndex(AttachmentTable.FILE_TYPE))).setTotalBytes(cursor.getInt(cursor.getColumnIndex(AttachmentTable.TOTAL_BYTES)));
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

    public void deleteAttachmentByUuid(String uuid){
        String whereClause = AttachmentTable.UUID + " = ?";
        getWritableDatabase().delete(AttachmentTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    private void deleteAttachmentByMacGuid(String macGuid){
        String whereClause = AttachmentTable.MAC_GUID + " = ?";
        getWritableDatabase().delete(AttachmentTable.TABLE_NAME, whereClause, new String[] { macGuid });
    }

    public Contact buildContact(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(ContactTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Contact contact = new Contact().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ContactTable.UUID))))
                .setFirstName(cursor.getString(cursor.getColumnIndex(ContactTable.FIRST_NAME))).setLastName(cursor.getString(cursor.getColumnIndex(ContactTable.LAST_NAME)))
                .setHandle(getHandleByUuid(cursor.getString(cursor.getColumnIndex(ContactTable.HANDLE_UUID))))
                .setDoNotDisturb(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ContactTable.IS_DO_NOT_DISTURB))))
                .setBlocked(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ContactTable.IS_BLOCKED))))
                .setContactPictureFileLocation(new FileLocationContainer(cursor.getString(cursor.getColumnIndex(ContactTable.CONTACT_PICTURE_FILE_LOCATION))));
        return contact;
    }

    public ContentValues contactToContentValues(Contact contact){
        ContentValues values = new ContentValues();

        values.put(ContactTable.UUID, contact.getUuid().toString());
        values.put(ContactTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(ContactTable.FIRST_NAME, contact.getFirstName());
        values.put(ContactTable.LAST_NAME, contact.getLastName());
        values.put(ContactTable.HANDLE_UUID, contact.getHandle().getUuid().toString());
        values.put(ContactTable.IS_DO_NOT_DISTURB, booleanToInteger(contact.isDoNotDisturb()));
        values.put(ContactTable.IS_BLOCKED, booleanToInteger(contact.isBlocked()));

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
        String selectQuery = "SELECT * FROM " + ContactTable.TABLE_NAME + " WHERE " + ContactTable.HANDLE_UUID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] { handle.getUuid().toString() });
        Contact contact = null;

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            contact = buildContact(cursor);
        }
        cursor.close();
        return contact;
    }

    public void addContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(ContactTable.TABLE_NAME, null, contactToContentValues(contact));
    }

    public void updateContact(String uuid, Contact newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = contactToContentValues(newData);
        String selection = ContactTable.UUID + " = ?";

        db.update(ContactTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    public void updateContactByHandle(Handle handle, Contact newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = contactToContentValues(newData);
        String selection = ContactTable.HANDLE_UUID + " = ?";

        db.update(ContactTable.TABLE_NAME, values, selection, new String[]{ handle.getUuid().toString() });
    }

    public Handle buildHandle(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(HandleTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Handle handle = new Handle().setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(HandleTable.UUID)))).setHandleID(cursor.getString(cursor.getColumnIndex(HandleTable.HANDLE_ID)))
                .setHandleType(Handle.HandleType.stringToHandleType(cursor.getString(cursor.getColumnIndex(HandleTable.HANDLE_TYPE))));
        return handle;
    }

    public ContentValues handleToContentValues(Handle handle){
        ContentValues values = new ContentValues();

        values.put(HandleTable.UUID, handle.getUuid().toString());
        values.put(HandleTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(HandleTable.HANDLE_ID, handle.getHandleID());
        values.put(HandleTable.HANDLE_TYPE, handle.getHandleType().getTypeName());

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

        Cursor cursor = db.rawQuery(selectQuery, new String[] { handleID });
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

    public void addHandle(Handle handle){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(HandleTable.TABLE_NAME, null, handleToContentValues(handle));
    }

    public void updateHandle(String uuid, Handle newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = handleToContentValues(newData);
        String selection = HandleTable.UUID + " = ?";

        db.update(HandleTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    public void updateHandleByHandleID(String handleID, Handle newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = handleToContentValues(newData);
        String selection = HandleTable.HANDLE_ID + " = ?";

        db.update(HandleTable.TABLE_NAME, values, selection, new String[]{ handleID });
    }

    public Chat buildChat(Cursor cursor){
        if (!(cursor.getString(cursor.getColumnIndex(ChatTable.ACCOUNT_UUID)).equals(weMessage.get().getCurrentAccount().getUuid().toString()))){
            return null;
        }

        Chat chat;
        ChatType chatType = ChatType.stringToHandleType(cursor.getString(cursor.getColumnIndex(ChatTable.CHAT_TYPE)));

        if (chatType == ChatType.PEER){
            chat = new PeerChat().setContact(getContactByUuid(cursor.getString(cursor.getColumnIndex(ChatTable.CONTACT_UUID))))
                    .setUuid(UUID.fromString(cursor.getString(cursor.getColumnIndex(ChatTable.UUID))))
                    .setMacGuid(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GUID))).setMacGroupID(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_GROUP_ID)))
                    .setMacChatIdentifier(cursor.getString(cursor.getColumnIndex(ChatTable.MAC_CHAT_IDENTIFIER)))
                    .setIsInChat(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.IS_IN_CHAT))))
                    .setHasUnreadMessages(integerToBoolean(cursor.getInt(cursor.getColumnIndex(ChatTable.HAS_UNREAD_MESSAGES))));
        }else {
            chat = new GroupChat().setDisplayName(cursor.getString(cursor.getColumnIndex(ChatTable.DISPLAY_NAME)))
                    .setParticipants(stringListToContacts(Arrays.asList(cursor.getString(cursor.getColumnIndex(ChatTable.PARTICIPANTS)).split(", "))))
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
            values.put(ChatTable.CONTACT_UUID, ((PeerChat) chat).getContact().getUuid().toString());
            values.putNull(ChatTable.PARTICIPANTS);
            values.putNull(ChatTable.DISPLAY_NAME);
            values.putNull(ChatTable.IS_DO_NOT_DISTURB);
        }else if (chat instanceof GroupChat){
            values.put(ChatTable.DISPLAY_NAME, ((GroupChat) chat).getDisplayName());
            values.put(ChatTable.PARTICIPANTS, StringUtils.join(contactsToStringList(((GroupChat) chat).getParticipants()), ", ", 2));
            values.put(ChatTable.IS_DO_NOT_DISTURB, booleanToInteger(((GroupChat) chat).isDoNotDisturb()));
            values.putNull(ChatTable.CONTACT_UUID);
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

    public void addChat(Chat chat){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(ChatTable.TABLE_NAME, null, chatToContentValues(chat));
    }

    public void updateChat(String uuid, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.UUID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    public void updateChatByMacGuid(String macGuid, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_GUID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macGuid });
    }

    public void updateChatByMacGroupID(String macGroupID, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_GROUP_ID + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macGroupID });
    }

    public void updateChatByMacChatIdentifier(String macChatIdentifier, Chat newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = chatToContentValues(newData);
        String selection = ChatTable.MAC_CHAT_IDENTIFIER + " = ?";

        db.update(ChatTable.TABLE_NAME, values, selection, new String[]{ macChatIdentifier });
    }

    public void deleteChatByUuid(String uuid){
        String whereClause = ChatTable.UUID + " = ?";
        final Chat chat = getChatByUuid(uuid);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();

        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { uuid });
    }

    public void deleteChatByMacGuid(String macGuid){
        String whereClause = ChatTable.MAC_GUID + " = ?";
        final Chat chat = getChatByMacGuid(macGuid);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();

        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { macGuid });
    }

    public void deleteChatByMacGroupID(String macGroupID){
        String whereClause = ChatTable.MAC_GROUP_ID + " = ?";
        final Chat chat = getChatByMacGroupId(macGroupID);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteActionMessage(actionMessage.getUuid().toString());
                }
            }
        }.start();
        getWritableDatabase().delete(ChatTable.TABLE_NAME, whereClause, new String[] { macGroupID });
    }

    public void deleteChatByMacChatIdentifier(String macChatIdentifier){
        String whereClause = ChatTable.MAC_CHAT_IDENTIFIER + " = ?";
        final Chat chat = getChatByMacChatIdentifier(macChatIdentifier);

        new Thread(){
            @Override
            public void run() {
                for (Message message : getReversedMessages(chat, 0, Integer.MAX_VALUE)){
                    deleteMessageByUuid(message.getUuid().toString());
                }
                for (ActionMessage actionMessage : getReversedActionMessages(chat, 0, Integer.MAX_VALUE)){
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
                .setSender(getContactByUuid(cursor.getString(cursor.getColumnIndex(MessageTable.SENDER_UUID))))
                .setAttachments(stringListToAttachments(Arrays.asList(cursor.getString(cursor.getColumnIndex(MessageTable.ATTACHMENTS)).split(", "))))
                .setText(cursor.getString(cursor.getColumnIndex(MessageTable.TEXT))).setDateSent(cursor.getInt(cursor.getColumnIndex(MessageTable.DATE_SENT)))
                .setDateDelivered(cursor.getInt(cursor.getColumnIndex(MessageTable.DATE_DELIVERED))).setDateRead(cursor.getInt(cursor.getColumnIndex(MessageTable.DATE_READ)))
                .setHasErrored(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.ERRORED)))).setIsSent(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_SENT))))
                .setDelivered(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_DELIVERED)))).setRead(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_READ))))
                .setFinished(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_FINISHED)))).setFromMe(integerToBoolean(cursor.getInt(cursor.getColumnIndex(MessageTable.IS_FROM_ME))));
        return message;
    }

    public ContentValues messageToContentValues(Message message){
        ContentValues values = new ContentValues();

        values.put(MessageTable.UUID, message.getUuid().toString());
        values.put(MessageTable.ACCOUNT_UUID, weMessage.get().getCurrentAccount().getUuid().toString());
        values.put(MessageTable.MAC_GUID, message.getMacGuid());
        values.put(MessageTable.CHAT_UUID, message.getChat().getUuid().toString());
        values.put(MessageTable.SENDER_UUID, message.getSender().getUuid().toString());
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

    public void addMessage(Message message){
        SQLiteDatabase db = getWritableDatabase();
        db.insert(MessageTable.TABLE_NAME, null, messageToContentValues(message));
    }

    public void updateMessage(String uuid, Message newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = messageToContentValues(newData);
        String selection = MessageTable.UUID + " = ?";

        db.update(MessageTable.TABLE_NAME, values, selection, new String[]{ uuid });
    }

    public void updateMessageByMacGuid(String macGuid, Message newData){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = messageToContentValues(newData);
        String selection = MessageTable.MAC_GUID + " = ?";

        db.update(MessageTable.TABLE_NAME, values, selection, new String[]{ macGuid });
    }

    public void deleteMessageByUuid(String uuid){
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

    public void deleteMessageByMacGuid(String macGuid){
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

    private List<String> contactsToStringList(List<Contact> contacts){
        List<String> stringList = new ArrayList<>();

        for (Contact contact : contacts){
            stringList.add(contact.getHandle().getHandleID());
        }
        return stringList;
    }

    private List<Contact> stringListToContacts(List<String> stringList){
        List<Contact> contacts = new ArrayList<>();

        for (String s : stringList){
            contacts.add(getContactByHandle(getHandleByHandleID(s)));
        }
        return contacts;
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

    private Integer getMaxIdFromTable(String tableName, String idRow){
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT MAX(" + idRow + ") FROM " + tableName;

        Cursor cursor = db.rawQuery(selectQuery, null);

        Integer result = null;
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            result = cursor.getInt(0);
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
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String HANDLE_UUID = "handle_uuid";
        public static final String IS_DO_NOT_DISTURB = "is_do_not_disturb";
        public static final String IS_BLOCKED = "is_blocked";
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
        public static final String CONTACT_UUID = "contact_uuid";
        public static final String DISPLAY_NAME = "display_name";
        public static final String PARTICIPANTS = "participants";
        public static final String CHAT_PICTURE_FILE_LOCATION = "chat_picture_file_location";
    }

    public static class HandleTable {
        public static final String TABLE_NAME = "handles";
        public static final String _ID = "_id";
        public static final String UUID = "uuid";
        public static final String ACCOUNT_UUID = "account_uuid";
        public static final String HANDLE_ID = "handle_id";
        public static final String HANDLE_TYPE = "handle_type";
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
    }

    public static class AccountNotLoggedInException extends NullPointerException { }
}