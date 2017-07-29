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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.WeApp;
import scott.wemessage.app.activities.ChatListActivity;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.launch.LaunchActivity;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.view.chat.ChatTitleView;
import scott.wemessage.app.view.dialog.DialogDisplayer;
import scott.wemessage.app.view.messages.MessageView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;

public class ConversationFragment extends Fragment implements MessageManager.Callbacks {

    private final String TAG = "ConversationFragment";
    private final Object chatLock = new Object();
    private final int MESSAGE_QUEUE_AMOUNT = 50;
    private final int ERROR_SNACKBAR_DURATION = 5000;

    private Chat chat;
    private ChatTitleView chatTitleView;
    private MessagesList messageList;
    private MessagesListAdapter<IMessage> messageListAdapter;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private boolean isBoundToConnectionService = false;

    //TODO: Participant duplication caused by update title view everytime its caused? toolbar code?

    private BroadcastReceiver messageListBroadcastReceiver = new BroadcastReceiver() {
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
        MessageDatabase messageDatabase = WeApp.get().getMessageDatabase();

        if (savedInstanceState == null) {
            try {
                Intent startingIntent = getActivity().getIntent();
                Class startingClass = Class.forName(startingIntent.getStringExtra(weMessage.BUNDLE_RETURN_POINT));

                Chat chat = messageDatabase.getChatByUuid(startingIntent.getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT));

                if (chat == null) {
                    Intent returnIntent = new Intent(WeApp.get(), startingClass);
                    returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, getString(R.string.conversation_load_failure));

                    startActivity(returnIntent);
                    getActivity().finish();
                } else {
                    setChat(chat);
                }
            } catch (Exception ex) {
                Intent returnIntent = new Intent(WeApp.get(), ChatListActivity.class);
                returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, getString(R.string.conversation_load_failure));

                AppLogger.error(TAG, "Could not load ConversationFragment because an error occurred", ex);
                startActivity(returnIntent);
                getActivity().finish();
            }
        }else {
            chat = messageDatabase.getChatByUuid(savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT));
        }

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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageListBroadcastReceiver, broadcastIntentFilter);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        MessageManager messageManager = MessageManager.getInstance(getActivity());

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.conversationToolbar);
        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        chatTitleView = (ChatTitleView) toolbar.findViewById(R.id.chatTitleView);
        chatTitleView.setChat(chat);

        messageList = (MessagesList) view.findViewById(R.id.messagesList);

        MessagesListAdapter<IMessage> messageListAdapter = new MessagesListAdapter<>(WeApp.get().getCurrentAccount().getUuid().toString(), new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(ConversationFragment.this).load(url).into(imageView);
            }
        });

        messageList.setAdapter(messageListAdapter);
        this.messageListAdapter = messageListAdapter;

        messageManager.queueMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, chat.getUuid().toString());

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
        MessageManager messageManager = MessageManager.getInstance(getActivity());

        messageListAdapter.clear();
        messageManager.unhookCallbacks(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageListBroadcastReceiver);

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
    public void onChatAdd(Chat chat) {

    }

    @Override
    public void onChatUpdate(Chat oldData, final Chat newData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(newData)){
                    setChat(newData);
                }
            }
        });
    }

    @Override
    public void onUnreadMessagesUpdate(final Chat chat, boolean hasUnreadMessages) {
        //TODO: Date header stuff
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(chat)){
                    setChat(chat);
                }
            }
        });
    }

    @Override
    public void onChatRename(final Chat chat, String displayName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(chat)){
                    setChat(chat);
                }
            }
        });
    }

    @Override
    public void onParticipantAdd(final Chat chat, Contact contact) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isChatThis(chat)){
                    setChat(chat);
                }
            }
        });
    }

    @Override
    public void onParticipantRemove(final Chat chat, Contact contact) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(chat)){
                    setChat(chat);
                }
            }
        });
    }

    @Override
    public void onLeaveGroup(final Chat chat) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(chat)){
                    setChat(chat);
                }
            }
        });
    }

    @Override
    public void onChatDelete(Chat chat) {
        if (isChatThis(chat)){
            Intent returnIntent = new Intent(WeApp.get(), ChatListActivity.class);
            returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, getString(R.string.chat_delete_message_go_back));

            startActivity(returnIntent);
            getActivity().finish();
        }
    }

    @Override
    public void onChatListRefresh(ConcurrentHashMap<String, Chat> chats) {

    }

    @Override
    public void onMessageAdd(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(message.getChat())){
                    MessageView messageView = new MessageView(MessageManager.getInstance(getActivity()), message);
                    messageListAdapter.addToStart(messageView, true);
                }
            }
        });
    }

    @Override
    public void onMessageUpdate(Message oldData, final Message newData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(newData.getChat())){
                    messageListAdapter.update(new MessageView(MessageManager.getInstance(getActivity()), newData));
                }
            }
        });
    }

    @Override
    public void onMessageDelete(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isChatThis(message.getChat())){
                    messageListAdapter.deleteById(message.getUuid().toString());
                }
            }
        });
    }

    @Override
    public void onMessagesQueueFinish(final ConcurrentHashMap<String, Message> messages) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Message message : messages.values()){
                    if (isChatThis(message.getChat())) {
                        MessageView messageView = new MessageView(MessageManager.getInstance(getActivity()), message);

                        messageListAdapter.addToStart(messageView, true);
                    }
                }
            }
        });
    }

    @Override
    public void onMessagesRefresh() {
        MessageManager.getInstance(getActivity()).queueMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);

        //TODO: Be sure to get action messages too
    }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) {
        //TODO: Stuff here
    }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {
        //TODO: Stuff here
    }

    private Chat getChat(){
        synchronized (chatLock){
            return chat;
        }
    }

    private void setChat(Chat chat){
        synchronized (chatLock){
            this.chat = chat;

            if (chatTitleView != null) {
                this.chatTitleView.setChat(chat);
            }
        }
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

    private boolean isChatThis(Chat c){
        return c.getUuid().toString().equals(getChat().getUuid().toString());
    }
}