package scott.wemessage.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
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
import scott.wemessage.app.connection.IConnectionBinder;
import scott.wemessage.app.messages.MessageCallbacks;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.messages.ActionMessage;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.messages.MessageBase;
import scott.wemessage.app.models.sms.chats.SmsChat;
import scott.wemessage.app.models.sms.chats.SmsGroupChat;
import scott.wemessage.app.models.sms.chats.SmsPeerChat;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.ContactInfo;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.view.chat.CreateChatBottomSheet;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.ui.view.font.FontTextView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.ListUtils;
import scott.wemessage.commons.utils.StringUtils;

public class CreateChatFragment extends MessagingFragment implements MessageCallbacks, IConnectionBinder {

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
                        LaunchActivity.launchActivity(getActivity(), CreateChatFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), CreateChatFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), CreateChatFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), CreateChatFragment.this, false);
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
                DialogDisplayer.showContactSyncResult(false, getActivity(), getFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS)){
                DialogDisplayer.showContactSyncResult(true, getActivity(), getFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION)){
                DialogDisplayer.showNoAccountsFoundDialog(getActivity(), getFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_COMPOSE_SMS_LAUNCH)){
                if (getActivity() == null) return;

                if (!(getActivity().getIntent().getAction() != null &&
                        (getActivity().getIntent().getAction().equals(Intent.ACTION_SEND) || getActivity().getIntent().getAction().equals(Intent.ACTION_SENDTO)))){
                    getActivity().finish();
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isConnectionServiceRunning()){
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
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_FAILED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_COMPOSE_SMS_LAUNCH);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(createChatBroadcastReceiver, broadcastIntentFilter);
        weMessage.get().getMessageManager().hookCallbacks(callbackUuid, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_chat, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.newChatToolbar);
        Button cancelButton = toolbar.findViewById(R.id.chatCreateCancelButton);

        bottomSheetLayout = view.findViewById(R.id.createChatSheetLayout);
        selectedContactsView = view.findViewById(R.id.selectedContactsView);
        searchContactEditText = view.findViewById(R.id.searchContactEditText);
        newChatMessageInputView = view.findViewById(R.id.newChatMessageInputView);
        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView);
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

        insertFromIntent();

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
                        if (getActivity() == null) return;
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

                    ContactInfo attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

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
                if (!StringUtils.isEmpty(searchContactEditText.getText().toString().trim())) {
                    String text = searchContactEditText.getText().toString().trim();

                    ContactInfo attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

                    if (attemptedSearchContact != null){
                        addContactToSelectedView(attemptedSearchContact);
                        clearEditText(searchContactEditText, true);
                    }else {
                        if (text.contains(" ")) {
                            boolean finish = true;

                            String[] textParticipants = text.split(" ");

                            for (String s : textParticipants){
                                if (!AuthenticationUtils.isValidEmailFormat(s)
                                        && !PhoneNumberUtil.getInstance().isPossibleNumber(text, Resources.getSystem().getConfiguration().locale.getCountry())){
                                    finish = false;
                                    break;
                                }
                            }

                            if (finish){
                                for (String s : textParticipants) {
                                    addUnknownContactToSelectedView(s);
                                }
                                clearEditText(searchContactEditText, true);
                            }else {
                                closeKeyboard();
                                invalidateField(searchContactEditText);
                                showErroredSnackbar(getString(R.string.invalid_contact_format), 5);
                                return false;
                            }
                        } else {
                            if (AuthenticationUtils.isValidEmailFormat(text)) {
                                addUnknownContactToSelectedView(text);
                                clearEditText(searchContactEditText, true);
                            } else {
                                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                                if (phoneNumberUtil.isPossibleNumber(text, Resources.getSystem().getConfiguration().locale.getCountry())) {
                                    addUnknownContactToSelectedView(text);
                                    clearEditText(searchContactEditText, true);
                                } else {
                                    closeKeyboard();
                                    invalidateField(searchContactEditText);
                                    showErroredSnackbar(getString(R.string.invalid_contact_format), 5);
                                    return false;
                                }
                            }
                        }
                    }
                }

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

        ArrayList<ContactInfo> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

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
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(createChatBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onContactCreate(final ContactInfo contact) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
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
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
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
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
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
    public void onParticipantAdd(Chat chat, Handle contact) { }

    @Override
    public void onParticipantRemove(Chat chat, Handle contact) { }

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
    public void onActionMessageAdd(ActionMessage message) { }

    @Override
    public void onMessageSendFailure(final ReturnType returnType) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessageSendFailureSnackbar(returnType);
            }
        });
    }

    @Override
    public void onActionPerformFailure(final JSONAction jsonAction, final ReturnType returnType) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showActionFailureSnackbar(jsonAction, returnType);
            }
        });
    }

    @Override
    public void onAttachmentSendFailure(final FailReason failReason) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAttachmentSendFailureSnackbar(failReason);
            }
        });
    }

    @Override
    public void onAttachmentReceiveFailure(final FailReason failReason) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAttachmentReceiveFailureSnackbar(failReason);
            }
        });
    }

    @Override
    public void bindService(){
        Intent intent = new Intent(getActivity(), ConnectionService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
        isBoundToConnectionService = true;
    }

    @Override
    public void unbindService(){
        if (isBoundToConnectionService) {
            getActivity().unbindService(serviceConnection);
            isBoundToConnectionService = false;
        }
    }

    public void goToChatList(){
        Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

        startActivity(returnIntent);
        getActivity().finish();
    }

    private boolean processMessage(String text, List<SelectedNameView> selectedNameViews){
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

        if (selectedNameViews.size() == 1){
            if (selectedNameViews.get(0) instanceof SelectedContactNameView){
                Handle handle = ((SelectedContactNameView) selectedNameViews.get(0)).getContact().pullHandle(false);

                return sendMessage(text, handle);
            }else {
                SelectedUnknownNameView selectedUnknownNameView = (SelectedUnknownNameView) selectedNameViews.get(0);

                return sendMessage(text, new Handle().setHandleID(selectedUnknownNameView.getHandle()).setHandleType(Handle.HandleType.UNKNOWN));
            }
        }else {
            List<String> participants = new ArrayList<>();

            for (SelectedNameView nameView : selectedNameViews){
                if (nameView instanceof SelectedContactNameView){
                    participants.add(((SelectedContactNameView) nameView).getContact().pullHandle(false).getHandleID());
                }else if (nameView instanceof SelectedUnknownNameView){
                    participants.add(((SelectedUnknownNameView) nameView).getHandle());
                }
            }

            if (isConnectionServiceRunning() && !isStillConnecting()){
                String groupName;
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
            }else {
                if (MmsManager.isDefaultSmsApp()){
                    boolean isSms = true;

                    for (String s : participants){
                        if (!isPossibleNumber(s)){
                            isSms = false;
                            break;
                        }
                    }

                    if (isSms){
                        List<Handle> handles = new ArrayList<>();

                        for (String s : participants){
                            handles.add(new Handle().setHandleID(s).setHandleType(Handle.HandleType.SMS));
                        }

                        SmsGroupChat groupChat = new SmsGroupChat(null, handles, null, false, false);
                        MmsMessage mmsMessage = new MmsMessage(null, groupChat,
                                weMessage.get().getCurrentSession().getSmsHandle(), new ArrayList<Attachment>(), text,
                                Calendar.getInstance().getTime(), null, false, false, true, false,true
                        );

                        weMessage.get().getMmsManager().sendMessage(mmsMessage);
                        return true;
                    }else {
                        showOfflineActionDialog(getString(R.string.offline_mode_action_create_chat, MmsManager.isDefaultSmsApp() ? getString(R.string.sms_mode) : getString(R.string.offline_mode)));
                        return false;
                    }
                }else {
                    if (isStillConnecting()){
                        showErroredSnackbar(getString(R.string.still_connecting_perform_action), 5);
                        return false;
                    }else {
                        showOfflineActionDialog(getString(R.string.offline_mode_action_create_chat, MmsManager.isDefaultSmsApp() ? getString(R.string.sms_mode) : getString(R.string.offline_mode)));
                        return false;
                    }
                }
            }
        }
    }

    private boolean sendMessage(String input, Handle handle){
        Chat chat = weMessage.get().getMessageDatabase().getChatByHandle(handle);

        if (chat == null){
            if (handle.getHandleType() == Handle.HandleType.UNKNOWN || handle.getHandleType() == Handle.HandleType.IMESSAGE){
                if (isConnectionServiceRunning() && !isStillConnecting()){
                    performSend(input, handle, false);
                    return true;
                }else if (MmsManager.isDefaultSmsApp()){
                    if (isPossibleNumber(handle.getHandleID())){
                        performSend(input, handle, true);
                        return true;
                    }else {
                        showOfflineActionDialog(getString(R.string.offline_mode_message_send, MmsManager.isDefaultSmsApp() ? getString(R.string.sms_mode) : getString(R.string.offline_mode)));
                        return false;
                    }
                }else {
                    if (isStillConnecting()){
                        showErroredSnackbar(getString(R.string.still_connecting_send_message), 5);
                        return false;
                    } else {
                        showOfflineActionDialog(getString(R.string.offline_mode_message_send, MmsManager.isDefaultSmsApp() ? getString(R.string.sms_mode) : getString(R.string.offline_mode)));
                        return false;
                    }
                }
            }else if (handle.getHandleType() == Handle.HandleType.SMS){
                if (MmsManager.isDefaultSmsApp()){
                    performSend(input, handle, true);
                    return true;
                }else {
                    if (isConnectionServiceRunning() && !isStillConnecting()){
                        performSend(input, handle, false);
                        return true;
                    }else {
                        DialogDisplayer.generateAlertDialog(getString(R.string.sms_error), getString(R.string.send_message_sms_not_default)).show(getFragmentManager(), "SendSmsErrorAlert");
                        return false;
                    }
                }
            }else return false;
        }else {
            if (chat instanceof SmsChat){
                MmsMessage mmsMessage = new MmsMessage(null, chat,
                        weMessage.get().getCurrentSession().getSmsHandle(), new ArrayList<Attachment>(), input,
                        Calendar.getInstance().getTime(), null, false, false, true, false, false
                );
                weMessage.get().getMmsManager().sendMessage(mmsMessage);
                return true;
            }else {
                Message message = new Message(
                        UUID.randomUUID().toString(),
                        null,
                        chat,
                        weMessage.get().getCurrentSession().getAccount().getHandle(),
                        new ArrayList<Attachment>(),
                        input,
                        DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime()),
                        null, null, false, true, false, false, true, true, false, MessageEffect.NONE, false
                );
                serviceConnection.getConnectionService().getConnectionHandler().sendOutgoingMessage(message, true);
                return true;
            }
        }
    }

    private void performSend(String input, Handle handle, boolean isMms){
        if (isMms){
            MmsMessage mmsMessage = new MmsMessage(null, new SmsPeerChat(null, handle, false),
                    weMessage.get().getCurrentSession().getSmsHandle(), new ArrayList<Attachment>(), input,
                    Calendar.getInstance().getTime(), null, false, false, true, false,false
            );
            weMessage.get().getMmsManager().sendMessage(mmsMessage);
        }else {
            Message message = new Message(
                    UUID.randomUUID().toString(),
                    null,
                    new PeerChat(
                            UUID.randomUUID().toString(),
                            null,
                            null,
                            null,
                            true,
                            false,
                            handle
                    ),
                    weMessage.get().getCurrentSession().getAccount().getHandle(),
                    new ArrayList<Attachment>(),
                    input,
                    DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime()),
                    null, null, false, true, false, false, true, true, false, MessageEffect.NONE, false
            );
            serviceConnection.getConnectionService().getConnectionHandler().sendOutgoingMessage(message, true);
        }
    }

    private void insertFromIntent(){
        String action = getActivity().getIntent().getAction();

        if (action != null && (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SENDTO))){
            Bundle extras = getActivity().getIntent().getExtras();
            Uri intentUri = getActivity().getIntent().getData();

            String message = extras.getString(Intent.EXTRA_TEXT);
            String recipients = getRecipients(intentUri);

            if (!StringUtils.isEmpty(recipients)){
                String[] addresses = TextUtils.split(recipients, ";");

                for (String s : addresses){
                    Handle handle = weMessage.get().getMessageDatabase().getHandleByHandleID(s);

                    if (handle != null){
                        addContactToSelectedView(handle.findRoot());
                    }else {
                        addUnknownContactToSelectedView(s);
                    }
                }
            }

            if (!StringUtils.isEmpty(message)){
                newChatMessageInputView.getInputEditText().setText(message);
            }
        }
    }

    private void addContactToSelectedView(ContactInfo contact){
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

    private void showOfflineActionDialog(String message){
        DialogDisplayer.AlertDialogFragmentDouble alertDialogFragment = DialogDisplayer.generateOfflineDialog(getActivity(), message);

        alertDialogFragment.setOnDismiss(new Runnable() {
            @Override
            public void run() {
                LaunchActivity.launchActivity(getActivity(), CreateChatFragment.this, true);
            }
        });

        alertDialogFragment.show(getFragmentManager(), "OfflineModeAlertDialog");
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
            TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(5);

            snackbar.show();
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

    private String getRecipients(Uri uri) {
        try {
            String base = uri.getSchemeSpecificPart();
            int pos = base.indexOf('?');
            return (pos == -1) ? base : base.substring(0, pos);
        }catch (Exception ex){
            return null;
        }
    }

    private boolean isStillConnecting(){
        return serviceConnection.getConnectionService() == null || !serviceConnection.getConnectionService().getConnectionHandler().isConnected().get();
    }

    private boolean isContactMe(ContactInfo contactInfo){
        if (weMessage.get().getCurrentSession().isMe(contactInfo)) return true;

        if (contactInfo instanceof Handle){
            return ((Handle) contactInfo).getHandleType() == Handle.HandleType.ME;
        }else if (contactInfo instanceof Contact){
            for (Handle h : ((Contact) contactInfo).getHandles()){
                if (h.getHandleType() == Handle.HandleType.ME) return true;
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

    private boolean isPossibleNumber(String number){
        return PhoneNumberUtil.getInstance().isPossibleNumber(number, Resources.getSystem().getConfiguration().locale.getCountry());
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private boolean isSelected;
        private ContactInfo contact;
        private String contactUuid;

        private ImageView selectedContactBubble;
        private ImageView contactPictureView;
        private TextView contactDisplayNameView;
        private TextView contactHandle;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_select_contact, parent, false));

            selectedContactBubble = itemView.findViewById(R.id.selectedContactBubble);
            contactPictureView = itemView.findViewById(R.id.contactPictureView);
            contactDisplayNameView = itemView.findViewById(R.id.contactDisplayNameView);
            contactHandle = itemView.findViewById(R.id.contactHandle);

            itemView.setOnClickListener(this);
        }

        public void bind(ContactInfo contact){
            if (!isContactMe(contact)) {
                this.contact = contact;
                contactUuid = contact.getUuid().toString();
                contactDisplayNameView.setText(contact.getDisplayName());
                contactHandle.setText(contact.pullHandle(false).getHandleID());

                Glide.with(CreateChatFragment.this).load(IOUtils.getContactIconUri(contact.pullHandle(false), IOUtils.IconSize.NORMAL)).into(contactPictureView);

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

        private ArrayList<ContactInfo> originalList = new ArrayList<>();
        private ArrayList<SearchableContact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

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

        public ContactInfo getContactFromSearchKey(String search){
            int index = Collections.binarySearch(contacts, new SearchableContact(null).setSearchName(search), new Comparator<SearchableContact>() {
                @Override
                public int compare(SearchableContact c1, SearchableContact c2) {
                    return c1.getSearchName().compareToIgnoreCase(c2.getSearchName());
                }
            });

            try {
                return contacts.get(index).getContact();
            }catch (Exception ex){
                return null;
            }
        }

        public void setOriginalList(ArrayList<ContactInfo> contacts){
            for (ContactInfo c : contacts){
                if (!isContactBlocked(c)){
                    originalList.add(c);
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
                    if (!getActivity().isFinishing() && !getActivity().isDestroyed()){
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isContactMe(c) && !isContactBlocked(c)){
                        if (c instanceof Contact && contacts.contains(new SearchableContact(c))) contacts.removeAll(Collections.singleton(new SearchableContact(c)));

                        contacts.add(new SearchableContact(c));
                        notifyItemInserted(contacts.size() - 1);
                    }
                }
            });
        }

        public void addContactToOriginal(ContactInfo c){
            if (!isContactMe(c) && !isContactBlocked(c)){
                if (c instanceof Contact && originalList.contains(c)) originalList.removeAll(Collections.singleton(c));

                originalList.add(c);
            }
        }

        public void updateContact(ContactInfo oldData, ContactInfo newData){
            if (!isContactMe(newData)) {
                new AsyncTask<ContactInfo, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (SearchableContact searchableContact : new ArrayList<>(contacts)) {
                            ContactInfo contactInfo = searchableContact.getContact();

                            if (contactInfo.equals(params[0])) {
                                if (isContactBlocked(params[0])) break;

                                if (contactInfo instanceof Handle) {
                                    contacts.set(i, new SearchableContact(params[0]));
                                    return i;
                                }else if (contactInfo instanceof Contact){
                                    Contact contact = (Contact) contactInfo;
                                    List<Handle> oldHandles;

                                    if (params[1] instanceof Contact){
                                        oldHandles = ((Contact) params[1]).getHandles();
                                    }else {
                                        oldHandles = new ArrayList<>();
                                        oldHandles.add((Handle) params[1]);
                                    }

                                    List<ListUtils.ObjectContainer> handleDiffs = ListUtils.findDifference(oldHandles, contact.getHandles());

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
                        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
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
                new AsyncTask<ContactInfo, Void, Void>() {
                    @Override
                    protected Void doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (ContactInfo contactInfo : new ArrayList<>(originalList)) {
                            if (contactInfo.equals(params[0])) {
                                if (contactInfo instanceof Handle) {
                                    originalList.set(i, params[0]);
                                    break;
                                }else if (contactInfo instanceof Contact){
                                    Contact contact = (Contact) contactInfo;
                                    List<Handle> oldHandles;

                                    if (params[1] instanceof Contact){
                                        oldHandles = ((Contact) params[1]).getHandles();
                                    }else {
                                        oldHandles = new ArrayList<>();
                                        oldHandles.add((Handle) params[1]);
                                    }

                                    List<ListUtils.ObjectContainer> handleDiffs = ListUtils.findDifference(oldHandles, contact.getHandles());

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
                        for (SearchableContact contact : new ArrayList<>(contacts)) {
                            ContactInfo contactInfo = contact.getContact();

                            if (contactInfo.equals(params[0])) {
                                contacts.remove(i);
                                return i;
                            }
                            i++;
                        }
                        return -1;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
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
                        for (ContactInfo contact : new ArrayList<>(originalList)) {
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
                            if (!isContactMe((ContactInfo) o) && !isContactBlocked((ContactInfo) o)) {
                                unsortedList.add(new SearchableContact((ContactInfo) o));
                            }
                        }
                    }

                    Collections.sort(unsortedList, new Comparator<SearchableContact>() {
                        @Override
                        public int compare(SearchableContact c1, SearchableContact c2) {
                            return c1.getContact().getDisplayName().compareTo(c2.getContact().getDisplayName());
                        }
                    });

                    contacts = unsortedList;
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                        notifyDataSetChanged();
                    }
                }
            }.execute(contactsList);
        }
    }

    private class SearchableContact {

        private String searchName;
        private ContactInfo contact;

        public SearchableContact(ContactInfo contact){
            this.contact = contact;
        }

        public ContactInfo getContact(){
            return contact;
        }

        public String getSearchName(){
            if (StringUtils.isEmpty(searchName)){
                return contact.getDisplayName();
            }
            return searchName;
        }

        public SearchableContact setSearchName(String searchName){
            this.searchName = searchName;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SearchableContact){
                if (((SearchableContact) obj).getContact().equals(contact)) return true;
            }

            return false;
        }
    }

    private class SelectedContactNameView extends SelectedNameView {

        private ContactInfo contact;

        public SelectedContactNameView(Context context) {
            super(context);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public SelectedContactNameView(Context context, AttributeSet attributeSet, int defStyle) {
            super(context, attributeSet, defStyle);
        }

        public ContactInfo getContact(){
            return contact;
        }

        public String getContactUuid(){
            return contact.getUuid().toString();
        }

        public void initializeNameView(ContactInfo contact){
            this.contact = contact;

            super.initializeView();
            setText(contact.getDisplayName());
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