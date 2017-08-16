package scott.wemessage.app.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.stfalcon.chatkit.messages.MessageInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import scott.wemessage.R;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.ui.view.font.FontTextView;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.StringUtils;

public class CreateChatActivity extends AppCompatActivity implements MessageManager.Callbacks {

    //TODO: If single person, sendMessage, if multiple, create group chat

    private ArrayList<String> selectedContactUuids = new ArrayList<>();
    private ArrayList<String> selectedContactsViewIntegrity = new ArrayList<>();

    private FlexboxLayout selectedContactsView;
    private EditText searchContactEditText;
    private MessageInput newChatMessageInputView;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        if (savedInstanceState != null){
            selectedContactUuids = savedInstanceState.getStringArrayList(weMessage.BUNDLE_CREATE_CHAT_CONTACT_UUIDS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.newChatToolbar);
        Button cancelButton = (Button) toolbar.findViewById(R.id.chatCreateCancelButton);
        selectedContactsView = (FlexboxLayout) findViewById(R.id.selectedContactsView);
        searchContactEditText = (EditText) findViewById(R.id.searchContactEditText);
        newChatMessageInputView = (MessageInput) findViewById(R.id.newChatMessageInputView);
        contactsRecyclerView = (RecyclerView) findViewById(R.id.contactsRecyclerView);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatList();
            }
        });
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);

        contactAdapter = new ContactAdapter();
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactAdapter);

        searchContactEditText.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();
            private final long delay = 500;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

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
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(weMessage.BUNDLE_CREATE_CHAT_CONTACT_UUIDS, selectedContactUuids);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        goToChatList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) {
        if (contactAdapter != null){
            contactAdapter.addContact(contact);
            contactAdapter.addContactToOriginal(contact);
        }
    }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {
        if (contactAdapter != null){
            contactAdapter.updateContact(newData);
            contactAdapter.updateContactToOriginal(newData);
        }
    }

    @Override
    public void onContactListRefresh(List<Contact> contacts) {
        if (contactAdapter != null){
            contactAdapter.refreshList(contacts);
            contactAdapter.setOriginalList(new ArrayList<>(contacts));
        }
    }

    @Override
    public void onChatAdd(Chat chat) {

    }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) {

    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {

    }

    @Override
    public void onChatRename(Chat chat, String displayName) {

    }

    @Override
    public void onParticipantAdd(Chat chat, Contact contact) {

    }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) {

    }

    @Override
    public void onLeaveGroup(Chat chat) {

    }

    @Override
    public void onChatDelete(Chat chat) {

    }

    @Override
    public void onChatListRefresh(List<Chat> chats) {

    }

    @Override
    public void onMessageAdd(Message message) {

    }

    @Override
    public void onMessageUpdate(Message oldData, Message newData) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMessagesQueueFinish(List<MessageBase> messages) {

    }

    @Override
    public void onMessagesRefresh() {

    }

    @Override
    public void onActionMessageAdd(ActionMessage message) {

    }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) {

    }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {

        //TODO: Stuff goes here

    }

    public void goToChatList(){
        Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

        startActivity(returnIntent);
        finish();
    }

    private void addContactToSelectedView(Contact contact){
        if (!selectedContactsViewIntegrity.contains(contact.getUuid().toString())) {
            SelectedContactNameView selectedContactNameView = new SelectedContactNameView(this);

            selectedContactsView.addView(selectedContactNameView);
            selectedContactNameView.initializeNameView(contact);
        }
    }

    private void removeContactFromSelectedView(String uuid){
        if (selectedContactsViewIntegrity.contains(uuid)) {
            for (int i = 0; i < selectedContactsView.getChildCount(); i++) {
                View v = selectedContactsView.getChildAt(i);

                if (v instanceof SelectedContactNameView) {
                    if (((SelectedContactNameView) v).getContactUuid().equals(uuid)) {
                        selectedContactsView.removeViewAt(i);
                    }
                }
            }
        }
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

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private boolean isSelected;
        private Contact contact;
        private String contactUuid;

        private ImageView selectedContactBubble;
        private ImageView contactPictureView;
        private TextView contactDisplayNameView;
        private TextView contactHandle;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_contact_create_chat, parent, false));

            selectedContactBubble = (ImageView) itemView.findViewById(R.id.selectedContactBubble);
            contactPictureView = (ImageView) itemView.findViewById(R.id.contactPictureView);
            contactDisplayNameView = (TextView) itemView.findViewById(R.id.contactDisplayNameView);
            contactHandle = (TextView) itemView.findViewById(R.id.contactHandle);

            itemView.setOnClickListener(this);
        }

        public void bind(Contact contact){
            if (!(contact.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString()))) {
                this.contact = contact;
                contactUuid = contact.getUuid().toString();
                contactDisplayNameView.setText(contact.getUIDisplayName());
                contactHandle.setText(contact.getHandle().getHandleID());

                Glide.with(CreateChatActivity.this).load(AndroidIOUtils.getContactIconUri(contact)).into(contactPictureView);

                if (selectedContactUuids.contains(contactUuid)){
                    setSelected(true);
                }else {
                    setSelected(false);
                }
            }
        }

        public void setSelected(boolean selected){
            isSelected = selected;

            if (selected){
                if (!selectedContactUuids.contains(contactUuid)) {
                    selectedContactUuids.add(contactUuid);
                }
                selectedContactBubble.setImageDrawable(getDrawable(R.drawable.ic_checkmark_circle));
                addContactToSelectedView(contact);

                if (!selectedContactsViewIntegrity.contains(contactUuid)){
                    selectedContactsViewIntegrity.add(contactUuid);
                }
            } else {
                if (selectedContactUuids.contains(contactUuid)) {
                    selectedContactUuids.remove(contactUuid);
                }
                selectedContactBubble.setImageDrawable(getDrawable(R.drawable.circle_outline));
                removeContactFromSelectedView(contactUuid);

                if (selectedContactsViewIntegrity.contains(contactUuid)){
                    selectedContactsViewIntegrity.remove(contactUuid);
                }
            }
        }

        @Override
        public void onClick(View view) {
            setSelected(!isSelected);
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        private ArrayList<Contact> originalList = new ArrayList<>();
        private ArrayList<Contact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(CreateChatActivity.this);

            return new ContactHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact c = contacts.get(position);

            holder.bind(c);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        public void setOriginalList(ArrayList<Contact> contacts){
            this.originalList = contacts;
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
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())){
                contacts.add(c);
                notifyItemInserted(contacts.size() - 1);
            }
        }

        public void addContactToOriginal(Contact c){
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())){
                originalList.add(c);
            }
        }

        public void updateContact(Contact c){
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())) {

                new AsyncTask<Contact, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Contact... params) {
                        int i = 0;
                        for (Contact contact : contacts) {
                            if (contact.getUuid().toString().equals(params[0].getUuid().toString())) {
                                contacts.set(i, params[0]);
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
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())) {
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

        public void refreshList(List<Contact> contactsList){
            contacts.clear();

            new AsyncTask<List, Void, Void>(){
                @Override
                protected Void doInBackground(List... lists) {
                    ArrayList<Contact> unsortedList = new ArrayList<>();

                    for (Object o : lists[0]){
                        if (o instanceof Contact){
                            if (!((Contact) o).getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase()
                                    .getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())) {
                                unsortedList.add((Contact) o);
                            }
                        }
                    }

                    Collections.sort(unsortedList, new Comparator<Contact>() {
                        @Override
                        public int compare(Contact c1, Contact c2) {
                            return c1.getUIDisplayName().compareTo(c2.getUIDisplayName());
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
    }

    private class SelectedContactNameView extends FontTextView {

        private String contactUuid;
        private int marginHorizontalDp = 8;

        public SelectedContactNameView(Context context) {
            super(context);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet, int defStyle) {
            super(context, attributeSet, defStyle);
        }

        public String getContactUuid(){
            return contactUuid;
        }

        public void initializeNameView(Contact c){
            FlexboxLayout.LayoutParams layoutParams = (FlexboxLayout.LayoutParams) getLayoutParams();
            contactUuid = c.getUuid().toString();

            layoutParams.setMarginStart(DisplayUtils.convertDpToRoundedPixel(marginHorizontalDp, getContext()));
            layoutParams.setMarginEnd(DisplayUtils.convertDpToRoundedPixel(marginHorizontalDp, getContext()));

            setFont("OrkneyLight");
            setTextSize(16);
            setTextColor(getColor(R.color.colorHeader));
            setFilters(new InputFilter[] { new InputFilter.LengthFilter(32) });
            setEllipsize(TextUtils.TruncateAt.END);
            setLayoutParams(layoutParams);
            setText(c.getUIDisplayName());
        }
    }

    interface IPredicate<T> {
        boolean apply(T type);
    }
}