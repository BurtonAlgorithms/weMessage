package scott.wemessage.app.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.webkit.MimeTypeMap;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.chats.SmsGroupChat;
import scott.wemessage.app.models.sms.chats.SmsPeerChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.commons.utils.StringUtils;

public final class MmsDatabase {

    private weMessage app;

    public MmsDatabase(weMessage app){
        this.app = app;
    }

    public MmsMessage getMessageFromUri(Uri uri){
        return getMessageFromUri(null, uri);
    }

    public MmsMessage getMessageFromUri(String taskIdentifier, Uri uri){
        if (!MmsManager.hasSmsPermissions()) return null;

        String uriString = uri.toString();
        MmsMessage mmsMessage = null;
        boolean isMms = false;

        try {
            if (uriString.contains("sms") && uriString.contains("mms")) {
                Cursor cursor = app.getContentResolver().query(uri, new String[]{"ct_t"}, null, null, null);

                if (cursor.moveToFirst()) {
                    String contentType = cursor.getString(cursor.getColumnIndex("ct_t"));
                    isMms = !StringUtils.isEmpty(contentType) && (contentType.equalsIgnoreCase("application/vnd.wap.multipart.related") || contentType.equalsIgnoreCase("application/vnd.wap.multipart.mixed"));
                }
                cursor.close();
            } else if (uriString.contains("mms")) {
                isMms = true;
            }

            if (isMms) {
                Cursor mmsCursor = app.getContentResolver().query(uri, new String[]{"date_sent", "date", "msg_box", "_id", "thread_id"}, null, null, null);

                if (mmsCursor.moveToFirst()) {
                    mmsMessage = buildMmsMessage(taskIdentifier, mmsCursor);
                    mmsCursor.close();
                } else {
                    mmsCursor.close();

                    Uri newUri = Uri.parse("content://mms/" + uriString.substring(uriString.lastIndexOf("/") + 1));
                    Cursor mmsCursorRetry = app.getContentResolver().query(newUri, new String[]{"date_sent", "date", "msg_box", "_id", "thread_id"}, null, null, null);

                    if (mmsCursorRetry.moveToFirst()) mmsMessage = buildMmsMessage(taskIdentifier, mmsCursorRetry);
                    mmsCursorRetry.close();
                }
            } else {
                Cursor smsCursor = app.getContentResolver().query(uri, new String[]{"address", "date_sent", "date", "body", "type", "_id", "thread_id"}, null, null, null);

                if (smsCursor.moveToFirst()){
                    mmsMessage = buildSmsMessage(smsCursor);
                    smsCursor.close();
                }else {
                    smsCursor.close();

                    Uri newUri = Uri.parse("content://sms/" + uriString.substring(uriString.lastIndexOf("/") + 1));
                    Cursor smsCursorRetry = app.getContentResolver().query(newUri, new String[]{"address", "date_sent", "date", "body", "type", "_id", "thread_id"}, null, null, null);

                    if (smsCursorRetry.moveToFirst()) mmsMessage = buildSmsMessage(smsCursorRetry);
                    smsCursorRetry.close();
                }
            }

            if (mmsMessage != null) {
                String firstMessageId = mmsMessage.getIdentifier();
                String messageId = mmsMessage.getIdentifier();

                if (app.getMmsManager().getSmsChat(mmsMessage.getChat().getIdentifier()) == null) {
                    app.getMessageManager().addChat(mmsMessage.getChat(), false);
                }

                if (app.getMmsManager().getMmsMessage(messageId) != null) {
                    long largestMessageId = getLargestMessageId();
                    if (largestMessageId == -1L) return null;

                    messageId = String.valueOf(largestMessageId + 1L);
                }

                if (!firstMessageId.equals(messageId)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("_id", messageId);

                    Uri updateUri = Uri.parse("content://mms-sms/conversations/" + mmsMessage.getChat().getIdentifier());

                    if (isMms) {
                        app.getContentResolver().update(updateUri, contentValues, "_id = " + firstMessageId, null);
                    } else {
                        app.getContentResolver().update(updateUri, contentValues, "_id = " + firstMessageId, null);
                    }

                    mmsMessage.setIdentifier(messageId);
                }
            }
        }catch (Exception ex){
            AppLogger.error("An error occurred while parsing a message from a URI", ex);
        }

        return mmsMessage;
    }

    protected void markChatAsRead(String threadId){
        if (!MmsManager.hasSmsPermissions()) return;

        Uri smsUri = Uri.parse("content://sms/");
        Uri mmsUri = Uri.parse("content://mms/");
        ContentResolver contentResolver = app.getContentResolver();

        ContentValues readValues = new ContentValues();
        readValues.put("read", 1);
        readValues.put("seen", 1);

        Cursor smsCursor = contentResolver.query(smsUri, new String[] {"_id"}, "thread_id = " + threadId + " AND read = 0", null, null);

        if (smsCursor != null){
            if (smsCursor.moveToFirst()){
                do {
                    String messageId = smsCursor.getString(smsCursor.getColumnIndex("_id"));

                    contentResolver.update(smsUri, readValues, "_id = " + messageId, null);
                }while (smsCursor.moveToNext());
            }
            smsCursor.close();
        }

        Cursor mmsCursor = contentResolver.query(mmsUri, new String[] {"_id"}, "thread_id = " + threadId + " AND read = 0", null, null);

        if (mmsCursor != null){
            if (mmsCursor.moveToFirst()){
                do {
                    String messageId = mmsCursor.getString(mmsCursor.getColumnIndex("_id"));

                    contentResolver.update(mmsUri, readValues, "_id = " + messageId, null);
                }while (mmsCursor.moveToNext());
            }
            mmsCursor.close();
        }
    }

    protected void deleteChat(String threadId){
        if (!MmsManager.hasSmsPermissions()) return;

        app.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId),null,null);
    }

    protected Uri addSmsMessage(Object[] smsExtra){
        if (!MmsManager.hasSmsPermissions()) return null;

        StringBuilder text = new StringBuilder("");
        SmsMessage[] smsMessages = new SmsMessage[smsExtra.length];

        for (int i = 0; i < smsMessages.length; i++){
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            text.append(sms.getMessageBody());

            if (i == smsMessages.length - 1) {
                ContentValues values = new ContentValues();
                values.put("address", sms.getOriginatingAddress());
                values.put("body", text.toString());
                values.put("date", sms.getTimestampMillis());
                values.put("status", sms.getStatus());
                values.put("read", 0);
                values.put("seen", 0);
                values.put("type", 1);

                return app.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
            }
        }

        return null;
    }

    protected void deleteMessage(MmsMessage message){
        if (!MmsManager.hasSmsPermissions()) return;

        if (message.isMms()){
            app.getContentResolver().delete(Uri.parse("content://mms/" + message.getIdentifier()), null, null);
        }else {
            app.getContentResolver().delete(Uri.parse("content://sms/" + message.getIdentifier()), null, null);
        }
    }

    public void executeChatSync(){
        if (!MmsManager.hasSmsPermissions()) return;

        Uri uri = Uri.parse("content://mms-sms/conversations/");
        Cursor chatQuery = app.getContentResolver().query(uri, new String[]{ "thread_id", "_id", "ct_t", "read" }, null, null, null);

        if (chatQuery == null) return;

        if (chatQuery.moveToFirst()){
            do {
                try {
                    String threadId = chatQuery.getString(chatQuery.getColumnIndex("thread_id"));

                    if (app.getMmsManager().getSmsChat(threadId) != null) continue;

                    SmsChat smsChat = buildSmsChat(chatQuery);

                    if (smsChat != null) {
                        app.getMmsManager().getSyncingChats().put(threadId, smsChat);
                        app.getMmsManager().addChat(smsChat);
                    }
                }catch (Exception ex){
                    AppLogger.error("An error occurred while fetching a chat from the database during SMS Chat Sync", ex);
                }
            }while (chatQuery.moveToNext());
        }

        chatQuery.close();
        app.getMessageManager().refreshChats(false, true);
    }

    public void executeMessageSync(String identifier){
        if (!MmsManager.hasSmsPermissions()) return;

        Uri uri = Uri.parse("content://mms-sms/conversations/" + identifier);
        Cursor initialQuery = app.getContentResolver().query(uri, new String[]{ "_id", "ct_t", "address" }, null, null, null);

        if (initialQuery == null) return;

        if (initialQuery.moveToFirst()){
            do {
                try {
                    String firstMessageId = initialQuery.getString(initialQuery.getColumnIndex("_id"));
                    String messageId = initialQuery.getString(initialQuery.getColumnIndex("_id"));
                    String contentType = initialQuery.getString(initialQuery.getColumnIndex("ct_t"));
                    boolean isMms = !StringUtils.isEmpty(contentType) && (contentType.equalsIgnoreCase("application/vnd.wap.multipart.related") || contentType.equalsIgnoreCase("application/vnd.wap.multipart.mixed"));

                    if (app.getMmsManager().getMmsMessage(messageId) != null) {
                        long largestMessageId = getLargestMessageId();
                        if (largestMessageId == -1L) continue;

                        messageId = String.valueOf(largestMessageId + 1L);
                    }

                    if (!firstMessageId.equals(messageId)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_id", messageId);

                        if (isMms) {
                            app.getContentResolver().update(uri, contentValues, "_id = " + firstMessageId, null);
                        } else {
                            app.getContentResolver().update(uri, contentValues, "_id = " + firstMessageId, null);
                        }
                    }

                    if (isMms) {
                        Cursor mmsCursor = app.getContentResolver().query(Uri.parse("content://mms/"), new String[]{"date_sent", "date", "msg_box", "_id", "thread_id"}, "_id = " + messageId, null, null);

                        if (mmsCursor.moveToFirst()) {
                            MmsMessage message = buildMmsMessage(null, mmsCursor);

                            if (message != null)
                                app.getMmsManager().addMessage(message);
                        }

                        mmsCursor.close();
                    } else {
                        Cursor smsCursor = app.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"address", "date_sent", "date", "body", "type", "_id", "thread_id"}, "_id = " + messageId, null, null);

                        if (smsCursor.moveToFirst()) {
                            MmsMessage message = buildSmsMessage(smsCursor);

                            if (message != null)
                                app.getMmsManager().addMessage(message);
                        }

                        smsCursor.close();
                    }
                }catch (Exception ex){
                    AppLogger.error("An error occurred while fetching a message from the database during SMS Message Sync", ex);
                }
            }while (initialQuery.moveToNext());
        }

        initialQuery.close();
        app.getMmsManager().getSyncingChats().remove(identifier);
        app.getMessageManager().updateChat(identifier, (Chat) app.getMmsManager().getSmsChat(identifier),false);
    }

    private SmsChat buildSmsChat(Cursor chatCursor){
        SmsChat smsChat = null;
        List<Handle> handles = new ArrayList<>();

        String threadId = chatCursor.getString(chatCursor.getColumnIndex("thread_id"));
        String messageId = chatCursor.getString(chatCursor.getColumnIndex("_id"));
        String contentType = chatCursor.getString(chatCursor.getColumnIndex("ct_t"));
        boolean hasUnreadMessages = !integerToBoolean(chatCursor.getInt(chatCursor.getColumnIndex("read")));

        if (!StringUtils.isEmpty(contentType) && (contentType.equalsIgnoreCase("application/vnd.wap.multipart.related") || contentType.equalsIgnoreCase("application/vnd.wap.multipart.mixed"))) {
            Uri addressUri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", messageId));
            Cursor cursorAddress = app.getContentResolver().query(addressUri, new String[] { "address" }, "msg_id = " + messageId, null, null);

            if (cursorAddress.moveToFirst()) {
                do {
                    String number = cursorAddress.getString(cursorAddress.getColumnIndex("address"));

                    if (!StringUtils.isEmpty(number)){
                        handles.add(processHandle(number));
                    }
                }while (cursorAddress.moveToNext());
            }

            cursorAddress.close();
        } else {
            Cursor addressCursor = app.getContentResolver().query(Uri.parse("content://sms"), new String[] { "address" }, "_id = " + messageId, null, null);

            if (addressCursor.moveToFirst()) {
                String phone = addressCursor.getString(addressCursor.getColumnIndex("address"));
                handles.add(processHandle(phone));
            }

            addressCursor.close();
        }

        if (handles.isEmpty()) return null;

        if (handles.size() == 1){
            smsChat = new SmsPeerChat(threadId, handles.get(0), hasUnreadMessages);
        }

        if (handles.size() > 1){
            handles.removeAll(Collections.singleton(weMessage.get().getCurrentSession().getSmsHandle()));

            if (handles.isEmpty()){
                smsChat = new SmsPeerChat(threadId, weMessage.get().getCurrentSession().getSmsHandle(), hasUnreadMessages);
            }else if (handles.size() == 1) {
                smsChat = new SmsPeerChat(threadId, handles.get(0), hasUnreadMessages);
            } else {
                smsChat = new SmsGroupChat(threadId, handles, null, hasUnreadMessages, false);
            }
        }

        return smsChat;
    }

    private MmsMessage buildMmsMessage(String taskIdentifier, Cursor mmsCursor){
        MmsMessage mmsMessage = null;
        Handle sender;
        boolean isFromMe = false;
        List<Attachment> attachments = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder("");

        String messageId = mmsCursor.getString(mmsCursor.getColumnIndex("_id"));
        String threadId = mmsCursor.getString(mmsCursor.getColumnIndex("thread_id"));
        Date dateSent = processDate(mmsCursor.getString(mmsCursor.getColumnIndex("date_sent")));
        Date dateDelivered = processDate(mmsCursor.getString(mmsCursor.getColumnIndex("date")));
        boolean isErrored = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box")) == 5;
        boolean isDelivered = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box")) == 2;
        boolean skipAttachments = !StringUtils.isEmpty(taskIdentifier) && app.getMmsManager().getMmsMessage(taskIdentifier) != null;

        String address = getMmsSender(messageId);

        if (StringUtils.isEmpty(address)) {
            sender = null;
        } else {
            if (app.getCurrentSession().getSmsHandle().getHandleID().equals(Handle.parseHandleId(address))) {
                sender = app.getCurrentSession().getSmsHandle();
                isFromMe = true;
            } else {
                sender = processHandle(address);
            }
        }

        if (skipAttachments){
            attachments.addAll(app.getMmsManager().getMmsMessage(taskIdentifier).getAttachments());
        }

        Cursor partCursor = app.getContentResolver().query(Uri.parse("content://mms/part"), new String[]{"_id", "ct", "_data", "text"}, "mid = " + messageId, null, null);

        if (partCursor.moveToFirst()) {
            do {
                String partId = partCursor.getString(partCursor.getColumnIndex("_id"));
                String type = partCursor.getString(partCursor.getColumnIndex("ct"));
                String data = partCursor.getString(partCursor.getColumnIndex("_data"));

                if (type.equals("text/plain")) {
                    if (data != null) {
                        textBuilder.append(getMmsText(partId));
                    } else {
                        textBuilder.append(partCursor.getString(partCursor.getColumnIndex("text")));
                    }
                } else {
                    if (skipAttachments) continue;

                    FileLocationContainer fileLocationContainer = null;
                    String fileName;

                    if (StringUtils.isEmpty(data) || StringUtils.isEmpty(type)) continue;

                    if (data.contains("/")) {
                        String[] split = data.split("/");
                        fileName = split[split.length - 1];
                    } else {
                        fileName = data;
                    }

                    try {
                        if ((MimeType.getTypeFromString(type) == MimeType.IMAGE || type.equals("image/jpg") || type.equals("image/bmp")) && !type.equals("image/gif")) {
                            fileLocationContainer = processMmsImage(partId, fileName);
                        } else if (MimeTypeMap.getSingleton().getExtensionFromMimeType(type) != null) {
                            fileLocationContainer = processAttachment(partId, fileName, "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type));
                        }
                    } catch (IOException ex) {
                        AppLogger.error("An error occurred while loading an attachment from an MMS message", ex);
                    }

                    if (fileLocationContainer != null) {
                        Attachment attachment = new Attachment(UUID.randomUUID(), "", fileLocationContainer.getFile().getName(),
                                fileLocationContainer, type, fileLocationContainer.getFile().length());

                        attachments.add(attachment);
                    }
                }
            } while (partCursor.moveToNext());
        }
        partCursor.close();

        Chat chat = (Chat) app.getMmsManager().getSmsChat(threadId);

        if (chat == null){
            Cursor chatQuery = app.getContentResolver().query(Uri.parse("content://mms-sms/conversations/"), new String[]{ "thread_id", "_id", "ct_t", "read" }, "thread_id = " + threadId, null, null);

            if (chatQuery.moveToFirst()){
                chat = (Chat) buildSmsChat(chatQuery);
            }
            chatQuery.close();
        }

        if (sender != null && chat != null) {
            mmsMessage = new MmsMessage(messageId,
                    chat,
                    sender,
                    attachments,
                    textBuilder.toString(),
                    dateSent,
                    dateDelivered,
                    isErrored,
                    isDelivered,
                    isFromMe,
                    true);
        }

        return mmsMessage;
    }

    private MmsMessage buildSmsMessage(Cursor smsCursor){
        MmsMessage mmsMessage = null;
        Handle sender = null;

        String messageId = smsCursor.getString(smsCursor.getColumnIndex("_id"));
        String threadId = smsCursor.getString(smsCursor.getColumnIndex("thread_id"));
        String address = smsCursor.getString(smsCursor.getColumnIndex("address"));
        String text = smsCursor.getString(smsCursor.getColumnIndex("body"));
        Date dateSent = processDate(smsCursor.getString(smsCursor.getColumnIndex("date_sent")));
        Date dateDelivered = processDate(smsCursor.getString(smsCursor.getColumnIndex("date")));
        int type = smsCursor.getInt(smsCursor.getColumnIndex("type"));
        boolean isFromMe = false;
        boolean errored = type == 5;

        if (type == 2 || type == 3 || type == 4 || type == 5 || type == 6){
            isFromMe = true;
        }

        if (!StringUtils.isEmpty(address)){
            if (isFromMe) {
                sender = app.getCurrentSession().getSmsHandle();
            } else {
                sender = processHandle(address);
            }
        }

        Chat chat = (Chat) app.getMmsManager().getSmsChat(threadId);

        if (chat == null){
            Cursor chatQuery = app.getContentResolver().query(Uri.parse("content://mms-sms/conversations/"), new String[]{ "thread_id", "_id", "ct_t", "read" }, "thread_id = " + threadId, null, null);

            if (chatQuery.moveToFirst()){
                chat = (Chat) buildSmsChat(chatQuery);
            }
            chatQuery.close();
        }

        if (sender != null && chat != null){
            mmsMessage = new MmsMessage(messageId,
                    chat,
                    sender,
                    new ArrayList<Attachment>(),
                    text,
                    dateSent,
                    dateDelivered,
                    errored,
                    type == 2 && (dateSent != null || dateDelivered != null),
                    isFromMe,
                    false);
        }

        return mmsMessage;
    }

    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = app.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private String getMmsSender(String messageId) {
        String addressUri = MessageFormat.format("content://mms/{0}/addr", messageId);
        Uri uriAddress = Uri.parse(addressUri);
        Cursor addressCursor = app.getContentResolver().query(uriAddress, new String[] { "address" }, "type = 137 AND msg_id = " + messageId, null, null);
        String address = null;

        if (addressCursor.moveToFirst()) {
            address = addressCursor.getString(addressCursor.getColumnIndex("address"));
        }

        addressCursor.close();
        return address;
    }

    private FileLocationContainer processMmsImage(String partId, String name) throws IOException {
        Bitmap photo;
        InputStream is = null;
        FileOutputStream out = null;
        File newFile = new File(app.getAttachmentFolder(), name + "Imported.png");

        if (newFile.exists()) return new FileLocationContainer(newFile);

        try {
            Uri partURI = Uri.parse("content://mms/part/" + partId);
            is = app.getContentResolver().openInputStream(partURI);
            photo = BitmapFactory.decodeStream(is);

            is.close();
            newFile.createNewFile();

            out = new FileOutputStream(newFile);
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.close();
        }catch (OutOfMemoryError error){
            System.gc();
            return null;
        }finally {
            photo = null;

            if (is != null) is.close();
            if (out != null) out.close();
        }

        return new FileLocationContainer(newFile);
    }

    private FileLocationContainer processAttachment(String partId, String name, String extension) throws IOException {
        File newFile = new File(app.getAttachmentFolder(), name + extension);
        if (newFile.exists()) return new FileLocationContainer(newFile);

        Uri partURI = Uri.parse("content://mms/part/" + partId);
        InputStream inputStream = app.getContentResolver().openInputStream(partURI);

        newFile.createNewFile();
        FileUtils.writeInputStreamToFile(inputStream, newFile);

        return new FileLocationContainer(newFile);
    }

    private Handle processHandle(String address){
        Handle handle = app.getMessageDatabase().getHandleByHandleID(address);

        if (handle == null) {
            handle = new Handle(UUID.randomUUID(), address, Handle.HandleType.SMS, false, false);
            app.getMessageManager().addHandle(handle, false);
        }

        return handle;
    }

    private Date processDate(String date){
        if (StringUtils.isEmpty(date)) return null;
        Long dateLong = Long.parseLong(date);

        if (dateLong < 100) return null;

        if (new DateTime(dateLong).getYear() < 2000){
            return processDate(String.valueOf(dateLong * 10L));
        }

        return new Date(dateLong);
    }

    private Long getLargestMessageId(){
        Long smsMaxId;
        Long mmsMaxId;

        try {
            Cursor smsMaxIdCursor = app.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"MAX(_id) as max_id"}, null, null, null);
            smsMaxIdCursor.moveToFirst();
            smsMaxId = smsMaxIdCursor.getLong(0);
            smsMaxIdCursor.close();
        }catch (Exception ex){
            smsMaxId = -1L;
        }

        try {
            Cursor mmsMaxIdCursor = app.getContentResolver().query(Uri.parse("content://mms/"), new String[]{"MAX(_id) as max_id"}, null, null, null);
            mmsMaxIdCursor.moveToFirst();
            mmsMaxId = mmsMaxIdCursor.getLong(0);
            mmsMaxIdCursor.close();
        }catch (Exception ex){
            mmsMaxId = -1L;
        }

        return smsMaxId > mmsMaxId ? smsMaxId : mmsMaxId;
    }

    private boolean integerToBoolean(Integer integer){
        if (integer > 1 || integer < 0) throw new ArrayIndexOutOfBoundsException("Parsing a boolean from an int must be either 0 or 1. Found: " + integer);
        return integer == 1;
    }
}
