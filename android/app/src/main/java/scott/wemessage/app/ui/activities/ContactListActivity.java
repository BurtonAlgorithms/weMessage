package scott.wemessage.app.ui.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageCallbacks;
import scott.wemessage.app.messages.models.ActionMessage;
import scott.wemessage.app.messages.models.Message;
import scott.wemessage.app.messages.models.MessageBase;
import scott.wemessage.app.messages.models.chats.Chat;
import scott.wemessage.app.messages.models.users.Contact;
import scott.wemessage.app.messages.models.users.ContactInfo;
import scott.wemessage.app.messages.models.users.Handle;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.ContactUtils;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.ListUtils;
import scott.wemessage.commons.utils.StringUtils;

public class ContactListActivity extends AppCompatActivity implements MessageCallbacks, OnMenuItemClickListener {

    private AtomicBoolean isInBlockedMode = new AtomicBoolean(false);
    private String callbackUuid;

    private Toolbar toolbar;
    private EditText searchContactEditText;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private ContextMenuDialogFragment menuDialogFragment;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private boolean isBoundToConnectionService = false;

    private BroadcastReceiver contactsListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED)){
                unbindService();
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                showErroredSnackbar(getString(R.string.send_message_error), 5);
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (intent.getExtras() != null){
                    showErroredSnackbar(intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE), 5);
                }else {
                    showErroredSnackbar(getString(R.string.action_perform_error_default), 5);
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                showErroredSnackbar(getString(R.string.result_process_error), 5);
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_FAILED)){
                DialogDisplayer.showContactSyncResult(false, ContactListActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS)){
                DialogDisplayer.showContactSyncResult(true, ContactListActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION)){
                DialogDisplayer.showNoAccountsFoundDialog(ContactListActivity.this, getSupportFragmentManager());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

        IntentFilter broadcastIntentFilter = new IntentFilter();

        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_FORCED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SEND_MESSAGE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_ACTION_PERFORM_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_RESULT_PROCESS_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_FAILED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION);

        callbackUuid = UUID.randomUUID().toString();
        weMessage.get().getMessageManager().hookCallbacks(callbackUuid, this);
        LocalBroadcastManager.getInstance(this).registerReceiver(contactsListBroadcastReceiver, broadcastIntentFilter);

        toolbar = findViewById(R.id.contactsListToolbar);
        toolbar.findViewById(R.id.contactsListBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });

        toolbar.findViewById(R.id.contactsListMenuButton).setOnClickListener(new OnClickWaitListener(500L) {
            @Override
            public void onWaitClick(View v) {
                menuDialogFragment.show(getSupportFragmentManager(), "ContactListContextMenuDialogFragment");
            }
        });

        toolbar.setTitle(null);
        setSupportActionBar(toolbar);

        searchContactEditText = findViewById(R.id.searchContactEditText);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        contactAdapter = new ContactAdapter();
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactAdapter);

        searchContactEditText.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long delay = 500;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(final Editable editable) {
                timer.cancel();
                timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String searchVal = editable.toString().trim();

                                if (StringUtils.isEmpty(searchVal)) {
                                    contactAdapter.killSearch(true);
                                }else {
                                    contactAdapter.searchContacts(searchVal);
                                }
                            }
                        });
                    }
                }, delay);
            }
        });

        ViewTreeObserver viewTreeObserver = toolbar.getViewTreeObserver();

        if (viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    buildContextMenu(toolbar.getHeight());
                }
            });
        }

        ArrayList<ContactInfo> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

        contactAdapter.refreshList(contacts);
        contactAdapter.setOriginalList(contacts);
    }

    @Override
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactsListBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case weMessage.REQUEST_PERMISSION_READ_CONTACTS:
                if (isGranted(grantResults)){
                    phoneContactSync();
                } else {
                    DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.sync_contacts_android_permission_denied)).show(getSupportFragmentManager(), "ContactSyncPhonePermissionDenied");
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        goToSettings();
    }

    @Override
    public void onContactCreate(final ContactInfo contact) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.addContact(contact.findRoot());
                    contactAdapter.addContactToOriginal(contact.findRoot());
                }
            }
        });

    }

    @Override
    public void onContactUpdate(final ContactInfo oldData, final ContactInfo newData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.updateContact(oldData, newData.findRoot());
                    contactAdapter.updateContactToOriginal(oldData, newData.findRoot());
                }
            }
        });
    }

    @Override
    public void onContactListRefresh(final List<? extends ContactInfo> contacts) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.refreshList(new ArrayList<>(contacts));
                    contactAdapter.setOriginalList(new ArrayList<>(contacts));
                }
            }
        });
    }

    @Override
    public void onChatAdd(Chat chat) { }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) { }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) { }

    @Override
    public void onChatRename(Chat chat, String displayName) { }

    @Override
    public void onParticipantAdd(Chat chat, Handle handle) { }

    @Override
    public void onParticipantRemove(Chat chat, Handle handle) { }

    @Override
    public void onLeaveGroup(Chat chat) { }

    @Override
    public void onChatDelete(Chat chat) { }

    @Override
    public void onChatListRefresh(List<Chat> chats) { }

    @Override
    public void onMessageAdd(Message message) { }

    @Override
    public void onMessageUpdate(Message oldData, Message newData) { }

    @Override
    public void onMessageDelete(Message message) { }

    @Override
    public void onMessagesQueueFinish(List<MessageBase> messages) { }

    @Override
    public void onMessagesRefresh() { }

    @Override
    public void onActionMessageAdd(ActionMessage message) { }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) { }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) { }

    @Override
    public void onAttachmentSendFailure(FailReason failReason) { }

    @Override
    public void onAttachmentReceiveFailure(FailReason failReason) { }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        switch (position){
            case 0: break;
            case 1:
                ContactUtils.showContactSyncDialog(this, getSupportFragmentManager(),

                new Runnable() {
                    @Override
                    public void run() {
                        macContactSync();
                    }
                },

                new Runnable() {
                    @Override
                    public void run() {
                        phoneContactSync();
                    }
                });
                break;
            case 2:
                toggleBlocked();
                break;
            default: break;
        }
    }

    private void toggleBlocked(){
        if (isInBlockedMode.get()){
            isInBlockedMode.set(false);
            ((TextView) toolbar.findViewById(R.id.contactsListToolbarTextView)).setText(R.string.word_contacts);
        }else {
            isInBlockedMode.set(true);
            ((TextView) toolbar.findViewById(R.id.contactsListToolbarTextView)).setText(R.string.blocked_contacts);
        }

        buildContextMenu(toolbar.getHeight());
        ArrayList<ContactInfo> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

        contactAdapter.refreshList(contacts);
        contactAdapter.setOriginalList(contacts);
    }

    private void buildContextMenu(int height){
        ArrayList<MenuObject> menuObjects = new ArrayList<>();
        MenuParams menuParams = new MenuParams();

        Drawable closeDrawable = getDrawable(R.drawable.ic_close);
        Drawable syncDrawable = getDrawable(R.drawable.ic_sync);
        Drawable blockedDrawable = getDrawable(R.drawable.ic_blocked);

        closeDrawable.setTint(getResources().getColor(R.color.colorHeader));
        syncDrawable.setTint(getResources().getColor(R.color.colorHeader));
        blockedDrawable.setTint(getResources().getColor(R.color.colorHeader));

        MenuObject closeMenu = new MenuObject();
        closeMenu.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        closeMenu.setBgDrawable(getResources().getDrawable(R.drawable.menu_button_drawable_top));
        closeMenu.setMenuTextAppearanceStyle(R.style.MenuFragmentStyle_TextView);
        closeMenu.setDrawable(closeDrawable);

        MenuObject contactSync = new MenuObject();
        contactSync.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        contactSync.setBgDrawable(getResources().getDrawable(R.drawable.menu_button_drawable_middle));
        contactSync.setMenuTextAppearanceStyle(R.style.MenuFragmentStyle_TextView);
        contactSync.setDrawable(syncDrawable);
        contactSync.setTitle(getString(R.string.sync_contacts));

        MenuObject toggleBlocked = new MenuObject();
        toggleBlocked.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        toggleBlocked.setBgDrawable(getResources().getDrawable(R.drawable.menu_button_drawable_bottom));
        toggleBlocked.setMenuTextAppearanceStyle(R.style.MenuFragmentStyle_TextView);
        toggleBlocked.setDrawable(blockedDrawable);
        toggleBlocked.setTitle(isInBlockedMode.get() ? getString(R.string.toggle_unblocked_contacts) : getString(R.string.toggle_blocked_contacts));

        menuObjects.add(closeMenu);
        menuObjects.add(contactSync);
        menuObjects.add(toggleBlocked);

        menuParams.setActionBarSize(DisplayUtils.convertDpToRoundedPixel(64, this));
        menuParams.setMenuObjects(menuObjects);
        menuParams.setClosableOutside(false);
        menuParams.setAnimationDelay(50);
        menuParams.setAnimationDuration(75);

        menuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        menuDialogFragment.setItemClickListener(this);
    }

    private void phoneContactSync(){
        if (!hasPermission(Manifest.permission.READ_CONTACTS, false, null, "ContactSyncReadContactsPermission", weMessage.REQUEST_PERMISSION_READ_CONTACTS)) return;

        ContactUtils.syncContacts(weMessage.get());
    }

    private void macContactSync(){
        if (!isServiceRunning(ConnectionService.class)){
            DialogDisplayer.AlertDialogFragmentDouble alertDialogFragment = DialogDisplayer.generateOfflineDialog(this, getString(R.string.contact_sync_fail_offline));

            alertDialogFragment.setOnDismiss(new Runnable() {
                @Override
                public void run() {
                    goToLauncherReconnect();
                }
            });

            alertDialogFragment.show(getSupportFragmentManager(), "OfflineModeContactSyncAlertDialog");
            return;
        }

        if (isStillConnecting()){
            showErroredSnackbar(getString(R.string.still_connecting_perform_action), 5);
            return;
        }

        serviceConnection.getConnectionService().getConnectionHandler().requestContactSync();
    }

    private void bindService(){
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
        isBoundToConnectionService = true;
    }

    private void unbindService(){
        if (isBoundToConnectionService) {
            unbindService(serviceConnection);
            isBoundToConnectionService = false;
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(this, getSupportFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private void showErroredSnackbar(String message, int duration){
        if (!isFinishing() && !isDestroyed()) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.contactSelectLayout), message, duration * 1000);

            snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.brightRedText));

            View snackbarView = snackbar.getView();
            TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);

            snackbar.show();
        }
    }

    private void launchContactView(String handleUuid){
        Intent launcherIntent = new Intent(weMessage.get(), ContactViewActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_CONTACT_VIEW_UUID, handleUuid);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, weMessage.BUNDLE_GO_TO_CONTACT_LIST);

        startActivity(launcherIntent);
        finish();
    }

    private void goToSettings(){
        Intent launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);

        startActivity(launcherIntent);
        finish();
    }

    private void goToLauncher(){
        if (!isFinishing() && !isDestroyed()) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            finish();
        }
    }

    private void goToLauncherReconnect(){
        if (!isFinishing() && !isDestroyed()) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            startActivity(launcherIntent);
            finish();
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isStillConnecting(){
        return serviceConnection.getConnectionService() == null || !serviceConnection.getConnectionService().getConnectionHandler().isConnected().get();
    }

    private <T> Collection<T> filter(Collection<T> target, IPredicate<T> predicate) {
        Collection<T> result = new ArrayList<>();
        for (T element : target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    private boolean isContactMe(ContactInfo contactInfo){
        if (contactInfo instanceof Handle){
            return contactInfo.getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString()) || ((Handle) contactInfo).getHandleType() == Handle.HandleType.ME;
        }else if (contactInfo instanceof Contact){
            for (Handle h : ((Contact) contactInfo).getHandles()){
                if (h.getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString()) || h.getHandleType() == Handle.HandleType.ME) return true;
            }
        }
        return false;
    }

    private boolean isContactBlocked(ContactInfo contactInfo){
        if (contactInfo instanceof Handle){
            return ((Handle) contactInfo).isBlocked();
        }else if (contactInfo instanceof Contact){
            for (Handle h : ((Contact) contactInfo).getHandles()){
                if (h.isBlocked()) return true;
            }
        }

        return false;
    }

    private boolean hasPermission(final String permission, boolean showRationale, String rationaleString, String alertTagId, final int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (showRationale && shouldShowRequestPermissionRationale(permission)){
                DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), rationaleString);

                alertDialogFragment.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    }
                });
                alertDialogFragment.show(getSupportFragmentManager(), alertTagId);
                return false;
            } else {
                requestPermissions(new String[] { permission }, requestCode);
                return false;
            }
        }
        return true;
    }

    private boolean isGranted(int[] grantResults){
        return (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    private class ContactHolder extends RecyclerView.ViewHolder {
        private boolean isInit = false;

        private SwipeLayout swipeLayout;
        private ImageView contactPictureView;
        private TextView contactDisplayNameView;
        private TextView contactHandle;
        private TextView toggleBlockButton;
        private TextView removeContactButton;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_contact, parent, false));
        }

        public void bind(final ContactInfo contact){
            init();

            Glide.with(ContactListActivity.this).load(IOUtils.getContactIconUri(contact.pullHandle(false), IOUtils.IconSize.NORMAL)).into(contactPictureView);
            contactDisplayNameView.setText(contact.getDisplayName());
            contactHandle.setText(contact.pullHandle(false).getHandleID());

            toggleBlockButton.setText(isInBlockedMode.get() ? R.string.word_unblock : R.string.word_block);
            toggleBlockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (contact instanceof Handle) {
                        Handle h = (Handle) contact;

                        weMessage.get().getMessageManager().updateHandle(h.getUuid().toString(), h.setBlocked(!isInBlockedMode.get()), true);
                    }else if (contact instanceof Contact){
                        for (Handle h : ((Contact) contact).getHandles()){
                            weMessage.get().getMessageManager().updateHandle(h.getUuid().toString(), h.setBlocked(!isInBlockedMode.get()), false);
                        }
                        weMessage.get().getMessageManager().updateContact(contact.getUuid().toString(), (Contact) contact, true);
                    }
                }
            });

            removeContactButton.setOnClickListener(new OnClickWaitListener(500L) {
                @Override
                public void onWaitClick(View v) {
                    if (contact instanceof Handle) {
                        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragmentDouble = DialogDisplayer.generateAlertDialogDouble(getString(R.string.delete_handle_title), getString(R.string.delete_handle_message), getString(R.string.word_delete));
                        alertDialogFragmentDouble.setOnDismiss(new Runnable() {
                            @Override
                            public void run() {
                                weMessage.get().getMessageManager().deleteHandle(contact.getUuid().toString(), true);
                            }
                        });
                        alertDialogFragmentDouble.show(getSupportFragmentManager(), "DeleteContactDialog");
                    }else if (contact instanceof Contact){
                        weMessage.get().getMessageManager().deleteContact(contact.getUuid().toString(), true);
                    }
                }
            });

            itemView.findViewById(R.id.contactItemLayout).setOnClickListener(new OnClickWaitListener(500L) {
                @Override
                public void onWaitClick(View v) {
                    if (!isInBlockedMode.get()) launchContactView(contact.pullHandle(false).getUuid().toString());
                }
            });

            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.contactSwipeButtonLayout));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    if (contactAdapter.showingDeletePosition != null && contactAdapter.showingDeletePosition != getAdapterPosition()){
                        RecyclerView.ViewHolder viewHolder = contactsRecyclerView.findViewHolderForAdapterPosition(contactAdapter.showingDeletePosition);

                        if (viewHolder != null && viewHolder instanceof ContactHolder){
                            ((ContactHolder) viewHolder).closeUnderlyingView();
                        }
                    }
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    contactAdapter.showingDeletePosition = getAdapterPosition();
                }

                @Override
                public void onStartClose(SwipeLayout layout) { }

                @Override
                public void onClose(SwipeLayout layout) {
                    if (contactAdapter.showingDeletePosition != null && contactAdapter.showingDeletePosition == getAdapterPosition()) {
                        contactAdapter.showingDeletePosition = null;
                    }
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) { }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) { }
            });
        }

        public void closeUnderlyingView(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (swipeLayout.getOpenStatus() != SwipeLayout.Status.Close) {
                        swipeLayout.close();
                    }
                }
            });
        }

        private void init(){
            if (!isInit){
                isInit = true;

                swipeLayout = (SwipeLayout) itemView;
                contactPictureView = itemView.findViewById(R.id.contactPictureView);
                contactDisplayNameView = itemView.findViewById(R.id.contactDisplayNameView);
                contactHandle = itemView.findViewById(R.id.contactHandle);
                toggleBlockButton = itemView.findViewById(R.id.contactToggleBlockButton);
                removeContactButton = itemView.findViewById(R.id.contactRemoveButton);
            }
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        private Integer showingDeletePosition;

        private ArrayList<ContactInfo> originalList = new ArrayList<>();
        private ArrayList<SearchableContact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ContactListActivity.this);

            return new ContactHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            ContactInfo c = contacts.get(position).getContact();

            holder.bind(c);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        public void setOriginalList(ArrayList<ContactInfo> contacts){
            for (ContactInfo c : contacts){
                if (isInBlockedMode.get()){
                    if (isContactBlocked(c)){
                        originalList.add(c);
                    }
                }else {
                    if (!isContactBlocked(c)) {
                        originalList.add(c);
                    }
                }
            }
        }

        public void searchContacts(final String search){
            killSearch(false);

            searchTask = new AsyncTask<String, Void, Collection>(){
                @Override
                protected Collection doInBackground(final String... strings) {

                    IPredicate<ContactInfo> contactPredicate = new IPredicate<ContactInfo>() {
                        public boolean apply(ContactInfo contact) {
                            return StringUtils.containsIgnoreCase(contact.getDisplayName(), strings[0]);
                        }
                    };
                    return filter(originalList, contactPredicate);
                }

                @Override
                protected void onPostExecute(Collection collection) {
                    if (!isFinishing() && !isDestroyed()){
                        refreshList(new ArrayList<ContactInfo>(collection));
                    }
                }
            };
            searchTask.execute(search);
        }

        public void killSearch(boolean defaultList){
            if (searchTask != null){
                if (searchTask.getStatus() == AsyncTask.Status.RUNNING) {
                    searchTask.cancel(true);
                }
                searchTask = null;
            }

            if (defaultList){
                refreshList(originalList);
            }
        }

        public void addContact(final ContactInfo c){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isInBlockedMode.get()){
                        if (!isContactBlocked(c)) return;
                        if (c instanceof Contact && contacts.contains(new SearchableContact(c))) contacts.removeAll(Collections.singleton(new SearchableContact(c)));

                        contacts.add(new SearchableContact(c));
                        notifyItemInserted(contacts.size() - 1);

                    } else {
                        if (!isContactMe(c) && !isContactBlocked(c)) {
                            if (c instanceof Contact && contacts.contains(new SearchableContact(c))) contacts.removeAll(Collections.singleton(new SearchableContact(c)));

                            contacts.add(new SearchableContact(c));
                            notifyItemInserted(contacts.size() - 1);
                        }
                    }
                }
            });
        }

        public void addContactToOriginal(ContactInfo c){
            if (isInBlockedMode.get()){
                if (!isContactBlocked(c)) return;
                if (c instanceof Contact && originalList.contains(c)) originalList.removeAll(Collections.singleton(c));

                originalList.add(c);
            } else {
                if (!isContactMe(c) && !isContactBlocked(c)) {
                    if (c instanceof Contact && originalList.contains(c)) originalList.removeAll(Collections.singleton(c));

                    originalList.add(c);
                }
            }
        }

        public void updateContact(ContactInfo oldData, ContactInfo newData){
            if (!isContactMe(newData)) {

                if (isInBlockedMode.get()){
                    if (!isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())) return;

                    if (!isContactBlocked(oldData) && isContactBlocked(newData.findRoot())){
                        addContact(newData.findRoot());
                        return;
                    }

                    if (isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())){
                        removeContact(newData.findRoot());
                        return;
                    }
                } else {
                    if (isContactBlocked(oldData) && isContactBlocked(newData.findRoot())) return;

                    if (isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())){
                        addContact(newData.findRoot());
                        return;
                    }

                    if (!isContactBlocked(oldData) && isContactBlocked(newData.findRoot())){
                        removeContact(newData.findRoot());
                        return;
                    }
                }

                new AsyncTask<ContactInfo, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (SearchableContact searchableContact : contacts) {
                            ContactInfo contactInfo = searchableContact.getContact();

                            if (contactInfo.equals(params[0])) {
                                if (contactInfo instanceof Handle) {
                                    contacts.set(i, new SearchableContact(params[0]));
                                    return i;
                                }else if (contactInfo instanceof Contact){
                                    Contact contact = (Contact) contactInfo;
                                    Contact oldContact = (Contact) params[1];
                                    List<ListUtils.ObjectContainer> handleDiffs = ListUtils.findDifference(oldContact.getHandles(), contact.getHandles());

                                    for (ListUtils.ObjectContainer handleDiff : handleDiffs){
                                        if (handleDiff.getStatus() == ListUtils.ListStatus.REMOVED){
                                            addContact((ContactInfo) handleDiff.getObject());
                                        }else if (handleDiff.getStatus() == ListUtils.ListStatus.ADDED){
                                            removeContact((ContactInfo) handleDiff.getObject());
                                        }
                                    }
                                    contacts.set(i, new SearchableContact(params[0]));
                                    return i;
                                }
                            }
                            i++;
                        }
                        return -1;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        if (!isFinishing() && !isDestroyed()) {
                            if (integer != -1) {
                                notifyItemChanged(integer);
                            }
                        }
                    }
                }.execute(newData, oldData);
            }
        }

        public void updateContactToOriginal(ContactInfo oldData, ContactInfo newData){
            if (!isContactMe(newData)) {
                if (isInBlockedMode.get()){
                    if (!isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())) return;

                    if (!isContactBlocked(oldData) && isContactBlocked(newData.findRoot())){
                        addContactToOriginal(newData.findRoot());
                        return;
                    }

                    if (isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())){
                        removeContactFromOriginal(newData.findRoot());
                        return;
                    }
                } else {
                    if (isContactBlocked(oldData) && isContactBlocked(newData.findRoot())) return;

                    if (isContactBlocked(oldData) && !isContactBlocked(newData.findRoot())){
                        addContactToOriginal(newData.findRoot());
                        return;
                    }

                    if (!isContactBlocked(oldData) && isContactBlocked(newData.findRoot())){
                        removeContactFromOriginal(newData.findRoot());
                        return;
                    }
                }

                new AsyncTask<ContactInfo, Void, Void>() {
                    @Override
                    protected Void doInBackground(ContactInfo... params) {
                        int i = 0;

                        for (ContactInfo contactInfo : originalList) {
                            if (contactInfo.equals(params[0])) {
                                if (contactInfo instanceof Handle) {
                                    originalList.set(i, params[0]);
                                    break;
                                }else if (contactInfo instanceof Contact){
                                    Contact contact = (Contact) contactInfo;
                                    Contact oldContact = (Contact) params[1];
                                    List<ListUtils.ObjectContainer> handleDiffs = ListUtils.findDifference(oldContact.getHandles(), contact.getHandles());

                                    for (ListUtils.ObjectContainer handleDiff : handleDiffs){
                                        if (handleDiff.getStatus() == ListUtils.ListStatus.REMOVED){
                                            addContactToOriginal((ContactInfo) handleDiff.getObject());
                                        }else if (handleDiff.getStatus() == ListUtils.ListStatus.ADDED){
                                            removeContactFromOriginal((ContactInfo) handleDiff.getObject());
                                        }
                                    }
                                    originalList.set(i, params[0]);
                                    break;
                                }
                            }
                            i++;
                        }
                        return null;
                    }

                }.execute(newData, oldData);
            }
        }

        public void removeContact(ContactInfo c){
            if (!isContactMe(c)) {
                new AsyncTask<ContactInfo, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (SearchableContact contact : contacts) {
                            ContactInfo contactInfo = contact.getContact();

                            if (contactInfo.equals(params[0])) {
                                closeUnderlyingView();
                                contacts.remove(i);
                                return i;
                            }
                            i++;
                        }
                        return -1;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        if (!isFinishing() && !isDestroyed()) {
                            if (integer != -1) {
                                notifyItemRemoved(integer);
                            }
                        }
                    }
                }.execute(c);
            }
        }

        public void removeContactFromOriginal(ContactInfo c){
            if (!isContactMe(c)) {
                new AsyncTask<ContactInfo, Void, Void>() {
                    @Override
                    protected Void doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (ContactInfo contact : originalList) {
                            if (contact.equals(params[0])) {
                                originalList.remove(i);
                                break;
                            }
                            i++;
                        }
                        return null;
                    }

                }.execute(c);
            }
        }

        public void refreshList(List<ContactInfo> contactsList){
            contacts.clear();

            new AsyncTask<List, Void, Void>(){
                @Override
                protected Void doInBackground(List... lists) {
                    ArrayList<SearchableContact> unsortedList = new ArrayList<>();

                    for (Object o : lists[0]){
                        if (o instanceof ContactInfo){
                            if (!isContactMe((ContactInfo) o)) {
                                if (isInBlockedMode.get()) {
                                    if (isContactBlocked((ContactInfo) o)) unsortedList.add(new SearchableContact((ContactInfo) o));
                                }else {
                                    if (!isContactBlocked((ContactInfo) o)) unsortedList.add(new SearchableContact((ContactInfo) o));
                                }
                            }
                        }
                    }

                    Collections.sort(unsortedList, new Comparator<SearchableContact>() {
                        @Override
                        public int compare(SearchableContact c1, SearchableContact c2) {
                            return c1.getContact().getDisplayName().compareTo(c2.getContact().getDisplayName());
                        }
                    });

                    contacts.addAll(unsortedList);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (!isFinishing() && !isDestroyed()) {
                        notifyDataSetChanged();
                    }
                }
            }.execute(contactsList);
        }

        private void closeUnderlyingView(){
            if (showingDeletePosition == null) return;

            RecyclerView.ViewHolder viewHolder = contactsRecyclerView.findViewHolderForAdapterPosition(showingDeletePosition);

            if (viewHolder != null && viewHolder instanceof ContactHolder){
                ((ContactHolder) viewHolder).closeUnderlyingView();
            }
        }
    }

    private class SearchableContact {

        private ContactInfo contact;

        public SearchableContact(ContactInfo contact){
            this.contact = contact;
        }

        public ContactInfo getContact(){
            return contact;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SearchableContact){
                if (((SearchableContact) obj).getContact().equals(contact)) return true;
            }

            return false;
        }
    }

    private class ContactInsert {
        Integer position;
        List<Integer> removed;

        public ContactInsert(Integer position, List<Integer> removed) {
            this.position = position;
            this.removed = removed;
        }
    }

    interface IPredicate<T> {
        boolean apply(T type);
    }
}