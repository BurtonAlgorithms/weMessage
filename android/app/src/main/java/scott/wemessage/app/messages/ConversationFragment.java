package scott.wemessage.app.messages;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import scott.wemessage.app.view.dialog.DialogDisplayer;
import scott.wemessage.app.view.messages.MessageView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;

public class ConversationFragment extends Fragment implements MessageManager.Callbacks {

    private final String TAG = "ConversationFragment";
    private final int MESSAGE_QUEUE_AMOUNT = 50;

    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private MessagesList messageList;
    private MessagesListAdapter<IMessage> messageListAdapter;
    private Chat chat;
    private boolean isBoundToConnectionService = false;

    private BroadcastReceiver messageListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //TODO: All broadcasts need to go here, figure out how are we going to handle displaying them

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }
        MessageManager messageManager = MessageManager.getInstance(getActivity());
        MessageDatabase messageDatabase = WeApp.get().getMessageDatabase();

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
                this.chat = chat;
            }
        }catch (Exception ex){
            Intent returnIntent = new Intent(WeApp.get(), ChatListActivity.class);
            returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, getString(R.string.conversation_load_failure));

            AppLogger.error(TAG, "Could not load ConversationFragment because an error occurred", ex);
            startActivity(returnIntent);
            getActivity().finish();
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

        messageList = (MessagesList) view.findViewById(R.id.messagesList);

        MessagesListAdapter<IMessage> messageListAdapter = new MessagesListAdapter<>(WeApp.get().getCurrentAccount().getUuid().toString(), new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(ConversationFragment.this).load(url).into(imageView);
            }
        });

        messageList.setAdapter(messageListAdapter);
        this.messageListAdapter = messageListAdapter;

        messageManager.queueMessages(chat, 0, MESSAGE_QUEUE_AMOUNT, true);

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
    public void onChatListRefresh(ConcurrentHashMap<String, Chat> chats) {

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
    public void onMessagesQueueFinish(ConcurrentHashMap<String, Message> messages) {
        for (Message message : messages.values()){

            if (message.getChat().getUuid().toString().equals(chat.getUuid().toString())) {
                MessageView messageView = new MessageView(MessageManager.getInstance(getActivity()), message);

                messageListAdapter.addToStart(messageView, true);
            }
        }
    }

    @Override
    public void onMessagesRefresh() {

    }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) {

    }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {

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
}