package scott.wemessage.app.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.webkit.MimeTypeMap;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
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

    protected HashMap<String, SmsChat> getChats(){
        HashMap<String, SmsChat> chats = new HashMap<>();

        Uri uri = Uri.parse("content://mms-sms/conversations/");
        Cursor chatQuery = app.getContentResolver().query(uri, new String[]{ "thread_id" }, null, null, null);

        if (chatQuery == null) return chats;

        if (chatQuery.moveToFirst()){
            do {
                SmsChat chat = buildChat(chatQuery.getString(chatQuery.getColumnIndex("thread_id")));

                if (chat != null) {
                    chats.put(((Chat) chat).getIdentifier(), chat);
                }
            }while (chatQuery.moveToNext());
        }

        chatQuery.close();

        return chats;
    }

    public SmsPeerChat getChatByHandle(Handle handle){
        for (SmsChat chat : app.getMmsManager().getChats().values()){
            if (chat instanceof SmsPeerChat){
                if (((SmsPeerChat) chat).getHandle().equals(handle)) return (SmsPeerChat) chat;
            }
        }
        return null;
    }

    protected List<MmsMessage> getReversedMessagesByTime(String threadId, long startIndex, long numberToFetch){
        List<MmsMessage> messages = new ArrayList<>();
        Uri uri = Uri.parse("content://mms-sms/conversations/" + threadId);

        Cursor rowCursor = app.getContentResolver().query(uri, new String[] {"_id"}, null, null, null);
        long finalRow;

        try {
            rowCursor.moveToLast();
            finalRow = rowCursor.getLong(rowCursor.getColumnIndex("_id"));
            rowCursor.close();
        }catch (Exception ex){ return messages; }

        long start = finalRow - startIndex;
        Cursor cursor = app.getContentResolver().query(uri, new String[]{ "_id" }, "_id <= " + start, null, "date DESC LIMIT " + numberToFetch);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String messageId = cursor.getString(cursor.getColumnIndex("_id"));
                MmsMessage message = app.getMmsManager().getMmsMessage(messageId, false);

                if (message != null) {
                    messages.add(message);
                }
                cursor.moveToNext();
            }
        }

        cursor.close();

        return messages;
    }

    public MmsMessage getLastMessageFromChat(String threadId){
        MmsMessage message = null;
        Uri uri = Uri.parse("content://mms-sms/conversations/");
        Cursor initialQuery = app.getContentResolver().query(uri, new String[]{ "_id" }, "thread_id = " + threadId, null, null);

        if (initialQuery == null) return null;

        if (initialQuery.moveToFirst()){
            String messageId = initialQuery.getString(initialQuery.getColumnIndex("_id"));
            message = app.getMmsManager().getMmsMessage(messageId, true);
        }

        initialQuery.close();

        return message;
    }

    public String getMessageId(Uri uri){
        Cursor cursor = app.getContentResolver().query(uri, new String[] { "_id" }, null, null, null);
        String id = null;

        if (cursor == null) return null;

        if (cursor.moveToFirst()){
            id = cursor.getString(cursor.getColumnIndex("_id"));
        }

        cursor.close();
        return id;
    }

    private String getSmsMessageId(String address, String body, long date){
        String id = null;
        Cursor query = app.getContentResolver().query(Uri.parse("content://sms"),
                new String[] { "_id" }, "address = \"" + address + "\" AND body = \"" + body +"\" AND date = " + date, null, null);

        if (query == null) return null;

        if (query.moveToFirst()){
            id = query.getString(query.getColumnIndex("_id"));
        }

        query.close();
        return id;
    }

    protected SmsChat buildChat(String threadId){
        SmsChat smsChat = null;
        List<Handle> handles = new ArrayList<>();
        boolean hasUnreadMessages;

        ContentResolver contentResolver = app.getContentResolver();
        Uri uri = Uri.parse("content://mms-sms/conversations/");
        Cursor initialQuery = contentResolver.query(uri, new String[]{ "_id", "ct_t", "read" }, "thread_id = " + threadId, null, null);

        if (initialQuery == null) return null;

        if (initialQuery.moveToFirst()){
            hasUnreadMessages = !integerToBoolean(initialQuery.getInt(initialQuery.getColumnIndex("read")));
            String messageId = initialQuery.getString(initialQuery.getColumnIndex("_id"));
            String contentType = initialQuery.getString(initialQuery.getColumnIndex("ct_t"));

            if (!StringUtils.isEmpty(contentType) && contentType.equals("application/vnd.wap.multipart.related")) {
                Uri addressUri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", messageId));
                Cursor cursorAddress = app.getContentResolver().query(addressUri, new String[] { "address" }, "msg_id = " + messageId, null, null);

                if (cursorAddress.moveToFirst()) {
                    do {
                        String number = cursorAddress.getString(cursorAddress.getColumnIndex("address"));

                        if (!StringUtils.isEmpty(number) && PhoneNumberUtil.getInstance().isPossibleNumber(number, Resources.getSystem().getConfiguration().locale.getCountry())){
                            Handle handle = app.getMessageDatabase().getHandleByHandleID(number);

                            if (handle == null){
                                handle = new Handle(UUID.randomUUID(), number, Handle.HandleType.SMS, false, false);
                                app.getMessageManager().addHandle(handle, false);
                            }

                            handles.add(handle);
                        }
                    }while (cursorAddress.moveToNext());
                }

                cursorAddress.close();
            } else {
                Cursor addressCursor = contentResolver.query(Uri.parse("content://sms"), new String[] { "address" }, "_id = " + messageId, null, null);

                if (addressCursor.moveToFirst()) {
                    String phone = addressCursor.getString(addressCursor.getColumnIndex("address"));
                    Handle handle = app.getMessageDatabase().getHandleByHandleID(phone);

                    if (handle == null) {
                        handle = new Handle(UUID.randomUUID(), phone, Handle.HandleType.SMS, false, false);
                        app.getMessageManager().addHandle(handle, false);
                    }

                    handles.add(handle);
                }

                addressCursor.close();
            }

            FileLocationContainer fileLocationContainer = null;
            boolean isDoNotDisturb = false;

            if (app.getMessageDatabase().getSmsChatByThreadId(threadId) != null){
                SmsChat dbSmsChat = app.getMessageDatabase().getSmsChatByThreadId(threadId);

                fileLocationContainer = ((Chat) dbSmsChat).getChatPictureFileLocation();

                if (dbSmsChat instanceof GroupChat){
                    isDoNotDisturb = ((GroupChat) dbSmsChat).isDoNotDisturb();
                }
            }

            if (handles.isEmpty()){  return null; }

            if (handles.size() == 1){
                smsChat = new SmsPeerChat(threadId, handles.get(0), hasUnreadMessages);
            }

            if (handles.size() > 1){
                smsChat = new SmsGroupChat(threadId, handles, fileLocationContainer, hasUnreadMessages, isDoNotDisturb);
            }

            if (app.getMessageDatabase().getSmsChatByThreadId(threadId) == null && smsChat != null){
                app.getMessageDatabase().addSmsChat(smsChat);
            }
        }
        initialQuery.close();

        return smsChat;
    }

    protected MmsMessage buildMessage(String messageId){
        MmsMessage mmsMessage = null;

        if (isMessageMms(messageId)) {
            mmsMessage = assembleMmsMessage(messageId);
        } else {
            mmsMessage = assembleSmsMessage(messageId);
        }

        return mmsMessage;
    }

    protected void markChatAsRead(String threadId){
        ContentValues readValues = new ContentValues();
        readValues.put("read", 1);

        Uri uri = Uri.parse("content://mms-sms/complete-conversations/");
        ContentResolver contentResolver = app.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[] {"_id"}, "thread_id = " + threadId + " AND read = 0", null, null);

        if (cursor != null){
            if (cursor.moveToFirst()){
                while (!cursor.isAfterLast()){
                    String messageId = cursor.getString(cursor.getColumnIndex("_id"));

                    contentResolver.update(uri, readValues, "_id = " + messageId, null);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
    }

    protected void updateChat(String threadId, SmsChat newData){
        app.getMessageDatabase().updateSmsChatByThreadId(threadId, newData);
    }

    protected void deleteChat(String threadId){
        app.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId),null,null);
        app.getMessageDatabase().deleteSmsChatByThreadId(threadId);

        for (Attachment a : app.getMessageDatabase().getAttachmentsBySmsThreadId(threadId)){
            a.getFileLocation().getFile().delete();
            app.getMessageDatabase().deleteAttachmentByUuid(a.getUuid().toString());
        }
    }

    public String addSmsMessage(Object[] smsExtra){
        String address = "";
        long timeStampMillis = 0L;
        StringBuilder text = new StringBuilder("");
        SmsMessage[] smsMessages = new SmsMessage[smsExtra.length];

        for (int i = 0; i < smsMessages.length; i++){
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            text.append(sms.getMessageBody());

            if (i == smsMessages.length - 1) {
                address = sms.getOriginatingAddress();
                timeStampMillis = sms.getTimestampMillis();

                ContentValues values = new ContentValues();
                values.put("address", address);
                values.put("body", text.toString());
                values.put("date", timeStampMillis);
                values.put("status", sms.getStatus());
                values.put("read", 0);
                values.put("type", 1);

                app.getContentResolver().insert(Uri.parse("content://sms"), values);
            }
        }

        return getSmsMessageId(address, text.toString(), timeStampMillis);
    }

    protected void deleteMessage(String messageId){
        Uri completeConversationUri = Uri.parse("content://mms-sms/complete-conversations/");
        Cursor initialQuery = app.getContentResolver().query(completeConversationUri, new String[] {"transport_type"}, "_id = " + messageId, null, null);

        if (initialQuery == null) return;

        if (initialQuery.moveToFirst()){
            String transportType = initialQuery.getString(initialQuery.getColumnIndex("transport_type"));

            if (transportType.equals("sms")){
                app.getContentResolver().delete(Uri.parse("content://sms/" + messageId), null, null);
            }else if (transportType.equals("mms")){
                app.getContentResolver().delete(Uri.parse("content://mms/" + messageId), null, null);
            }
        }
        initialQuery.close();

        for (Attachment a : app.getMessageDatabase().getAttachmentsBySmsMessage(messageId)){
            a.getFileLocation().getFile().delete();
            app.getMessageDatabase().deleteAttachmentByUuid(a.getUuid().toString());
        }
    }

    private MmsMessage assembleMmsMessage(String messageId){
        MmsMessage mmsMessage = null;
        Cursor mmsCursor = app.getContentResolver().query(Uri.parse("content://mms/"), new String[]{"date_sent", "date", "msg_box", "thread_id"}, "_id = " + messageId, null, null);

        if (mmsCursor.moveToFirst()) {
            Handle sender;
            List<Attachment> attachments = new ArrayList<>();
            boolean isFromMe = false;
            StringBuilder textBuilder = new StringBuilder("");
            SmsChat smsChat = app.getMmsManager().getSmsChat(mmsCursor.getString(mmsCursor.getColumnIndex("thread_id")));

            String address = getMmsSender(messageId);
            String dateSent = mmsCursor.getString(mmsCursor.getColumnIndex("date_sent"));
            String dateDelivered = mmsCursor.getString(mmsCursor.getColumnIndex("date"));
            boolean isErrored = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box")) == 5;
            boolean isDelivered = mmsCursor.getInt(mmsCursor.getColumnIndex("msg_box")) == 2;

            if (!(!StringUtils.isEmpty(address) && PhoneNumberUtil.getInstance().isPossibleNumber(address, Resources.getSystem().getConfiguration().locale.getCountry()))) {
                sender = null;
            } else {
                if (app.getCurrentSession().getSmsHandle().getHandleID().equals(Handle.parseHandleId(address))) {
                    sender = app.getCurrentSession().getSmsHandle();
                    isFromMe = true;
                } else {
                    Handle handle = app.getMessageDatabase().getHandleByHandleID(address);

                    if (handle == null) {
                        handle = new Handle(UUID.randomUUID(), address, Handle.HandleType.SMS, false, false);
                        app.getMessageManager().addHandle(handle, false);
                    }
                    sender = handle;
                }
            }

            HashMap<String, Attachment> existingAttachments = new HashMap<>();

            for (Attachment a : app.getMessageDatabase().getAttachmentsBySmsMessage(messageId)) {
                existingAttachments.put(a.getTransferName(), a);
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
                            textBuilder.append(partCursor.getColumnIndex("text"));
                        }
                    } else {
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
                            if (MimeType.getTypeFromString(type) == MimeType.IMAGE || type.equals("image/jpg") || type.equals("image/bmp")) {
                                fileLocationContainer = processMmsImage(partId, fileName);
                            } else if (MimeTypeMap.getSingleton().getExtensionFromMimeType(type) != null) {
                                fileLocationContainer = processAttachment(partId, fileName, "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type));
                            }
                        } catch (IOException ex) {
                            AppLogger.error("An error occurred while loading an attachment from an MMS message", ex);
                        }

                        if (fileLocationContainer != null) {
                            if (existingAttachments.containsKey(fileLocationContainer.getFile().getName())) {
                                attachments.add(existingAttachments.get(fileLocationContainer.getFile().getName()));
                            } else {
                                Attachment attachment = new Attachment(UUID.randomUUID(), "", fileLocationContainer.getFile().getName(),
                                        fileLocationContainer, type, fileLocationContainer.getFile().length());

                                attachment.setBoundSmsChat(((Chat) smsChat).getIdentifier());
                                attachment.setBoundSmsMessage(messageId);
                                attachments.add(attachment);
                                app.getMessageDatabase().addAttachment(attachment);
                            }
                        }
                    }
                } while (partCursor.moveToNext());
            }
            partCursor.close();

            if (StringUtils.isEmpty(dateSent) || Long.parseLong(dateSent) < 100)
                dateSent = null;
            if (StringUtils.isEmpty(dateDelivered) || Long.parseLong(dateDelivered) < 100)
                dateDelivered = null;

            if (sender != null) {
                mmsMessage = new MmsMessage(messageId,
                        (Chat) smsChat,
                        sender,
                        attachments,
                        textBuilder.toString(),
                        dateSent == null ? null : new Date(Long.parseLong(dateSent)),
                        dateDelivered == null ? null : new Date(Long.parseLong(dateDelivered)),
                        isErrored,
                        isDelivered,
                        isFromMe);
            }
        }

        mmsCursor.close();
        return mmsMessage;
    }

    private MmsMessage assembleSmsMessage(String messageId){
        MmsMessage mmsMessage = null;
        Cursor smsCursor = app.getContentResolver().query(Uri.parse("content://sms"), new String[] { "address", "date_sent", "date", "body", "type", "thread_id" }, "_id = " + messageId, null, null);

        if (smsCursor.moveToFirst()){
            Handle sender;
            SmsChat smsChat = app.getMmsManager().getSmsChat(smsCursor.getString(smsCursor.getColumnIndex("thread_id")));
            String address = smsCursor.getString(smsCursor.getColumnIndex("address"));
            String dateSent = smsCursor.getString(smsCursor.getColumnIndex("date_sent"));
            String dateDelivered = smsCursor.getString(smsCursor.getColumnIndex("date"));
            String text = smsCursor.getString(smsCursor.getColumnIndex("body"));
            int type = smsCursor.getInt(smsCursor.getColumnIndex("type"));
            boolean isFromMe = false;
            boolean errored = type == 5;

            if (type == 2 || type == 3 || type == 4 || type == 5 || type == 6){
                isFromMe = true;
            }

            if (!(!StringUtils.isEmpty(address) && PhoneNumberUtil.getInstance().isPossibleNumber(address, Resources.getSystem().getConfiguration().locale.getCountry()))){
                sender = null;
            }else {
                if (isFromMe) {
                    sender = app.getCurrentSession().getSmsHandle();
                } else {
                    Handle handle = app.getMessageDatabase().getHandleByHandleID(address);

                    if (handle == null) {
                        handle = new Handle(UUID.randomUUID(), address, Handle.HandleType.SMS, false, false);
                        app.getMessageManager().addHandle(handle, false);
                    }

                    sender = handle;
                }
            }

            if (StringUtils.isEmpty(dateSent) || Long.parseLong(dateSent) < 100) dateSent = null;
            if (StringUtils.isEmpty(dateDelivered) || Long.parseLong(dateDelivered) < 100) dateDelivered = null;

            if (sender != null){
                mmsMessage = new MmsMessage(messageId,
                        (Chat) smsChat,
                        sender,
                        new ArrayList<Attachment>(),
                        text,
                        dateSent == null ? null : new Date(Long.parseLong(dateSent)),
                        dateDelivered == null ? null : new Date(Long.parseLong(dateDelivered)),
                        errored,
                        type == 2 && (dateSent != null || dateDelivered != null),
                        isFromMe);
            }
        }

        smsCursor.close();
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

    private boolean isThreadMms(String threadId){
        boolean isMms;

        Cursor cursor = app.getContentResolver().query(Uri.parse("content://mms-sms/conversations/"), new String[]{ "ct_t" }, "thread_id = " + threadId, null, null);
        cursor.moveToFirst();

        String contentType = cursor.getString(cursor.getColumnIndex("ct_t"));

        isMms = contentType != null && contentType.equals("application/vnd.wap.multipart.related");
        cursor.close();

        return isMms;
    }

    private boolean isMessageMms(String messageId){
        boolean isMms;

        Cursor cursor = app.getContentResolver().query(Uri.parse("content://mms-sms/conversations/"), new String[]{ "ct_t" }, "_id = " + messageId, null, null);
        cursor.moveToFirst();

        String contentType = cursor.getString(cursor.getColumnIndex("ct_t"));

        isMms = contentType != null && contentType.equals("application/vnd.wap.multipart.related");
        cursor.close();

        return isMms;
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

    private boolean integerToBoolean(Integer integer){
        if (integer > 1 || integer < 0) throw new ArrayIndexOutOfBoundsException("Parsing a boolean from an int must be either 0 or 1. Found: " + integer);
        return integer == 1;
    }
}