package scott.wemessage.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import scott.wemessage.R;
import scott.wemessage.app.messages.MessageCallbacks;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.ui.activities.ConversationActivity;
import scott.wemessage.app.ui.activities.CreateChatActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.SettingsActivity;
import scott.wemessage.app.ui.view.chat.ChatDialogView;
import scott.wemessage.app.ui.view.chat.ChatDialogViewHolder;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.ReturnType;

public class ChatListFragment extends MessagingFragment implements MessageCallbacks {

    private final String TAG = "ChatListFragment";
    private final String GO_BACK_REASON_ALERT_TAG = "GoBackReasonAlert";
    private final int ERROR_SNACKBAR_DURATION = 5000;

    private String callbackUuid;

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
                if (DateFormatter.isToday(date)){
                    return DateFormatter.format(date, "h:mm a");
                }else if (DateFormatter.isYesterday(date)){
                    return getString(R.string.word_yesterday);
                }else {
                    if (DateFormatter.isCurrentYear(date)){
                        return DateFormatter.format(date, "MMMM d");
                    }else {
                        return DateFormatter.format(date, "MMMM d, yyyy");
                    }
                }
            }
        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<IDialog>() {
            @Override
            public void onDialogClick(IDialog dialog) {
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
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
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

        for (Chat chat : weMessage.get().getMessageManager().getChats().values()){
            if (!isChatBlocked(chat)) {
                dialogsListAdapter.addItem(new ChatDialogView(chat));
            }
        }

        if (getActivity().getIntent() != null && getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON) != null){
            DialogDisplayer.generateAlertDialog(getString(R.string.word_error), getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON)).show(getFragmentManager(), GO_BACK_REASON_ALERT_TAG);
        }

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

        dialogsListAdapter.clear();
        messageManager.unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(chatListBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) {

    }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {

    }

    @Override
    public void onContactListRefresh(List<Contact> contacts) {

    }

    @Override
    public void onChatAdd(final Chat chat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleNoConversations(false);
                if (!isChatBlocked(chat)) {
                    dialogsListAdapter.addItem(new ChatDialogView(chat));
                }
            }
        });
    }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) {
        final ChatDialogView chatDialogView = new ChatDialogView(newData);

        if (!isChatBlocked(newData)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.updateItemById(chatDialogView);
                }
            });
        }
    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {
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
    public void onParticipantAdd(Chat chat, Contact contact) {
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
    public void onParticipantRemove(Chat chat, Contact contact) {
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
        if (!isChatBlocked(chat)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogsListAdapter.deleteById(chat.getUuid().toString());
                    toggleNoConversations(dialogsListAdapter.isEmpty());
                }
            });
        }
    }

    @Override
    public void onChatListRefresh(final List<Chat> chats) {
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
                }
            }
        });
    }

    @Override
    public void onMessageAdd(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isChatBlocked(message.getChat())) {
                    MessageView messageView = new MessageView(message);
                    dialogsListAdapter.updateDialogWithMessage(message.getChat().getUuid().toString(), messageView);
                }
            }
        });
    }

    @Override
    public void onMessageUpdate(Message oldData, Message newData) {

    }

    @Override
    public void onMessageDelete(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isChatBlocked(message.getChat())) {
                    MessageView messageView = new MessageView(weMessage.get().getMessageDatabase().getLastMessageFromChat(message.getChat()));
                    dialogsListAdapter.updateDialogWithMessage(message.getChat().getUuid().toString(), messageView);
                }
            }
        });
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
    public void onMessageSendFailure(final JSONMessage jsonMessage, final ReturnType returnType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessageSendFailureSnackbar(jsonMessage, returnType);
            }
        });
    }

    @Override
    public void onActionPerformFailure(final JSONAction jsonAction, final ReturnType returnType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showActionFailureSnackbar(jsonAction, returnType);
            }
        });
    }

    @Override
    public void onAttachmentSendFailure(final FailReason failReason) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAttachmentSendFailureSnackbar(failReason);
            }
        });
    }

    @Override
    public void onAttachmentReceiveFailure(final FailReason failReason) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAttachmentReceiveFailureSnackbar(failReason);
            }
        });
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

    private void goToLauncher(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void goToLauncherReconnect(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private void toggleNoConversations(boolean bool){
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
        return chat instanceof PeerChat && (weMessage.get().getMessageDatabase().getContactByHandle(((PeerChat) chat).getContact().getHandle()).isBlocked());
    }
}