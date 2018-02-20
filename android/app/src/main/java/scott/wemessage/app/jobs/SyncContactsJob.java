package scott.wemessage.app.jobs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class SyncContactsJob extends Job {

    public static final String TAG = "weMessageSyncContactsJob";
    private static AtomicBoolean isSyncingContacts = new AtomicBoolean(false);

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        try {
            HashMap<String, Contact> contacts = new HashMap<>();
            ContentResolver contentResolver = getContext().getContentResolver();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (StringUtils.isEmpty(phoneNumber.trim())) continue;

                    Handle handle = weMessage.get().getMessageDatabase().getHandleByHandleID(phoneNumber);

                    if (handle == null) {
                        handle = new Handle(UUID.randomUUID(), phoneNumber, Handle.HandleType.SMS, false, false);
                        weMessage.get().getMessageManager().addHandleNoCallback(handle, false);
                    }

                    if (weMessage.get().getMessageDatabase().getContactByHandle(handle) != null) continue;

                    if (contacts.containsKey(contactId)) {
                        Contact c = contacts.get(contactId);
                        c.addHandle(handle);

                        contacts.put(contactId, c);
                        continue;
                    }

                    Contact contact = new Contact().setUuid(UUID.randomUUID());

                    if (name.contains(" ")) {
                        int i = name.lastIndexOf(" ");
                        String[] names = {name.substring(0, i), name.substring(i + 1)};
                        contact.setFirstName(names[0]).setLastName(names[1]);
                    } else {
                        contact.setFirstName(name).setLastName("");
                    }

                    ArrayList<Handle> handles = new ArrayList<>();
                    handles.add(handle);

                    contact.setHandles(handles).setPrimaryHandle(handle);

                    try {
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId)));

                        if (inputStream != null) {
                            Bitmap photo = BitmapFactory.decodeStream(inputStream);
                            File newFile = new File(weMessage.get().getChatIconsFolder(), contact.getUuid().toString() + "Imported.png");

                            newFile.createNewFile();

                            FileOutputStream out = new FileOutputStream(newFile);
                            photo.compress(Bitmap.CompressFormat.PNG, 100, out);

                            out.close();
                            inputStream.close();
                            photo = null;

                            contact.setContactPictureFileLocation(new FileLocationContainer(newFile));
                        }
                    } catch (Exception ex) {
                        contact.setContactPictureFileLocation(null);
                    }

                    if (!(StringUtils.isEmpty(contact.getFirstName()) && StringUtils.isEmpty(contact.getLastName()) && contact.getContactPictureFileLocation() == null)) {
                        contacts.put(contactId, contact);
                    }
                }
                cursor.close();

                for (Contact contact : contacts.values()) {
                    weMessage.get().getMessageManager().addContactNoCallback(contact, false);
                }
            }
            weMessage.get().getMessageManager().refreshContactList();
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS));
        }catch (Exception ex){
            AppLogger.error("An error occurred while syncing phone contacts", ex);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(weMessage.BROADCAST_CONTACT_SYNC_FAILED));
        }finally {
            isSyncingContacts.set(false);
        }

        return Result.SUCCESS;
    }

    public static void syncContacts(){
        if (isSyncingContacts.get()) return;
        isSyncingContacts.set(true);

        new JobRequest.Builder(SyncContactsJob.TAG)
                .startNow()
                .build()
                .schedule();
    }
}