package scott.wemessage.app.utils;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.ui.view.dialog.AlertDialogLayout;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.weMessage;

public class ContactUtils {

    private static AtomicBoolean isSyncingContacts = new AtomicBoolean(false);

    public static void showContactSyncDialog(final Context context, final FragmentManager fragmentManager, final Runnable macRunnable, final Runnable androidRunnable){
        ContactSyncAlertDialog contactSyncAlertDialog = new ContactSyncAlertDialog();

        contactSyncAlertDialog.setSyncRunnables(
                new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_android_message));
                        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.start_process));
                        alertDialogFragmentDouble.setArguments(bundle);

                        alertDialogFragmentDouble.setOnDismiss(androidRunnable);
                        alertDialogFragmentDouble.show(fragmentManager, "ContactSyncDialogPhone");
                    }
                },

                new Runnable() {
                    @Override
                    public void run() {
                        Bundle bundle = new Bundle();
                        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                        bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                        bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_mac_message));
                        bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.start_process));
                        alertDialogFragmentDouble.setArguments(bundle);

                        if (context.getSharedPreferences(weMessage.APP_IDENTIFIER, Context.MODE_PRIVATE).getBoolean(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW, true)) {
                            alertDialogFragmentDouble.setOnDismiss(new Runnable() {
                                @Override
                                public void run() {
                                    if (context == null || fragmentManager == null) return;

                                    context.getSharedPreferences(weMessage.APP_IDENTIFIER, Context.MODE_PRIVATE).edit().putBoolean(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW, false).apply();

                                    Bundle bundle = new Bundle();
                                    DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = new DialogDisplayer.AlertDialogFragmentDouble();

                                    bundle.putString(weMessage.BUNDLE_ALERT_TITLE, context.getString(R.string.sync_contacts_title));
                                    bundle.putString(weMessage.BUNDLE_ALERT_MESSAGE, context.getString(R.string.sync_contacts_message_permission));
                                    bundle.putString(weMessage.BUNDLE_ALERT_POSITIVE_BUTTON, context.getString(R.string.ok_button));
                                    alertDialogFragmentDouble.setArguments(bundle);

                                    alertDialogFragmentDouble.setOnDismiss(macRunnable);
                                    alertDialogFragmentDouble.show(fragmentManager,  "ContactSyncDialogComputerPermission");
                                }
                            });
                        }else {
                            alertDialogFragmentDouble.setOnDismiss(macRunnable);
                        }
                        alertDialogFragmentDouble.show(fragmentManager, "ContactSyncDialogComputer");
                    }
                });
        contactSyncAlertDialog.show(fragmentManager, "ContactSyncDialog");
    }

    public static void syncContacts(final Context context){
        if (isSyncingContacts.get()) return;
        isSyncingContacts.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, Contact> contacts = new HashMap<>();
                    ContentResolver contentResolver = context.getContentResolver();
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            Handle handle = weMessage.get().getMessageDatabase().getHandleByHandleID(phoneNumber);

                            if (handle == null) {
                                handle = new Handle(UUID.randomUUID(), phoneNumber, Handle.HandleType.SMS, false, false);
                                weMessage.get().getMessageManager().addHandle(handle, false);
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
                                    File newFile = new File(weMessage.get().getChatIconsFolder(), contact.getUuid().toString() + "Imported");

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

                            contacts.put(contactId, contact);
                        }
                        cursor.close();

                        for (Contact contact : contacts.values()) {
                            weMessage.get().getMessageManager().addContact(contact, false);
                        }
                    }
                    sendLocalBroadcast(context, weMessage.BROADCAST_CONTACT_SYNC_SUCCESS, null);
                }catch (Exception ex){
                    AppLogger.error("An error occurred while syncing phone contacts", ex);
                    sendLocalBroadcast(context, weMessage.BROADCAST_CONTACT_SYNC_SUCCESS, null);
                }finally {
                    isSyncingContacts.set(false);
                }
            }
        }).start();
    }

    private static void sendLocalBroadcast(Context context, String action, Bundle extras){
        Intent broadcastIntent = new Intent(action);

        if (extras != null) {
            broadcastIntent.putExtras(extras);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    public static class ContactSyncAlertDialog extends DialogFragment {
        private int syncMode = -1;
        private Runnable androidRunnable;
        private Runnable macRunnable;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            AlertDialogLayout alertDialogLayout = (AlertDialogLayout) getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

            alertDialogLayout.setTitle(getString(R.string.sync_contacts_title));
            alertDialogLayout.setMessage(getString(R.string.sync_contacts_message));

            builder.setView(alertDialogLayout);
            builder.setPositiveButton(getString(R.string.sync_with_mac), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    syncMode = 1;
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton(getString(R.string.sync_with_phone), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    syncMode = 2;
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.dismiss_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (syncMode != -1) {
                if (getActivity() != null && !getActivity().isDestroyed() && !getActivity().isFinishing() && isAdded()) {
                    if (syncMode == 1){
                        new Handler().postDelayed(macRunnable, 100L);
                    }else if (syncMode == 2){
                        new Handler().postDelayed(androidRunnable, 100L);
                    }
                }
            }

            super.onDismiss(dialog);
        }

        @Override
        public void show(FragmentManager manager, String tag) {
            try {
                super.show(manager, tag);
            }catch(Exception ex){
                AppLogger.log(AppLogger.Level.ERROR, null, "Attempted to show a dialog when display was exited.");
            }
        }

        public void setSyncRunnables(Runnable androidSync, Runnable macSync){
            this.androidRunnable = androidSync;
            this.macRunnable = macSync;
        }
    }
}