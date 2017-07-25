package scott.wemessage.app.chats;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import scott.wemessage.app.chats.objects.Chat;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.launch.LaunchActivity;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.view.chat.ChatDialogView;
import scott.wemessage.app.view.dialog.DialogDisplayer;
import scott.wemessage.app.view.messages.MessageView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.connection.ConnectionMessage;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;

public class ChatListFragment extends Fragment implements MessageManager.Callbacks {

    //TODO: Listen for broadcast of new message error; message update error; listen for message failure bundles

    private final String TAG = "ChatListFragment";

    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
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


            }else if(intent.getAction().equals(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR)) {


            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){


            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){


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
        broadcastIntentFilter.addAction(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_ACTION_PERFORM_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_RESULT_PROCESS_ERROR);

        messageManager.hookCallbacks(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(chatListBroadcastReceiver, broadcastIntentFilter);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setCustomView(R.layout.action_bar_chat_list);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        dialogsList = (DialogsList) view.findViewById(R.id.chatDialogsList);

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

        dialogsList.setAdapter(dialogsListAdapter);

        this.dialogsListAdapter = dialogsListAdapter;

        for (Chat chat : MessageManager.getInstance(getContext()).getChats().values()){
            dialogsListAdapter.addItem(new ChatDialogView(MessageManager.getInstance(getContext()), chat));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                dialogsListAdapter.updateDialogWithMessage(chatDialogView.getId(), chatDialogView.getLastMessage());
            }
        });
    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) {

    }

    @Override
    public void onChatRename(Chat chat, String displayName) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateDialogWithMessage(chatDialogView.getId(), chatDialogView.getLastMessage());
            }
        });

        //TODO: Send message saying this, for rest too
    }

    @Override
    public void onParticipantAdd(Chat chat, Contact contact) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateDialogWithMessage(chatDialogView.getId(), chatDialogView.getLastMessage());
            }
        });
    }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateDialogWithMessage(chatDialogView.getId(), chatDialogView.getLastMessage());
            }
        });
    }

    @Override
    public void onLeaveGroup(Chat chat) {
        final ChatDialogView chatDialogView = new ChatDialogView(MessageManager.getInstance(getContext()), chat);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.updateDialogWithMessage(chatDialogView.getId(), chatDialogView.getLastMessage());
            }
        });
    }

    @Override
    public void onChatDelete(final Chat chat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogsListAdapter.deleteById(chat.getUuid().toString());
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
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMessagesQueueFinish(ConcurrentHashMap<String, Message> messages) {

    }

    @Override
    public void onMessagesRefresh() {

    }

    @Override
    public void onActionResultReceived(ConnectionMessage connectionMessage, JSONAction jsonAction, List<ReturnType> returnTypes) {
        //TODO: Work here
    }

    @Override
    public void onMessageResultReceived(ConnectionMessage connectionMessage, JSONMessage jsonMessage, List<ReturnType> returnTypes) {

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

            MessageManager.dump(getContext());
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
}