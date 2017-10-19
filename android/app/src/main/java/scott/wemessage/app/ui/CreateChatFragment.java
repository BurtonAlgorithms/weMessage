package scott.wemessage.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;
import com.google.android.flexbox.FlexboxLayout;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.stfalcon.chatkit.messages.MessageInput;

import java.util.ArrayList;
import java.util.Calendar;
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
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Handle;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.view.chat.CreateChatBottomSheet;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.ui.view.font.FontTextView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;

public class CreateChatFragment extends MessagingFragment implements MessageCallbacks {

    private String callbackUuid;
    private int oldEditTextColor;
    private boolean isBoundToConnectionService = false;

    private ArrayList<String> selectedContactUuids = new ArrayList<>();
    private ArrayList<String> selectedContactsViewIntegrity = new ArrayList<>();
    private ArrayList<String> selectedUnknownContacts = new ArrayList<>();

    private CreateChatBottomSheet bottomSheetLayout;
    private FlexboxLayout selectedContactsView;
    private EditText searchContactEditText;
    private MessageInput newChatMessageInputView;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver createChatBroadcastReceiver = new BroadcastReceiver() {
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

        callbackUuid = UUID.randomUUID().toString();
        IntentFilter broadcastIntentFilter = new IntentFilter();

        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_FORCED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SEND_MESSAGE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_ACTION_PERFORM_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_RESULT_PROCESS_ERROR);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(createChatBroadcastReceiver, broadcastIntentFilter);
        weMessage.get().getMessageManager().hookCallbacks(callbackUuid, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_chat, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.newChatToolbar);
        Button cancelButton = (Button) toolbar.findViewById(R.id.chatCreateCancelButton);

        bottomSheetLayout = (CreateChatBottomSheet) view.findViewById(R.id.createChatSheetLayout);
        selectedContactsView = (FlexboxLayout) view.findViewById(R.id.selectedContactsView);
        searchContactEditText = (EditText) view.findViewById(R.id.searchContactEditText);
        newChatMessageInputView = (MessageInput) view.findViewById(R.id.newChatMessageInputView);
        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contactsRecyclerView);
        oldEditTextColor = searchContactEditText.getCurrentTextColor();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatList();
            }
        });
        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        contactAdapter = new ContactAdapter();
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactsRecyclerView.setAdapter(contactAdapter);

        if (savedInstanceState != null){
            selectedContactUuids = savedInstanceState.getStringArrayList(weMessage.BUNDLE_CREATE_CHAT_CONTACT_UUIDS);

            for (String unknown : savedInstanceState.getStringArrayList(weMessage.BUNDLE_CREATE_CHAT_UNKNOWN_HANDLES)){
                addUnknownContactToSelectedView(unknown);
            }
        }

        bottomSheetLayout.addOnSheetDismissedListener(new OnSheetDismissedListener() {
            @Override
            public void onDismissed(BottomSheetLayout bottomSheetLayout) {
                ((CreateChatBottomSheet) bottomSheetLayout).getSelectedNameView().toggleSelect();
            }
        });

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
                        getActivity().runOnUiThread(new Runnable() {
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

        searchContactEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    resetEditText(searchContactEditText);
                }
            }
        });

        searchContactEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    String text = searchContactEditText.getText().toString().trim();

                    if (StringUtils.isEmpty(text)){
                        clearEditText(searchContactEditText, true);
                        return true;
                    }

                    Contact attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

                    if (attemptedSearchContact != null){
                        addContactToSelectedView(attemptedSearchContact);
                        clearEditText(searchContactEditText, true);
                        return true;
                    }

                    if (AuthenticationUtils.isValidEmailFormat(text)){
                        addUnknownContactToSelectedView(text);
                        clearEditText(searchContactEditText, true);
                        return true;
                    }else {
                        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                        if (phoneNumberUtil.isPossibleNumber(text, Resources.getSystem().getConfiguration().locale.getCountry())){
                            addUnknownContactToSelectedView(text);
                            clearEditText(searchContactEditText, true);
                            return true;
                        } else {
                            closeKeyboard();
                            invalidateField(searchContactEditText);
                            showErroredSnackbar(getString(R.string.invalid_contact_format), 5);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        newChatMessageInputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                String text = input.toString().trim();
                ArrayList<SelectedNameView> selectedNameViews = new ArrayList<>();

                if (StringUtils.isEmpty(text)) return false;

                for (int i = 0; i < selectedContactsView.getChildCount(); i++) {
                    View v = selectedContactsView.getChildAt(i);

                    if (v instanceof SelectedNameView) {
                        selectedNameViews.add((SelectedNameView) v);
                    }
                }

                if (selectedNameViews.size() < 1){
                    closeKeyboard();
                    invalidateField(searchContactEditText);
                    showErroredSnackbar(getString(R.string.create_chat_minimum), 5);
                    return true;
                }

                boolean value = processMessage(text, selectedNameViews);

                if (value){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goToChatList();
                        }
                    }, 200L);
                }

                return value;
            }
        });

        ArrayList<Contact> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

        contactAdapter.refreshList(contacts);
        contactAdapter.setOriginalList(contacts);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(weMessage.BUNDLE_CREATE_CHAT_CONTACT_UUIDS, selectedContactUuids);
        outState.putStringArrayList(weMessage.BUNDLE_CREATE_CHAT_UNKNOWN_HANDLES, selectedUnknownContacts);

        super.onSaveInstanceState(outState);
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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(createChatBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onContactCreate(final Contact contact) {
        getActivity().runOnUiThread(new Runnable() {
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
    public void onContactUpdate(Contact oldData, final Contact newData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contactAdapter != null){
                    contactAdapter.updateContact(newData);
                    contactAdapter.updateContactToOriginal(newData);
                }
            }
        });
    }

    @Override
    public void onContactListRefresh(final List<Contact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
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
    public void onActionPerformFailure(final JSONAction jsonAction, final ReturnType returnType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showActionFailureSnackbar(jsonAction, returnType);
            }
        });
    }

    public void goToChatList(){
        Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

        startActivity(returnIntent);
        getActivity().finish();
    }

    private void bindService(){
        Intent intent = new Intent(getActivity(), ConnectionService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
        isBoundToConnectionService = true;
    }

    private void unbindService(){
        if (isBoundToConnectionService) {
            getActivity().unbindService(serviceConnection);
            isBoundToConnectionService = false;
        }
    }

    private boolean processMessage(String text, List<SelectedNameView> selectedNameViews){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

        if (selectedNameViews.size() == 1){
            if (selectedNameViews.get(0) instanceof SelectedContactNameView){
                Chat chat = messageDatabase.getChatByHandle(((SelectedContactNameView) selectedNameViews.get(0)).getContact().getHandle());
                if (chat != null){
                    sendMessage(text, chat);
                    return true;
                }else {
                    sendMessage(text, new PeerChat(
                            UUID.randomUUID(),
                            null,
                            null,
                            null,
                            true,
                            false,
                            ((SelectedContactNameView) selectedNameViews.get(0)).getContact()
                    ));
                    return true;
                }
            }else {
                SelectedUnknownNameView selectedUnknownNameView = (SelectedUnknownNameView) selectedNameViews.get(0);
                sendMessage(text, new PeerChat(
                        UUID.randomUUID(),
                        null,
                        null,
                        null,
                        true,
                        false,
                        new Contact().setHandle(new Handle().setHandleID(selectedUnknownNameView.getHandle())
                )));
                return true;
            }
        }else {
            String groupName;
            List<String> participants = new ArrayList<>();

            for (SelectedNameView nameView : selectedNameViews){
                if (nameView instanceof SelectedContactNameView){
                    participants.add(((SelectedContactNameView) nameView).getContact().getHandle().getHandleID());
                }else if (nameView instanceof SelectedUnknownNameView){
                    participants.add(((SelectedUnknownNameView) nameView).getHandle());
                }
            }

            int count = 1;

            for (GroupChat groupChat : messageDatabase.getGroupChatsWithLikeName(getString(R.string.default_group_name))){
                count++;
            }

            if (count == 1){
                groupName = getString(R.string.default_group_name);
            }else {
                groupName = getString(R.string.default_group_name) + " " + count;
            }

            serviceConnection.getConnectionService().getConnectionHandler().sendOutgoingCreateGroupAction(groupName, participants, text);
            return true;
        }
    }

    private void sendMessage(String input, Chat chat){
        Message message = new Message(
                UUID.randomUUID(),
                null,
                chat,
                weMessage.get().getMessageDatabase().getContactByHandle(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount())),
                new ArrayList<Attachment>(),
                input,
                DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime()),
                null,
                null,
                false,
                true,
                false,
                false,
                true,
                true
        );
        serviceConnection.getConnectionService().getConnectionHandler().sendOutgoingMessage(message, true);
    }

    private void addContactToSelectedView(Contact contact){
        if (!selectedContactsViewIntegrity.contains(contact.getUuid().toString())) {
            SelectedContactNameView selectedContactNameView = new SelectedContactNameView(getActivity());

            selectedContactsView.addView(selectedContactNameView);
            selectedContactNameView.initializeNameView(contact);
        }
    }

    private void addUnknownContactToSelectedView(String handle){
        if (!selectedUnknownContacts.contains(handle)) {
            selectedUnknownContacts.add(handle);
            SelectedUnknownNameView selectedUnknownNameView = new SelectedUnknownNameView(getActivity());

            selectedContactsView.addView(selectedUnknownNameView);
            selectedUnknownNameView.initializeNameView(handle);
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

    private void removeUnknownContactFromSelectedView(String handle){
        if (selectedUnknownContacts.contains(handle)) {
            selectedUnknownContacts.remove(handle);
            for (int i = 0; i < selectedContactsView.getChildCount(); i++) {
                View v = selectedContactsView.getChildAt(i);

                if (v instanceof SelectedUnknownNameView) {
                    if (((SelectedUnknownNameView) v).getHandle().equals(handle)) {
                        selectedContactsView.removeViewAt(i);
                    }
                }
            }
        }
    }

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void clearEditText(final EditText editText, boolean closeKeyboard){
        if (closeKeyboard) {
            closeKeyboard();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.setText("");
                editText.clearFocus();
            }
        }, 100);
    }

    private void invalidateField(final EditText editText){
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.colorHeader), getResources().getColor(R.color.brightRed));
        colorAnimation.setDuration(200);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                editText.setTextColor((int) animation.getAnimatedValue());
            }
        });

        Animation invalidShake = AnimationUtils.loadAnimation(getActivity(), R.anim.invalid_shake);
        invalidShake.setInterpolator(new CycleInterpolator(7F));

        colorAnimation.start();
        editText.startAnimation(invalidShake);
    }

    private void resetEditText( EditText editText){
        editText.setTextColor(oldEditTextColor);
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private void showErroredSnackbar(String message, int duration){
        if (getView() != null) {
            final Snackbar snackbar = Snackbar.make(getView(), message, duration * 1000);

            snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.brightRedText));

            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);

            snackbar.show();
        }
    }

    private void goToLauncher(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            getActivity().finish();
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
            super(inflater.inflate(R.layout.list_item_contact, parent, false));

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

                Glide.with(CreateChatFragment.this).load(IOUtils.getContactIconUri(contact, IOUtils.IconSize.NORMAL)).into(contactPictureView);

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
                selectedContactBubble.setImageDrawable(getResources().getDrawable(R.drawable.ic_checkmark_circle));
                addContactToSelectedView(contact);

                if (!selectedContactsViewIntegrity.contains(contactUuid)){
                    selectedContactsViewIntegrity.add(contactUuid);
                }
            } else {
                if (selectedContactUuids.contains(contactUuid)) {
                    selectedContactUuids.remove(contactUuid);
                }
                selectedContactBubble.setImageDrawable(getResources().getDrawable(R.drawable.circle_outline));
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
        private ArrayList<SearchableContact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

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

        public Contact getContactFromSearchKey(String search){
            int index = Collections.binarySearch(contacts, new SearchableContact(null).setSearchName(search), new Comparator<SearchableContact>() {
                @Override
                public int compare(SearchableContact c1, SearchableContact c2) {
                    return c1.getSearchName().compareTo(c2.getSearchName());
                }
            });

            try {
                return contacts.get(index).getContact();
            }catch (Exception ex){
                return null;
            }
        }

        public void setOriginalList(ArrayList<Contact> contacts){
            for (Contact c : contacts){
                if (!c.isBlocked()){
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
                    if (!getActivity().isFinishing() && !getActivity().isDestroyed()){
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
                if (c.isBlocked()) return;

                contacts.add(new SearchableContact(c));
                notifyItemInserted(contacts.size() - 1);
            }
        }

        public void addContactToOriginal(Contact c){
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())){
                if (c.isBlocked()) return;

                originalList.add(c);
            }
        }

        public void updateContact(Contact c){
            if (!c.getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())) {

                new AsyncTask<Contact, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Contact... params) {
                        int i = 0;
                        for (SearchableContact contact : contacts) {
                            if (contact.getContact().getUuid().toString().equals(params[0].getUuid().toString())) {
                                if (params[0].isBlocked()) break;

                                contacts.set(i, new SearchableContact(params[0]));
                                return i;
                            }
                            i++;
                        }
                        return -1;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        if (!getActivity().isFinishing() && !getActivity().isDestroyed()) {
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
                                if (params[0].isBlocked()) break;

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
                    ArrayList<SearchableContact> unsortedList = new ArrayList<>();

                    for (Object o : lists[0]){
                        if (o instanceof Contact){
                            if (!((Contact) o).getHandle().getUuid().toString().equals(weMessage.get().getMessageDatabase()
                                    .getHandleByAccount(weMessage.get().getCurrentAccount()).getUuid().toString())) {
                                if (!((Contact) o).isBlocked()) {
                                    unsortedList.add(new SearchableContact((Contact) o));
                                }
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
                    if (!getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        notifyDataSetChanged();
                    }
                }
            }.execute(contactsList);
        }
    }

    private class SearchableContact {

        private String searchName;
        private Contact contact;

        public SearchableContact(Contact contact){
            this.contact = contact;
        }

        public Contact getContact(){
            return contact;
        }

        public String getSearchName(){
            if (StringUtils.isEmpty(searchName)){
                return contact.getUIDisplayName();
            }
            return searchName;
        }

        public SearchableContact setSearchName(String searchName){
            this.searchName = searchName;
            return this;
        }
    }

    private class SelectedContactNameView extends SelectedNameView {

        private Contact contact;

        public SelectedContactNameView(Context context) {
            super(context);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet, int defStyle) {
            super(context, attributeSet, defStyle);
        }

        public Contact getContact(){
            return contact;
        }

        public String getContactUuid(){
            return contact.getUuid().toString();
        }

        public void initializeNameView(Contact contact){
            this.contact = contact;

            super.initializeView();
            setText(contact.getUIDisplayName());
        }
    }

    public class SelectedUnknownNameView extends SelectedNameView {

        private String handle;

        public SelectedUnknownNameView(Context context) {
            super(context);
        }

        public SelectedUnknownNameView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public SelectedUnknownNameView(Context context, AttributeSet attributeSet, int defStyle) {
            super(context, attributeSet, defStyle);
        }

        public String getHandle(){
            return handle;
        }

        public void initializeNameView(String handle){
            this.handle = handle;

            super.initializeView();
            setText(handle);
        }
    }

    public abstract class SelectedNameView extends FontTextView implements View.OnClickListener {

        private int marginHorizontalDp = 8;
        private int paddingVerticalDp = 4;
        private int paddingHorizontalDp = 6;
        boolean isSelected = false;

        public SelectedNameView(Context context) {
            super(context);
        }

        public SelectedNameView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public SelectedNameView(Context context, AttributeSet attributeSet, int defStyle) {
            super(context, attributeSet, defStyle);
        }

        @Override
        public void onClick(View view) {
            toggleSelect();
            bottomSheetLayout.setSelectedNameView(this);
            bottomSheetLayout.showWithSheetView(LayoutInflater.from(getActivity()).inflate(R.layout.sheet_create_chat_delete_contact, bottomSheetLayout, false));

            bottomSheetLayout.findViewById(R.id.createChatSheetDeleteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bottomSheetLayout.getSelectedNameView() instanceof SelectedUnknownNameView){
                        removeUnknownContactFromSelectedView(((SelectedUnknownNameView) bottomSheetLayout.getSelectedNameView()).getHandle());
                    }else if (bottomSheetLayout.getSelectedNameView() instanceof SelectedContactNameView){
                        removeContactFromSelectedView(((SelectedContactNameView) bottomSheetLayout.getSelectedNameView()).getContactUuid());
                    }
                    bottomSheetLayout.dismissSheet();
                }
            });

            bottomSheetLayout.findViewById(R.id.createChatSheetCancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetLayout.dismissSheet();
                }
            });
        }

        public void initializeView(){
            FlexboxLayout.LayoutParams layoutParams = (FlexboxLayout.LayoutParams) getLayoutParams();
            int paddingPxVer = DisplayUtils.convertDpToRoundedPixel(paddingVerticalDp, getContext());
            int paddingPxHoz = DisplayUtils.convertDpToRoundedPixel(paddingHorizontalDp, getContext());

            layoutParams.setMarginStart(DisplayUtils.convertDpToRoundedPixel(marginHorizontalDp, getContext()));
            layoutParams.setMarginEnd(DisplayUtils.convertDpToRoundedPixel(marginHorizontalDp, getContext()));

            setFont("OrkneyLight");
            setTextSize(16);
            setTextColor(getResources().getColor(R.color.colorHeader));
            setFilters(new InputFilter[] { new InputFilter.LengthFilter(32) });
            setEllipsize(TextUtils.TruncateAt.END);
            setLayoutParams(layoutParams);
            setPadding(paddingPxHoz, paddingPxVer, paddingPxHoz, paddingPxVer);

            setOnClickListener(this);
        }

        public void toggleSelect(){
            if (!isSelected) {
                isSelected = true;
                setTextColor(Color.WHITE);
                setBackgroundColor(getResources().getColor(R.color.colorHeader));

            } else {
                isSelected = false;
                setBackgroundColor(Color.TRANSPARENT);
                setTextColor(getResources().getColor(R.color.colorHeader));
            }
        }
    }

    interface IPredicate<T> {
        boolean apply(T type);
    }
}