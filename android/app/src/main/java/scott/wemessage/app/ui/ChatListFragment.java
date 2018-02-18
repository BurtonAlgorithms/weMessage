package scott.wemessage.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.R;
import scott.wemessage.app.messages.MessageCallbacks;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.messages.ActionMessage;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.messages.MessageBase;
import scott.wemessage.app.models.users.ContactInfo;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.ui.activities.ConversationActivity;
import scott.wemessage.app.ui.activities.CreateChatActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.SettingsActivity;
import scott.wemessage.app.ui.view.chat.ChatDialogView;
import scott.wemessage.app.ui.view.chat.ChatDialogViewHolder;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.StringUtils;

public class ChatListFragment extends MessagingFragment implements MessageCallbacks {

    private final String GO_BACK_REASON_ALERT_TAG = "GoBackReasonAlert";
    private final String UPDATE_LOG_ALERT_TAG = "UpdateLogAlert";
    private final int ERROR_SNACKBAR_DURATION = 5;

    private String callbackUuid;
    private AtomicBoolean isParseChatsTaskRunning = new AtomicBoolean(false);
    private boolean hasConversations = false;

    private LinearLayout noConversationsView;
    private FloatingActionButton addChatButton;
    private DialogsList dialogsList;
    private DialogsListAdapter<IDialog> dialogsListAdapter;

    private BroadcastReceiver chatListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), ChatListFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), ChatListFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), ChatListFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new Runnable() {
                    @Override
                    public void run() {
                        LaunchActivity.launchActivity(getActivity(), ChatListFragment.this, false);
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_NEW_MESSAGE_ERROR)){
                if (getView() != null) {
                    showErroredSnackbar(getString(R.string.new_message_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                if (getView() != null) {
                    showErroredSnackbar(getString(R.string.send_message_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR)) {
                if (getView() != null) {
                    showErroredSnackbar(getString(R.string.message_update_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (getView() != null) {
                    if (intent.getExtras() != null){
                        showErroredSnackbar(intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE));
                    }else {
                        showErroredSnackbar(getString(R.string.action_perform_error_default));
                    }
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                if (getView() != null) {
                    showErroredSnackbar(getString(R.string.result_process_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_FAILED)){
                DialogDisplayer.showContactSyncResult(false, getActivity(), getFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS)){
                DialogDisplayer.showContactSyncResult(true, getActivity(), getFragmentManager());
            }else if(intent.getAction().equals(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION)){
                DialogDisplayer.showNoAccountsFoundDialog(getActivity(), getFragmentManager());
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        MessageManager messageManager = weMessage.get().getMessageManager();
        IntentFilter broadcastIntentFilter = new IntentFilter();

        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_FORCED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_NEW_MESSAGE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_SEND_MESSAGE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_ACTION_PERFORM_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_RESULT_PROCESS_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_FAILED);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_CONTACT_SYNC_SUCCESS);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION);

        callbackUuid = UUID.randomUUID().toString();
        messageManager.hookCallbacks(callbackUuid, this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(chatListBroadcastReceiver, broadcastIntentFilter);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        dialogsList = view.findViewById(R.id.chatDialogsList);
        noConversationsView = view.findViewById(R.id.noConversationsView);
        addChatButton = view.findViewById(R.id.addChatButton);

        Toolbar toolbar = getActivity().findViewById(R.id.chatListToolbar);

        toolbar.findViewById(R.id.chatListSettingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);

                startActivity(launcherIntent);
                getActivity().finish();
            }
        });

        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        DialogsListAdapter<IDialog> dialogsListAdapter = new DialogsListAdapter<>(R.layout.list_item_chat, ChatDialogViewHolder.class, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(ChatListFragment.this).load(url).into(imageView);
            }
        });

        dialogsListAdapter.setDatesFormatter(new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                return AndroidUtils.processDate(getContext(), date, true, true);
            }
        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<IDialog>() {
            @Override
            public void onDialogClick(IDialog dialog) {
                if (MmsManager.isDefaultSmsApp()){
                    if (weMessage.get().getMmsManager().getSyncingChats().containsKey(dialog.getId())){
                        showErroredSnackbar(getString(R.string.sms_chat_syncing));
                        return;
                    }
                }

                Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

                launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, getActivity().getClass().getName());
                launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, dialog.getId());

                startActivity(launcherIntent);
                getActivity().finish();
            }
        });

        dialogsList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                closeDeleteButtonViews();
                return false;
            }
        });

        dialogsList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                closeDeleteButtonViews();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) { }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launcherIntent = new Intent(weMessage.get(), CreateChatActivity.class);

                startActivity(launcherIntent);
                getActivity().finish();
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);
        this.dialogsListAdapter = dialogsListAdapter;

        if (getActivity().getIntent() != null && !StringUtils.isEmpty(getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON))){
            DialogDisplayer.generateAlertDialog(getString(R.string.word_error), getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON)).show(getFragmentManager(), GO_BACK_REASON_ALERT_TAG);
        }

        showUpdateDialog();
        parseChats();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        toggleNoConversations(dialogsListAdapter.isEmpty());
        dialogsListAdapter.sortByLastMessageDate();

        super.onResume();
    }

    @Override
    public void onDestroy() {
        MessageManager messageManager = weMessage.get().getMessageManager();

        if (dialogsListAdapter != null) dialogsListAdapter.clear();
        messageManager.unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(chatListBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public void onContactCreate(ContactInfo contact) { }

    @Override
    public void onContactUpdate(ContactInfo oldData, ContactInfo newData) { }

    @Override
    public void onContactListRefresh(List<? extends ContactInfo> handles) { }

    @Override
    public void onChatAdd(final Chat chat) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleNoConversations(false);
                if (!isChatBlocked(chat)) {
                    dialogsListAdapter.addItem(new ChatDialogView(chat));
                    dialogsList.scrollToPosition(0);
                }
            }
        });
    }

    @Override
    public void onChatUpdate(final Chat oldData, Chat newData) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(newData);

        if (!isChatBlocked(newData)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(oldData.getIdentifier(), chatDialogView);

                    if (((LinearLayoutManager) dialogsList.getLayoutManager()).findFirstCompletelyVisibleItemPosition() <= 2){
                        dialogsList.scrollToPosition(0);
                    }
                }
            });
        }
    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(chat);

        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onChatRename(Chat chat, String displayName) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(chat);

        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onParticipantAdd(Chat chat, Handle handle) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(chat);

        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onParticipantRemove(Chat chat, Handle handle) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(chat);

        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onLeaveGroup(Chat chat) {
        if (getActivity() == null) return;
        final ChatDialogView chatDialogView = new ChatDialogView(chat);

        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onChatDelete(final Chat chat) {
        if (getActivity() == null) return;
        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.deleteById(chat.getIdentifier());
                    toggleNoConversations(dialogsListAdapter.isEmpty());
                }
            });
        }
    }

    @Override
    public void onChatListRefresh(final List<Chat> chats) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogsListAdapter != null) {
                    dialogsListAdapter.clear();

                    for (Chat chat : chats) {
                        if (!isChatBlocked(chat)) {
                            dialogsListAdapter.addItem(new ChatDialogView(chat));
                        }
                    }
                    dialogsListAdapter.sortByLastMessageDate();
                    toggleNoConversations(dialogsListAdapter.isEmpty());
                    dialogsList.scrollToPosition(0);
                }
            }
        });
    }

    @Override
    public void onMessageAdd(final Message message) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isChatBlocked(message.getChat())) {
                    MessageView messageView = new MessageView(message);
                    dialogsListAdapter.updateDialogWithMessage(message.getChat().getIdentifier(), messageView);

                    if (((LinearLayoutManager) dialogsList.getLayoutManager()).findFirstCompletelyVisibleItemPosition() <= 2){
                        dialogsList.scrollToPosition(0);
                    }
                }
            }
        });
    }

    @Override
    public void onMessageUpdate(Message oldData, final Message newData) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isChatBlocked(newData.getChat())) {
                    MessageView messageView = new MessageView(newData);
                    dialogsListAdapter.updateDialogWithMessage(newData.getChat().getIdentifier(), messageView);

                    if (((LinearLayoutManager) dialogsList.getLayoutManager()).findFirstCompletelyVisibleItemPosition() <= 2){
                        dialogsList.scrollToPosition(0);
                    }
                }
            }
        });
    }

    @Override
    public void onMessageDelete(final Message message) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isChatBlocked(message.getChat())) {
                    MessageView messageView = new MessageView(weMessage.get().getMessageDatabase().getLastMessageFromChat(message.getChat()));
                    dialogsListAdapter.updateDialogWithMessage(message.getChat().getIdentifier(), messageView);
                }
            }
        });
    }

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

    @SuppressWarnings("unchecked")
    private void parseChats(){
        if (isParseChatsTaskRunning.get()) return;
        isParseChatsTaskRunning.set(true);

        List<Chat> chats = weMessage.get().getMessageManager().getChats();

        if (!chats.isEmpty() && !isChatBlocked(chats.get(0))) dialogsListAdapter.addItem(new ChatDialogView(chats.get(0)));

        new AsyncTask<List<Chat>, ChatDialogView, Void>(){
            @Override
            protected Void doInBackground(List<Chat>[] params) {
                ArrayList<ChatDialogView> dialogViews = new ArrayList<>();

                for (int i = 1; i < params[0].size(); i++) {
                    Chat chat = params[0].get(i);
                    ChatDialogView chatDialogView = new ChatDialogView(chat);

                    if (!isChatBlocked(chat)) dialogViews.add(chatDialogView);

                    if (dialogViews.size() == 5 || i == params[0].size() - 1){
                        publishProgress(dialogViews.toArray(new ChatDialogView[dialogViews.size()]));
                        dialogViews.clear();
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(ChatDialogView... chatDialogViewBatch) {
                if (!hasConversations) toggleNoConversations(false);

                for (ChatDialogView dialogView : chatDialogViewBatch){
                    dialogsListAdapter.addItem(dialogView);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialogsListAdapter.sortByLastMessageDate();
                dialogsList.scrollToPosition(0);
                isParseChatsTaskRunning.set(false);
            }
        }.execute(chats);
    }

    private void showUpdateDialog(){
        SharedPreferences preferences = weMessage.get().getSharedPreferences();

        if (preferences.getBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, false)){
            int lastVersion = preferences.getInt(weMessage.SHARED_PREFERENCES_LAST_VERSION, -1);

            DialogDisplayer.AlertDialogFragment dialogFragment = null;

            if (lastVersion == 10){
                dialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.update_log), getString(R.string.version_11_update));
            }else if (lastVersion == 11){
                dialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.update_log), getString(R.string.version_12_update));
            }

            if (dialogFragment != null){
                dialogFragment.linkify(true);
                dialogFragment.setCancelableOnTouchedOutside(false);
                dialogFragment.show(getFragmentManager(), UPDATE_LOG_ALERT_TAG);
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, false);
            editor.apply();
        }
    }

    private void closeDeleteButtonViews(){
        if (dialogsList != null) {
            for (int childCount = dialogsList.getChildCount(), i = 0; i < childCount; ++i) {
                RecyclerView.ViewHolder holder = dialogsList.getChildViewHolder(dialogsList.getChildAt(i));

                if (holder instanceof ChatDialogViewHolder) {
                    ((ChatDialogViewHolder) holder).closeView();
                }
            }
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private void toggleNoConversations(boolean bool){
        hasConversations = !bool;

        if (bool){
            if (dialogsList.getVisibility() != View.GONE) {
                dialogsList.setVisibility(View.GONE);
            }
            if (noConversationsView.getVisibility() != View.VISIBLE) {
                noConversationsView.setVisibility(View.VISIBLE);
            }
        }else {
            if (dialogsList.getVisibility() != View.VISIBLE) {
                dialogsList.setVisibility(View.VISIBLE);
            }
            if (noConversationsView.getVisibility() != View.GONE) {
                noConversationsView.setVisibility(View.GONE);
            }
        }
    }

    private void showErroredSnackbar(String message){
        if (getView() != null) {
            final Snackbar snackbar = Snackbar.make(getView(), message, ERROR_SNACKBAR_DURATION * 1000);

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

    private boolean isChatBlocked(Chat chat){
        try {
            return chat instanceof PeerChat && (((PeerChat) chat).getHandle().isBlocked());
        }catch (NullPointerException ex){
            return false;
        }
    }
}