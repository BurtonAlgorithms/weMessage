package scott.wemessage.app.messages;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.WeApp;
import scott.wemessage.app.activities.ConversationActivity;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.launch.LaunchActivity;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.utils.reflection.ChatKitHelper;
import scott.wemessage.app.utils.view.RecyclerSwiper;
import scott.wemessage.app.view.chat.ChatDialogView;
import scott.wemessage.app.view.dialog.DialogDisplayer;
import scott.wemessage.app.view.messages.MessageView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;

public class ChatListFragment extends Fragment implements MessageManager.Callbacks {

    private final String TAG = "ChatListFragment";
    private final String GO_BACK_REASON_ALERT_TAG = "GoBackReasonAlert";
    private final int ERROR_SNACKBAR_DURATION = 5000;

    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private LinearLayout noConversationsView;
    private DialogsList dialogsList;
    private DialogsListAdapter<IDialog> dialogsListAdapter;
    private boolean isBoundToConnectionService = false;

    private BroadcastReceiver chatListBroadcastReceiver = new BroadcastReceiver() {
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
            }else if(intent.getAction().equals(weMessage.BROADCAST_NEW_MESSAGE_ERROR)){
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.new_message_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.send_message_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR)) {
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.message_update_error));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (getView() != null) {
                    if (intent.getExtras() != null){
                        generateErroredSnackBar(getView(), intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE));
                    }else {
                        generateErroredSnackBar(getView(), getString(R.string.action_perform_error_default));
                    }
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.result_process_error));
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

        MessageManager messageManager = MessageManager.getInstance(getActivity());
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

        messageManager.hookCallbacks(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(chatListBroadcastReceiver, broadcastIntentFilter);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        dialogsList = (DialogsList) view.findViewById(R.id.chatDialogsList);
        noConversationsView = (LinearLayout) view.findViewById(R.id.noConversationsView);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.chatListToolbar);
        toolbar.setTitle("");

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        new RecyclerSwiper(getActivity(), dialogsList) {
            @Override
            public void instantiateUnderlayButtons(final RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new RecyclerSwiper.UnderlayButton(
                        null,
                        R.drawable.trash_icon,
                        getResources().getColor(R.color.deleteButtonRed),
                        new RecyclerSwiper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                MessageManager.getInstance(getActivity()).deleteChat(WeApp.get().getMessageDatabase().
                                        getChatByUuid(ChatKitHelper.getChatIdFromPosition(dialogsListAdapter, viewHolder.getAdapterPosition())), true);
                            }
                        }
                ));
            }
        };

        DialogsListAdapter<IDialog> dialogsListAdapter = new DialogsListAdapter<>(R.layout.list_item_chat, new ImageLoader() {
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
                    return getString(R.string.yesterday);
                }else {
                    if (DateFormatter.isCurrentYear(date)){
                        return DateFormatter.format(date, "MMMM d");
                    }else {
                        return DateFormatter.format(date, "MMMM d yyyy");
                    }
                }
            }
        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<IDialog>() {
            @Override
            public void onDialogClick(IDialog dialog) {
                Intent launcherIntent = new Intent(WeApp.get(), ConversationActivity.class);

                launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, getActivity().getClass().getName());
                launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, dialog.getId());

                startActivity(launcherIntent);
                getActivity().finish();
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);
        this.dialogsListAdapter = dialogsListAdapter;

        for (Chat chat : MessageManager.getInstance(getContext()).getChats().values()){
            dialogsListAdapter.addItem(new ChatDialogView(MessageManager.getInstance(getContext()), chat));
        }

        if (getActivity().getIntent() != null && getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON) != null){
            DialogDisplayer.generateAlertDialog("", getActivity().getIntent().getStringExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON)).show(getFragmentManager(), GO_BACK_REASON_ALERT_TAG);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        if (!isServiceRunning(ConnectionService.class)){
            goToLauncher();
        }

        toggleNoConversations(dialogsListAdapter.isEmpty());
        dialogsListAdapter.sortByLastMessageDate();

        super.onResume();
    }

    @Override
    public void onDestroy() {
        MessageManager messageManager = MessageManager.getInstance(getActivity());

        dialogsListAdapter.clear();
        messageManager.unhookCallbacks(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(chatListBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }
        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) {

    }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {

    }

    @Override
    public void onContactListRefresh(ConcurrentHashMap<String, Contact> contacts) {

    }

    @Override
    public void onChatAdd(final Chat chat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleNoConversations(false);
                dialogsListAdapter.addItem(new ChatDialogView(MessageManager.getInstance(getContext()), chat));
            }
        });
    }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), newData);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onChatRename(Chat chat, String displayName) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onParticipantAdd(Chat chat, Contact contact) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onLeaveGroup(Chat chat) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateItemById(chatDialogView);
            }
        });
    }

    @Override
    public void onChatDelete(final Chat chat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.deleteById(chat.getUuid().toString());
                toggleNoConversations(dialogsListAdapter.isEmpty());
            }
        });
    }

    @Override
    public void onChatListRefresh(final ConcurrentHashMap<String, Chat> chats) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogsListAdapter != null) {
                    dialogsListAdapter.clear();

                    for (Chat chat : chats.values()) {
                        dialogsListAdapter.addItem(new ChatDialogView(MessageManager.getInstance(getContext()), chat));
                    }
                }
            }
        });
    }

    @Override
    public void onMessageAdd(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageView messageView = new MessageView(MessageManager.getInstance(getContext()), message);
                dialogsListAdapter.updateDialogWithMessage(message.getChat().getUuid().toString(), messageView);
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
                MessageView messageView = new MessageView(MessageManager.getInstance(getContext()), WeApp.get().getMessageDatabase().getLastMessageFromChat(message.getChat()));
                dialogsListAdapter.updateDialogWithMessage(message.getChat().getUuid().toString(), messageView);
            }
        });
    }

    @Override
    public void onMessagesQueueFinish(ConcurrentHashMap<String, Message> messages) {

    }

    @Override
    public void onMessagesRefresh() {

    }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) {


        //TODO: Impl Soon


    }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {


        //TODO: Impl Soon


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

    private void goToLauncher(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(WeApp.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    private Snackbar generateErroredSnackBar(View view, String message){
        final Snackbar snackbar = Snackbar.make(view, message, ERROR_SNACKBAR_DURATION);

        snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.lightRed));

        return snackbar;
    }
}