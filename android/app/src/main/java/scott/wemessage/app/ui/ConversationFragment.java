package scott.wemessage.app.ui;

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
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.MessageImageActivity;
import scott.wemessage.app.ui.activities.MessageVideoActivity;
import scott.wemessage.app.ui.view.chat.ChatTitleView;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.ui.view.messages.ActionMessageView;
import scott.wemessage.app.ui.view.messages.ActionMessageViewHolder;
import scott.wemessage.app.ui.view.messages.IncomingMessageViewHolder;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.utils.media.AudioAttachmentMediaPlayer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.DateUtils;

public class ConversationFragment extends Fragment implements MessageManager.Callbacks, AudioAttachmentMediaPlayer.AttachmentAudioCallbacks {

    private final String TAG = "ConversationFragment";
    private final Object chatLock = new Object();
    private final int MESSAGE_QUEUE_AMOUNT = 50;
    private final int ERROR_SNACKBAR_DURATION = 5;
    private final byte CONTENT_TYPE_ACTION = 22;

    private String callbackUuid;
    private Chat chat;
    private ChatTitleView chatTitleView;
    private MessagesList messageList;
    private MessagesListAdapter<IMessage> messageListAdapter;
    private AudioAttachmentMediaPlayer audioAttachmentMediaPlayer;
    private ConcurrentHashMap<String, Message> messageMapIntegrity = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ActionMessage> actionMessageMapIntegrity = new ConcurrentHashMap<>();
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();
    private boolean isBoundToConnectionService = false;

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
                    generateErroredSnackBar(getView(), getString(R.string.new_message_error)).show();
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.send_message_error)).show();
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR)) {
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.message_update_error)).show();
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (getView() != null) {
                    if (intent.getExtras() != null){
                        generateErroredSnackBar(getView(), intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE)).show();
                    }else {
                        generateErroredSnackBar(getView(), getString(R.string.action_perform_error_default)).show();
                    }
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                if (getView() != null) {
                    generateErroredSnackBar(getView(), getString(R.string.result_process_error)).show();
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_LOAD_ATTACHMENT_ERROR)){
                if (getView() != null){
                    generateErroredSnackBar(getView(), getString(R.string.load_attachment_error)).show();
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_PLAY_AUDIO_ATTACHMENT_ERROR)){
                if (getView() != null){
                    generateErroredSnackBar(getView(), getString(R.string.play_audio_attachment_error)).show();
                }
            }else if (intent.getAction().equals(weMessage.BROADCAST_IMAGE_FULLSCREEN_ACTIVITY_START)){
                launchFullScreenImageActivity(intent.getStringExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI));
            }else if (intent.getAction().equals(weMessage.BROADCAST_VIDEO_FULLSCREEN_ACTIVITY_START)){
                launchFullScreenVideoActivity(intent.getStringExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }
        MessageManager messageManager = weMessage.get().getMessageManager();
        MessageDatabase messageDatabase = weMessage.get().getMessageDatabase();

        if (savedInstanceState == null) {
            try {
                Intent startingIntent = getActivity().getIntent();
                Class startingClass = Class.forName(startingIntent.getStringExtra(weMessage.BUNDLE_RETURN_POINT));

                Chat chat = messageDatabase.getChatByUuid(startingIntent.getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT));

                if (chat == null) {
                    Intent returnIntent = new Intent(weMessage.get(), startingClass);
                    returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, getString(R.string.conversation_load_failure));

                    startActivity(returnIntent);
                    getActivity().finish();
                } else {
                    setChat(chat);
                }
            } catch (Exception ex) {
                AppLogger.error(TAG, "Could not load ConversationFragment because an error occurred", ex);
                goToChatList(getString(R.string.conversation_load_failure));
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
        broadcastIntentFilter.addAction(weMessage.BROADCAST_LOAD_ATTACHMENT_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_PLAY_AUDIO_ATTACHMENT_ERROR);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_IMAGE_FULLSCREEN_ACTIVITY_START);
        broadcastIntentFilter.addAction(weMessage.BROADCAST_VIDEO_FULLSCREEN_ACTIVITY_START);

        callbackUuid = UUID.randomUUID().toString();

        messageManager.hookCallbacks(callbackUuid, this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageListBroadcastReceiver, broadcastIntentFilter);

        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.conversationToolbar);
        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.conversationBackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatList(null);
            }
        });
        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        chatTitleView = (ChatTitleView) toolbar.findViewById(R.id.chatTitleView);
        messageList = (MessagesList) view.findViewById(R.id.messagesList);

        ImageLoader imageLoader;
        MessageManager messageManager = weMessage.get().getMessageManager();
        String meUuid = weMessage.get().getMessageDatabase().getContactByHandle(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount())).getUuid().toString();
        MessageHolders messageHolders = new MessageHolders()
                .setIncomingTextConfig(IncomingMessageViewHolder.class, R.layout.incoming_message)
                .registerContentType(CONTENT_TYPE_ACTION, ActionMessageViewHolder.class, R.layout.message_action, R.layout.message_action, new MessageHolders.ContentChecker() {
                    @Override
                    public boolean hasContentFor(IMessage message, byte type) {
                        switch (type){
                            case CONTENT_TYPE_ACTION:
                                return (message instanceof ActionMessageView);
                        }
                        return false;
                    }
                });

        if (chat instanceof PeerChat){
            imageLoader = null;
        } else {
            imageLoader = new ImageLoader() {
                @Override
                public void loadImage(ImageView imageView, String url) {
                    Glide.with(ConversationFragment.this).load(url).into(imageView);
                }
            };
        }

        MessagesListAdapter<IMessage> messageListAdapter = new MessagesListAdapter<>(meUuid, messageHolders, imageLoader);

        messageListAdapter.setDateHeadersFormatter(new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)){
                    return getString(R.string.today);
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

        messageListAdapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                weMessage.get().getMessageManager().queueMessages(chat, totalItemsCount, MESSAGE_QUEUE_AMOUNT, true);
            }
        });

        //TODO: If onMessageClick listener is being added, make sure the fragment is still attached

        messageList.setAdapter(messageListAdapter);
        this.messageListAdapter = messageListAdapter;

        chatTitleView.setChat(chat);
        messageManager.queueActionMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);
        messageManager.queueMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);
        messageManager.setHasUnreadMessages(chat, false, true);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, chat.getUuid().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        pauseAudio(getAudioAttachmentMediaPlayer().getAttachment());

        super.onPause();
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
        MessageManager messageManager = weMessage.get().getMessageManager();

        messageMapIntegrity.clear();
        messageListAdapter.clear();
        messageManager.unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageListBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        stopAudio();
        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) {

    }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {
        //TODO: Work here
    }

    @Override
    public void onContactListRefresh(List<Contact> contacts) {
        //TODO: Work here
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
                    chatTitleView.setChat(chat);
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
                    chatTitleView.setChat(chat);
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
                    chatTitleView.setChat(chat);
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
            goToChatList(getString(R.string.chat_delete_message_go_back));
        }
    }

    @Override
    public void onChatListRefresh(List<Chat> chats) {

    }

    @Override
    public void onMessageAdd(final Message message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(message.getChat())){
                    MessageView messageView = new MessageView(message);
                    messageListAdapter.addToStart(messageView, true);
                    messageMapIntegrity.put(message.getUuid().toString(), message);
                    weMessage.get().getMessageManager().setHasUnreadMessages(chat, false, true);
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
                    messageListAdapter.update(new MessageView(newData));
                    messageMapIntegrity.put(newData.getUuid().toString(), newData);
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
                    messageMapIntegrity.remove(message.getUuid().toString());
                }
            }
        });
    }

    @Override
    public void onMessagesQueueFinish(final List<Message> messages) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messages.size() > 0) {
                    List<IMessage> messageViews = new ArrayList<>();

                    for (Message message : messages) {
                        if (isChatThis(message.getChat())) {
                            if (!messageMapIntegrity.containsKey(message.getUuid().toString())) {
                                MessageView messageView = new MessageView(message);

                                messageViews.add(messageView);
                                messageMapIntegrity.put(message.getUuid().toString(), message);
                            }
                        }
                    }

                    if (messageViews.size() > 0) {
                        messageListAdapter.addToEnd(messageViews, false);
                    }

                    //TODO: TEMP CODE


                    MessageView me = new MessageView(new Message(UUID.randomUUID(), "Mac-" + UUID.randomUUID().toString(), chat,
                            weMessage.get().getMessageDatabase().getContactByHandle(weMessage.get().getMessageDatabase().getHandleByAccount(weMessage.get().getCurrentAccount())),
                            null, "This is a message from myself, as a test. hello. Lorem ispilum pi 214-868-7499", DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime()), null, null,
                            false, true, false, false, false, true));

                    messageListAdapter.addToStart(me, true);
                }
            }
        });
    }

    @Override
    public void onMessagesRefresh() {
        messageListAdapter.clear();
        messageMapIntegrity.clear();
        weMessage.get().getMessageManager().queueMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);
    }

    @Override
    public void onActionMessageAdd(final ActionMessage message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isChatThis(message.getChat())){
                    ActionMessageView messageView = new ActionMessageView(message);
                    messageListAdapter.addToStart(messageView, true);
                    actionMessageMapIntegrity.put(message.getUuid().toString(), message);
                }
            }
        });
    }

    @Override
    public void onActionMessagesQueueFinish(final List<ActionMessage> messages) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messages.size() > 0) {
                    List<IMessage> messageViews = new ArrayList<>();

                    for (ActionMessage message : messages) {
                        if (isChatThis(message.getChat())) {
                            if (!actionMessageMapIntegrity.containsKey(message.getUuid().toString())) {
                                ActionMessageView messageView = new ActionMessageView(message);

                                messageViews.add(messageView);
                                actionMessageMapIntegrity.put(message.getUuid().toString(), message);
                            }
                        }
                    }

                    if (messageViews.size() > 0) {
                        messageListAdapter.addToEnd(messageViews, false);
                    }
                }
            }
        });
    }

    @Override
    public void onActionMessagesRefresh() {
        actionMessageMapIntegrity.clear();
        weMessage.get().getMessageManager().queueActionMessages(getChat(), 0, MESSAGE_QUEUE_AMOUNT, true);
    }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) {
        //TODO: Stuff here
    }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {
        //TODO: Stuff here
    }

    //TODO: Outgoing
    @Override
    public void onPlaybackStart(Attachment a) {
        for (int childCount = messageList.getChildCount(), i = 0; i < childCount; ++i) {
            RecyclerView.ViewHolder holder = messageList.getChildViewHolder(messageList.getChildAt(i));

            if (holder instanceof IncomingMessageViewHolder){
                IncomingMessageViewHolder incomingHolder = (IncomingMessageViewHolder) holder;

                incomingHolder.notifyAudioPlaybackStart(a);
            }
        }
    }

    //TODO: Outgoing
    @Override
    public void onPlaybackStop(String attachmentUuid) {
        for (int childCount = messageList.getChildCount(), i = 0; i < childCount; ++i) {
            RecyclerView.ViewHolder holder = messageList.getChildViewHolder(messageList.getChildAt(i));

            if (holder instanceof IncomingMessageViewHolder){
                IncomingMessageViewHolder incomingHolder = (IncomingMessageViewHolder) holder;
                incomingHolder.notifyAudioPlaybackStop(attachmentUuid);
            }
            audioAttachmentMediaPlayer = null;
        }
    }

    public synchronized AudioAttachmentMediaPlayer getAudioAttachmentMediaPlayer(){
        if (audioAttachmentMediaPlayer == null){
            audioAttachmentMediaPlayer = new AudioAttachmentMediaPlayer();
            audioAttachmentMediaPlayer.setCallback(this);
        }
        return audioAttachmentMediaPlayer;
    }

    public synchronized boolean playAudio(Attachment a){
        try {
            if (getAudioAttachmentMediaPlayer().hasAudio()) {
                getAudioAttachmentMediaPlayer().stopAudioPlayback();
            }
            getAudioAttachmentMediaPlayer().setAttachment(a);
            getAudioAttachmentMediaPlayer().startAudioPlayback(AndroidIOUtils.getUriFromFile(a.getFileLocation().getFile()));
            return true;
        }catch(Exception ex){
            AppLogger.error("An error occurred while trying to play Audio Attachment: " + a.getUuid().toString(), ex);
            generateErroredSnackBar(getView(), getString(R.string.play_audio_attachment_error)).show();
            getAudioAttachmentMediaPlayer().forceRelease();
            return false;
        }
    }

    public synchronized boolean resumeAudio(Attachment a){
        if (getAudioAttachmentMediaPlayer().hasAudio() && getAudioAttachmentMediaPlayer().getAttachment().getUuid().toString().equals(a.getUuid().toString())){
            getAudioAttachmentMediaPlayer().resumeAudioPlayback();
            return true;
        }
        return false;
    }

    public synchronized boolean pauseAudio(Attachment a){
        if (getAudioAttachmentMediaPlayer().hasAudio() && getAudioAttachmentMediaPlayer().getAttachment().getUuid().toString().equals(a.getUuid().toString())){
            getAudioAttachmentMediaPlayer().pauseAudioPlayback();
            return true;
        }
        return false;
    }

    public synchronized boolean stopAudio(){
        if (getAudioAttachmentMediaPlayer().hasAudio()){
            getAudioAttachmentMediaPlayer().stopAudioPlayback();
            return true;
        }
        return false;
    }

    private Chat getChat(){
        synchronized (chatLock){
            return chat;
        }
    }

    private void setChat(Chat chat){
        synchronized (chatLock){
            this.chat = chat;
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
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void launchFullScreenImageActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageImageActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chat.getUuid().toString());

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void launchFullScreenVideoActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageVideoActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chat.getUuid().toString());

        startActivity(launcherIntent);
        getActivity().finish();
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
        final Snackbar snackbar = Snackbar.make(view, message, ERROR_SNACKBAR_DURATION * 1000);

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

    private void goToChatList(String reason){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

            if (reason != null) {
                returnIntent.putExtra(weMessage.BUNDLE_CONVERSATION_GO_BACK_REASON, reason);
            }

            startActivity(returnIntent);
            getActivity().finish();
        }
    }
}