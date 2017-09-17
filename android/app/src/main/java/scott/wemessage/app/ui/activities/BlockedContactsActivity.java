package scott.wemessage.app.ui.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import scott.wemessage.R;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageCallbacks;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.StringUtils;

public class BlockedContactsActivity extends AppCompatActivity implements MessageCallbacks {

    private boolean isBoundToConnectionService = false;
    private String callbackUuid;

    private EditText searchContactEditText;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver blockedContactsBroadcastReceiver = new BroadcastReceiver() {
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
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts);

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

        callbackUuid = UUID.randomUUID().toString();
        weMessage.get().getMessageManager().hookCallbacks(callbackUuid, this);
        LocalBroadcastManager.getInstance(this).registerReceiver(blockedContactsBroadcastReceiver, broadcastIntentFilter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.blockedContactsToolbar);
        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.blockedContactsBackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });

        toolbar.setTitle(null);
        setSupportActionBar(toolbar);

        searchContactEditText = (EditText) findViewById(R.id.searchContactEditText);
        contactsRecyclerView = (RecyclerView) findViewById(R.id.contactsRecyclerView);

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

        ArrayList<Contact> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

        contactAdapter.refreshList(contacts);
        contactAdapter.setOriginalList(contacts);
    }

    @Override
    public void onResume() {
        if (!isServiceRunning(ConnectionService.class)){
            goToLauncher();
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(blockedContactsBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        goToSettings();
    }

    @Override
    public void onContactCreate(final Contact contact) {
        if (!contact.isBlocked()) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.addContact(contact);
                    contactAdapter.addContactToOriginal(contact);
                }
            }
        });
    }

    @Override
    public void onContactUpdate(final Contact oldData, final Contact newData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    if (!oldData.isBlocked() && !newData.isBlocked()) return;

                    if (!oldData.isBlocked() && newData.isBlocked()){
                        contactAdapter.addContact(newData);
                        contactAdapter.addContactToOriginal(newData);
                    }

                    if (oldData.isBlocked() && !newData.isBlocked()){
                        contactAdapter.removeContact(newData);
                        contactAdapter.removeContactFromOriginal(newData);
                    }

                    if (oldData.isBlocked() && newData.isBlocked()) {
                        contactAdapter.updateContact(newData);
                        contactAdapter.updateContactToOriginal(newData);
                    }
                }
            }
        });
    }

    @Override
    public void onContactListRefresh(final List<Contact> contacts) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.refreshList(contacts);
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
    public void onParticipantAdd(Chat chat, Contact contact) { }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) { }

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
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.blockedContactsLayout), message, duration * 1000);

            snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.lightRed));

            snackbar.show();
        }
    }

    private void goToLauncher(){
        if (!isFinishing() && !isDestroyed()) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            finish();
        }
    }

    private void goToSettings(){
        Intent launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);

        startActivity(launcherIntent);
        finish();
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

    private <T> Collection<T> filter(Collection<T> target, IPredicate<T> predicate) {
        Collection<T> result = new ArrayList<>();
        for (T element : target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    private boolean isContactMe(Contact c){
        return c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString());
    }

    private class ContactHolder extends RecyclerView.ViewHolder {

        private boolean isInit = false;
        private boolean isDeleteButtonShowing = false;

        private SwipeLayout swipeLayout;
        private LinearLayout unblockContactButton;
        private ImageView contactPictureView;
        private TextView contactDisplayNameView;
        private TextView contactHandle;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_blocked_contact, parent, false));
        }

        public void bind(final Contact contact){
            init();

            contactDisplayNameView.setText(contact.getUIDisplayName());
            contactHandle.setText(contact.getHandle().getHandleID());

            Glide.with(BlockedContactsActivity.this).load(IOUtils.getContactIconUri(contact, IOUtils.IconSize.NORMAL)).into(contactPictureView);

            unblockContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weMessage.get().getMessageManager().updateContact(contact.getUuid().toString(), contact.setBlocked(false), true);
                }
            });

            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.contactUnblockButton));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    if (contactAdapter.showingDeletePosition != null){
                        RecyclerView.ViewHolder viewHolder = contactsRecyclerView.findViewHolderForAdapterPosition(contactAdapter.showingDeletePosition);

                        if (viewHolder != null && viewHolder instanceof ContactHolder){
                            ((ContactHolder) viewHolder).closeUnderlyingView();
                        }
                    }
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    isDeleteButtonShowing = true;
                    contactAdapter.showingDeletePosition = getAdapterPosition();
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    isDeleteButtonShowing = false;
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });
        }

        public void closeUnderlyingView(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isDeleteButtonShowing) {
                        isDeleteButtonShowing = false;
                        swipeLayout.close();
                    }
                    contactAdapter.showingDeletePosition = null;
                }
            });
        }

        private void init(){
            if (!isInit){
                isInit = true;

                swipeLayout = (SwipeLayout) itemView;
                contactPictureView = (ImageView) itemView.findViewById(R.id.blockedContactPictureView);
                contactDisplayNameView = (TextView) itemView.findViewById(R.id.blockedContactDisplayNameView);
                contactHandle = (TextView) itemView.findViewById(R.id.blockedContactHandle);
                unblockContactButton = (LinearLayout) itemView.findViewById(R.id.contactUnblockButton);
            }
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        private Integer showingDeletePosition;

        private ArrayList<Contact> originalList = new ArrayList<>();
        private ArrayList<SearchableContact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(BlockedContactsActivity.this);

            return new ContactHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact c = contacts.get(position).getContact();

            holder.bind(c);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        public void setOriginalList(ArrayList<Contact> contacts){
            for (Contact c : contacts){
                if (c.isBlocked()){
                    originalList.add(c);
                }
            }
        }

        public void searchContacts(final String search){
            killSearch(false);

            searchTask = new AsyncTask<String, Void, Collection>(){
                @Override
                protected Collection doInBackground(final String... strings) {

                    IPredicate<Contact> contactPredicate = new IPredicate<Contact>() {
                        public boolean apply(Contact contact) {
                            return contact.getUIDisplayName().contains(strings[0]);
                        }
                    };
                    return filter(originalList, contactPredicate);
                }

                @Override
                protected void onPostExecute(Collection collection) {
                    if (!isFinishing() && !isDestroyed()){
                        refreshList(new ArrayList<Contact>(collection));
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

        public void addContact(Contact c){
            if (!isContactMe(c) && c.isBlocked()){
                contacts.add(new SearchableContact(c));
                notifyItemInserted(contacts.size() - 1);
            }
        }

        public void addContactToOriginal(Contact c){
            if (!isContactMe(c) && c.isBlocked()){
                originalList.add(c);
            }
        }

        public void updateContact(Contact c){
            if (!isContactMe(c) && c.isBlocked()) {

                new AsyncTask<Contact, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Contact... params) {
                        int i = 0;
                        for (SearchableContact contact : contacts) {
                            if (contact.getContact().getUuid().toString().equals(params[0].getUuid().toString())) {
                                contacts.set(i, new SearchableContact(params[0]));
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
                                notifyItemChanged(integer);
                            }
                        }
                    }
                }.execute(c);
            }
        }

        public void updateContactToOriginal(Contact c){
            if (!isContactMe(c) && c.isBlocked()) {
                new AsyncTask<Contact, Void, Void>() {
                    @Override
                    protected Void doInBackground(Contact... params) {
                        int i = 0;
                        for (Contact contact : originalList) {
                            if (contact.getUuid().toString().equals(params[0].getUuid().toString())) {
                                originalList.set(i, params[0]);
                                break;
                            }
                            i++;
                        }
                        return null;
                    }

                }.execute(c);
            }
        }

        public void removeContact(Contact c){
            if (!isContactMe(c)) {
                new AsyncTask<Contact, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(Contact... params) {
                        int i = 0;
                        for (SearchableContact contact : contacts) {
                            if (contact.getContact().getUuid().toString().equals(params[0].getUuid().toString())) {
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

        public void removeContactFromOriginal(Contact c){
            if (!isContactMe(c)) {
                new AsyncTask<Contact, Void, Void>() {
                    @Override
                    protected Void doInBackground(Contact... params) {
                        int i = 0;
                        for (Contact contact : originalList) {
                            if (contact.getUuid().toString().equals(params[0].getUuid().toString())) {
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

        public void refreshList(List<Contact> contactsList){
            contacts.clear();

            new AsyncTask<List, Void, Void>(){
                @Override
                protected Void doInBackground(List... lists) {
                    ArrayList<SearchableContact> unsortedList = new ArrayList<>();

                    for (Object o : lists[0]){
                        if (o instanceof Contact){
                            if (!isContactMe((Contact) o) && ((Contact) o).isBlocked()) {
                                unsortedList.add(new SearchableContact((Contact) o));
                            }
                        }
                    }

                    Collections.sort(unsortedList, new Comparator<SearchableContact>() {
                        @Override
                        public int compare(SearchableContact c1, SearchableContact c2) {
                            if (c1.getContact().getUIDisplayName().equals(c2.getContact().getUIDisplayName())){
                                return c1.getContact().getHandle().getHandleID().compareTo(c2.getContact().getHandle().getHandleID());
                            }
                            return c1.getContact().getUIDisplayName().compareTo(c2.getContact().getUIDisplayName());
                        }
                    });

                    contacts = unsortedList;

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
            RecyclerView.ViewHolder viewHolder = contactsRecyclerView.findViewHolderForAdapterPosition(contactAdapter.showingDeletePosition);

            if (viewHolder != null && viewHolder instanceof ContactHolder){
                ((ContactHolder) viewHolder).closeUnderlyingView();
            }
        }
    }

    private class SearchableContact {

        private Contact contact;

        public SearchableContact(Contact contact){
            this.contact = contact;
        }

        public Contact getContact(){
            return contact;
        }
    }

    interface IPredicate<T> {
        boolean apply(T type);
    }
}