/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

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
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.messages.ActionMessage;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.messages.MessageBase;
import scott.wemessage.app.models.users.Account;
import scott.wemessage.app.models.users.Contact;
import scott.wemessage.app.models.users.ContactInfo;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.ui.activities.abstracts.IAccountSwitcher;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.crypto.BCrypt;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.ListUtils;
import scott.wemessage.commons.utils.StringUtils;

public class ContactSelectActivity extends BaseActivity implements MessageCallbacks, IAccountSwitcher {

    private static  final String BUNDLE_IS_SWITCH_ACCOUNTS_FRAGMENT_SHOWN = "bundleIsSwitchAccountsFragmentShown";
    private static final String BUNDLE_SWITCH_ACCOUNTS_EMAIL = "bundleSwitchAccountsEmail";

    private int oldEditTextColor;
    private boolean isBoundToConnectionService = false;
    private boolean isSwitchAccountsMode = false;
    private boolean isSwitchAccountFragmentShown = false;
    private boolean isSavingInstanceState = false;

    private String callbackUuid;
    private String chatUuid;
    private String[] handleChatUuidMap;
    private String selectedSwitchAccount;

    private EditText searchContactEditText;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private FrameLayout switchAccountsFragmentContainer;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver contactSelectBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED)){
                unbindService();
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(ContactSelectActivity.this, null, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(ContactSelectActivity.this, null, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(ContactSelectActivity.this, null, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(ContactSelectActivity.this, null, false);
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
                DialogDisplayer.showContactSyncResult(false, ContactSelectActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS)){
                DialogDisplayer.showContactSyncResult(true, ContactSelectActivity.this, getSupportFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION)){
                DialogDisplayer.showNoAccountsFoundDialog(ContactSelectActivity.this, getSupportFragmentManager());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_select);

        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

        if (savedInstanceState != null){
            chatUuid = savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT);
            handleChatUuidMap = savedInstanceState.getStringArray(weMessage.BUNDLE_HANDLE_UUID);
            isSwitchAccountsMode = savedInstanceState.getBoolean(weMessage.BUNDLE_SWITCH_ACCOUNTS_MODE);
            selectedSwitchAccount = savedInstanceState.getString(BUNDLE_SWITCH_ACCOUNTS_EMAIL);
        }else {
            chatUuid = getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
            handleChatUuidMap = getIntent().getStringArrayExtra(weMessage.BUNDLE_HANDLE_UUID);
            isSwitchAccountsMode = getIntent().getBooleanExtra(weMessage.BUNDLE_SWITCH_ACCOUNTS_MODE, false);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(contactSelectBroadcastReceiver, broadcastIntentFilter);

        searchContactEditText = findViewById(R.id.searchContactEditText);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        switchAccountsFragmentContainer = findViewById(R.id.switchAccountsFragmentContainer);
        oldEditTextColor = searchContactEditText.getCurrentTextColor();

        findViewById(R.id.contactSelectCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousView();
            }
        });

        contactAdapter = new ContactAdapter();
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactAdapter);

        ViewGroup.LayoutParams layoutParams = switchAccountsFragmentContainer.getLayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams.height = displayMetrics.heightPixels / 2;

        switchAccountsFragmentContainer.setLayoutParams(layoutParams);

        searchContactEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSwitchAccountFragmentShown){
                    toggleSwitchAccountsFragment(null, false, true);
                }
                return false;
            }
        });

        contactsRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSwitchAccountFragmentShown){
                    toggleSwitchAccountsFragment(null, false, true);
                }
                return false;
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

                    if (isSwitchAccountsMode){
                        ContactInfo attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

                        if (attemptedSearchContact != null){
                            clearEditText(searchContactEditText, true);
                            performAction(attemptedSearchContact.pullHandle(false).getHandleID());
                            return true;
                        }else {
                            closeKeyboard();
                            invalidateField(searchContactEditText);
                            showErroredSnackbar(getString(R.string.account_lookup_not_found), 5);
                            return true;
                        }
                    }

                    if (!StringUtils.isEmpty(handleChatUuidMap[0])){
                        ContactInfo attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

                        if (attemptedSearchContact != null){
                            clearEditText(searchContactEditText, true);
                            performAction(attemptedSearchContact.findRoot().getUuid().toString());
                            return true;
                        }else {
                            closeKeyboard();
                            invalidateField(searchContactEditText);
                            showErroredSnackbar(getString(R.string.contact_not_found), 5);
                            return true;
                        }
                    }

                    ContactInfo attemptedSearchContact = contactAdapter.getContactFromSearchKey(text);

                    if (attemptedSearchContact != null){
                        clearEditText(searchContactEditText, true);
                        performAction(attemptedSearchContact.pullHandle(true).getHandleID());
                        return true;
                    }

                    if (AuthenticationUtils.isValidEmailFormat(text)){
                        clearEditText(searchContactEditText, true);
                        performAction(text);
                        return true;
                    }else {
                        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                        if (phoneNumberUtil.isPossibleNumber(text, Resources.getSystem().getConfiguration().locale.getCountry())){
                            clearEditText(searchContactEditText, true);
                            performAction(text);
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

        if (savedInstanceState != null && savedInstanceState.getBoolean(BUNDLE_IS_SWITCH_ACCOUNTS_FRAGMENT_SHOWN)){
            toggleSwitchAccountsFragment(selectedSwitchAccount,true, false);
        }

        ArrayList<ContactInfo> contacts = new ArrayList<>(weMessage.get().getMessageManager().getContacts().values());

        contactAdapter.refreshList(contacts);
        contactAdapter.setOriginalList(contacts);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        isSavingInstanceState = true;

        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);
        outState.putStringArray(weMessage.BUNDLE_HANDLE_UUID, handleChatUuidMap);
        outState.putBoolean(weMessage.BUNDLE_SWITCH_ACCOUNTS_MODE, isSwitchAccountsMode);
        outState.putBoolean(BUNDLE_IS_SWITCH_ACCOUNTS_FRAGMENT_SHOWN, isSwitchAccountFragmentShown);

        if (isSwitchAccountFragmentShown){
            outState.putString(BUNDLE_SWITCH_ACCOUNTS_EMAIL, selectedSwitchAccount);
            toggleSwitchAccountsFragment(null, false, false);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactSelectBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        goToPreviousView();
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
    public void onActionMessageAdd(ActionMessage message) { }

    @Override
    public void onMessageSendFailure(ReturnType returnType) { }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) { }

    @Override
    public void onAttachmentSendFailure(FailReason failReason) { }

    @Override
    public void onAttachmentReceiveFailure(FailReason failReason) { }

    @Override
    public void onAccountSwitched() {
        toggleSwitchAccountsFragment(null, false, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToPreviousView();
            }
        }, 275L);
    }

    private void performAction(String arg){
        if (isSavingInstanceState) return;

        if (isSwitchAccountsMode){
            toggleSwitchAccountsFragment(arg, !isSwitchAccountFragmentShown, true);
        }else if (!StringUtils.isEmpty(chatUuid)){
            performAddParticipantAction(arg);
        }else if (!StringUtils.isEmpty(handleChatUuidMap[0])){
            addHandleToContact(arg);
        }
    }

    private void performAddParticipantAction(String participant){
        if (!isServiceRunning(ConnectionService.class)){
            DialogDisplayer.AlertDialogFragmentDouble alertDialogFragment = DialogDisplayer.generateOfflineDialog(this,
                    getString(R.string.offline_mode_action_add, MmsManager.isDefaultSmsApp() ? getString(R.string.sms_mode) : getString(R.string.offline_mode)));

            alertDialogFragment.setOnDismiss(new Runnable() {
                @Override
                public void run() {
                    LaunchActivity.launchActivity(ContactSelectActivity.this, null, true);
                }
            });

            alertDialogFragment.show(getSupportFragmentManager(), "OfflineModeAlertDialog");
            return;
        }

        serviceConnection.getConnectionService().getConnectionHandler().sendOutgoingAddParticipantAction(((GroupChat) weMessage.get().getMessageDatabase().getChatByIdentifier(chatUuid)), participant);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToPreviousView();
            }
        }, 100L);
    }

    private void addHandleToContact(String contactUuid){
        Contact contact = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);
        weMessage.get().getMessageManager().updateContact(contact.getUuid().toString(), contact.addHandle(weMessage.get().getMessageDatabase().getHandleByUuid(handleChatUuidMap[0])), true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToPreviousView();
            }
        }, 100L);
    }

    private void toggleSwitchAccountsFragment(final String email, boolean value, boolean performAnimation){
        if (isSwitchAccountFragmentShown != value){
            if (value){
                isSwitchAccountFragmentShown = true;

                if (performAnimation) {
                    switchAccountsFragmentContainer.animate().alpha(1.0f).translationY(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (isSavingInstanceState) return;

                            super.onAnimationStart(animation);

                            handleSwitchAccountsFragmentOpen(email);
                            switchAccountsFragmentContainer.setVisibility(View.VISIBLE);
                        }
                    });
                }else {
                    handleSwitchAccountsFragmentOpen(email);
                    switchAccountsFragmentContainer.setVisibility(View.VISIBLE);
                    switchAccountsFragmentContainer.animate().alpha(1.0f).translationY(0).setDuration(1);
                }
            }else {
                isSwitchAccountFragmentShown = false;

                int height = switchAccountsFragmentContainer.getHeight();

                if (performAnimation) {
                    switchAccountsFragmentContainer.animate().alpha(0.f).translationY(height).setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            handleSwitchAccountsFragmentClose();
                            switchAccountsFragmentContainer.setVisibility(View.GONE);
                        }
                    });
                }else {
                    handleSwitchAccountsFragmentClose();
                    switchAccountsFragmentContainer.setVisibility(View.GONE);
                    switchAccountsFragmentContainer.animate().alpha(0.f).translationY(height).setDuration(1);
                }
            }
        }
    }

    private void handleSwitchAccountsFragmentOpen(String email){
        closeKeyboard();

        SwitchAccountsFragment switchAccountsFragment = new SwitchAccountsFragment();
        Bundle popupArgs = new Bundle();

        popupArgs.putString(BUNDLE_SWITCH_ACCOUNTS_EMAIL, email);
        switchAccountsFragment.setArguments(popupArgs);
        switchAccountsFragment.setCallbacks(this);

        getSupportFragmentManager().beginTransaction().add(R.id.switchAccountsFragmentContainer, switchAccountsFragment).commit();
    }

    private void handleSwitchAccountsFragmentClose(){
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.switchAccountsFragmentContainer)).commit();
    }

    private void goToPreviousView(){
        Intent launcherIntent;

        if (isSwitchAccountsMode){
            launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);
        }else if (!StringUtils.isEmpty(chatUuid)) {
            launcherIntent = new Intent(weMessage.get(), ChatViewActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);
        }else {
            launcherIntent = new Intent(weMessage.get(), ContactViewActivity.class);
            launcherIntent.putExtra(weMessage.BUNDLE_CONTACT_VIEW_UUID, handleChatUuidMap[0]);
            launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, handleChatUuidMap[1]);
        }

        startActivity(launcherIntent);
        finish();
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

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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

        Animation invalidShake = AnimationUtils.loadAnimation(this, R.anim.invalid_shake);
        invalidShake.setInterpolator(new CycleInterpolator(7F));

        colorAnimation.start();
        editText.startAnimation(invalidShake);
    }

    private void resetEditText( EditText editText){
        editText.setTextColor(oldEditTextColor);
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

    private boolean excludeContact(ContactInfo contact){
        if (isSwitchAccountsMode){
            return !(isContactMe(contact) && AuthenticationUtils.isValidEmailFormat(contact.pullHandle(false).getHandleID()));
        }

        if (isContactMe(contact) || isContactBlocked(contact)) return true;

        if (!StringUtils.isEmpty(chatUuid)) {
            GroupChat groupChat = (GroupChat) weMessage.get().getMessageDatabase().getChatByIdentifier(chatUuid);

            for (Handle h : groupChat.getParticipants()) {
                if (contact.equals(h)) return true;
            }
        }else if (!StringUtils.isEmpty(handleChatUuidMap[0])) {
            if (contact instanceof Handle) return true;
        }

        return false;
    }

    public static class SwitchAccountsFragment extends Fragment {
        private IAccountSwitcher callbacks;

        int oldEditTextColor;
        EditText passwordEditText;
        Button switchAccountButton;
        String accountEmail;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_switch_accounts_password, container, false);

            accountEmail = getArguments().getString(BUNDLE_SWITCH_ACCOUNTS_EMAIL);
            passwordEditText = view.findViewById(R.id.switchAccountsEnterPasswordEditText);
            switchAccountButton = view.findViewById(R.id.switchAccountButton);
            oldEditTextColor = passwordEditText.getCurrentTextColor();

            passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        resetEditText(passwordEditText);
                    }
                }
            });

            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        clearEditText(passwordEditText, true);
                        switchAccount(accountEmail, passwordEditText.getText().toString());
                    }
                    return false;
                }
            });

            switchAccountButton.setOnClickListener(new OnClickWaitListener(500L) {
                @Override
                public void onWaitClick(View v) {
                    switchAccount(accountEmail, passwordEditText.getText().toString());
                }
            });

            return view;
        }

        void setCallbacks(IAccountSwitcher callbacks){
            this.callbacks = callbacks;
        }

        private void switchAccount(String email, String password){
            closeKeyboard();
            clearEditText(passwordEditText, false);

            if (StringUtils.isEmpty(password)) {
                invalidateField(passwordEditText);
                generateInvalidSnackBar(getView(), getString(R.string.no_password)).show();
                return;
            }

            AuthenticationUtils.PasswordValidateType validateType = AuthenticationUtils.isValidPasswordFormat(password);

            if (validateType == AuthenticationUtils.PasswordValidateType.LENGTH_TOO_SMALL) {
                invalidateField(passwordEditText);
                generateInvalidSnackBar(getView(), getString(R.string.password_too_short, weMessage.MINIMUM_PASSWORD_LENGTH)).show();
                return;
            }

            if (validateType == AuthenticationUtils.PasswordValidateType.PASSWORD_TOO_EASY) {
                invalidateField(passwordEditText);
                generateInvalidSnackBar(getView(), getString(R.string.password_too_easy)).show();
                return;
            }

            resetEditText(passwordEditText);

            Account account = weMessage.get().getMessageDatabase().getAccountByEmail(email);

            if (account == null){
                generateInvalidSnackBar(getView(), getString(R.string.account_lookup_not_found)).show();
                return;
            }

            if (!BCrypt.checkPassword(password, account.getEncryptedPassword())){
                invalidateField(passwordEditText);
                generateInvalidSnackBar(getView(), getString(R.string.offline_mode_incorrect_password)).show();
                return;
            }

            weMessage.get().getSharedPreferences().edit().putString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, email)
                    .putString(weMessage.SHARED_PREFERENCES_LAST_HASHED_PASSWORD, account.getEncryptedPassword()).apply();
            weMessage.get().signOut(false);
            weMessage.get().signIn(account);

            if (callbacks != null) callbacks.onAccountSwitched();
        }

        private void invalidateField(final EditText editText){
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.colorHeader), getResources().getColor(R.color.invalidRed));
            colorAnimation.setDuration(200);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    editText.getBackground().setColorFilter((int) animation.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                    editText.setTextColor((int) animation.getAnimatedValue());
                }
            });

            Animation invalidShake = AnimationUtils.loadAnimation(getActivity(), R.anim.invalid_shake);
            invalidShake.setInterpolator(new CycleInterpolator(7F));

            colorAnimation.start();
            editText.startAnimation(invalidShake);
        }

        private void resetEditText(EditText editText){
            editText.getBackground().setColorFilter(getResources().getColor(R.color.colorHeader), PorterDuff.Mode.SRC_ATOP);
            editText.setTextColor(oldEditTextColor);
        }

        private void clearEditText(final EditText editText, boolean closeKeyboard){
            if (closeKeyboard) {
                closeKeyboard();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    editText.clearFocus();
                }
            }, 100);
        }

        private void closeKeyboard(){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }

        private Snackbar generateInvalidSnackBar(View view, String message){
            final Snackbar snackbar = Snackbar.make(view, message, 5000);

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

            return snackbar;
        }
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ContactInfo contact;

        private ImageView contactPictureView;
        private TextView contactDisplayNameView;
        private TextView contactHandle;

        public ContactHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_select_contact, parent, false));

            contactPictureView = itemView.findViewById(R.id.contactPictureView);
            contactDisplayNameView = itemView.findViewById(R.id.contactDisplayNameView);
            contactHandle = itemView.findViewById(R.id.contactHandle);

            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.selectedContactBubble).setVisibility(View.GONE);
        }

        public void bind(ContactInfo contact){
            if (!excludeContact(contact)) {
                this.contact = contact;

                contactDisplayNameView.setText(contact.getDisplayName());
                contactHandle.setText(contact.pullHandle((handleChatUuidMap != null && StringUtils.isEmpty(handleChatUuidMap[0])) && !isSwitchAccountsMode).getHandleID());

                Glide.with(ContactSelectActivity.this).load(IOUtils.getContactIconUri(contact.pullHandle((handleChatUuidMap != null && StringUtils.isEmpty(handleChatUuidMap[0])) && !isSwitchAccountsMode), IOUtils.IconSize.NORMAL)).into(contactPictureView);
            }
        }

        @Override
        public void onClick(View view) {
            if (isSwitchAccountsMode){
                performAction(contact.pullHandle(false).getHandleID());
            }else if (!StringUtils.isEmpty(handleChatUuidMap[0])){
                performAction(contact.findRoot().getUuid().toString());
            }else if (!StringUtils.isEmpty(chatUuid)) {
                performAction(contact.pullHandle(true).getHandleID());
            }
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        private ArrayList<ContactInfo> originalList = new ArrayList<>();
        private ArrayList<SearchableContact> contacts = new ArrayList<>();
        private AsyncTask<String, Void, Collection> searchTask;

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ContactSelectActivity.this);

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
                if (!excludeContact(c)){
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
                    if (!excludeContact(c)){
                        if (c instanceof Contact && contacts.contains(new SearchableContact(c))) contacts.removeAll(Collections.singleton(new SearchableContact(c)));

                        contacts.add(new SearchableContact(c));
                        notifyItemInserted(contacts.size() - 1);
                    }
                }
            });
        }

        public void addContactToOriginal(ContactInfo c){
            if (!excludeContact(c)){
                if (c instanceof Contact && originalList.contains(c)) originalList.removeAll(Collections.singleton(c));

                originalList.add(c);
            }
        }

        public void updateContact(ContactInfo oldData, ContactInfo newData){
            if (!excludeContact(newData)) {

                new AsyncTask<ContactInfo, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(ContactInfo... params) {
                        int i = 0;
                        for (SearchableContact searchableContact : new ArrayList<>(contacts)) {
                            ContactInfo contactInfo = searchableContact.getContact();

                            if (contactInfo.equals(params[0])) {
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
            if (!excludeContact(newData)) {
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
            if (!excludeContact(c)) {
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
            if (!excludeContact(c)) {
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
                            if (!excludeContact((ContactInfo) o)) {
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
                    if (!isFinishing() && !isDestroyed()) {
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

    interface IPredicate<T> {
        boolean apply(T type);
    }
}