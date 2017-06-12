package scott.wemessage.app.chats;

import android.app.ActivityManager;
import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.chats.objects.Chat;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.view.dialog.DialogDisplayer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.connection.ConnectionMessage;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;

public class ChatListFragment extends Fragment implements MessageManager.Callbacks {

    //TODO: Listen for broadcast of new message error; message update error; listen for message failure bundles

    private final String TAG = "ChatListFragment";

    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private boolean isBoundToConnectionService = false;

    private BroadcastReceiver chatListBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(weMessage.BROADCAST_CONNECTION_SERVICE_STOPPED)){
                unbindService();
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_SERVER_CLOSED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_server_closed_message), new DialogInterface.OnDismissListener(){
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_ERROR)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_unknown_message), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_FORCED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_force_disconnect_message), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToLauncher();
                    }
                });
            }else if(intent.getAction().equals(weMessage.BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED)){
                showDisconnectReasonDialog(intent, getString(R.string.connection_error_client_disconnect_message), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
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

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);



        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        MessageManager messageManager = MessageManager.getInstance(getActivity());

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

    }

    @Override
    public void onMessagesRefresh() {

    }

    @Override
    public void onActionResultReceived(ConnectionMessage connectionMessage, JSONAction jsonAction, List<ReturnType> returnTypes) {

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
        Intent launcherIntent = new Intent(getActivity(), LauncherActivity.class);

        MessageManager.dump(getContext());
        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, DialogInterface.OnDismissListener onDismissListener){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, onDismissListener);
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