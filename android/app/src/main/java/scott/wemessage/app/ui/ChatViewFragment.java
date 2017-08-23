package scott.wemessage.app.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.ActionMessage;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.Message;
import scott.wemessage.app.messages.objects.MessageBase;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.ContactViewActivity;
import scott.wemessage.app.ui.activities.ConversationActivity;
import scott.wemessage.app.ui.activities.LaunchActivity;
import scott.wemessage.app.ui.activities.MessageImageActivity;
import scott.wemessage.app.ui.activities.MessageVideoActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.json.action.JSONAction;
import scott.wemessage.commons.json.message.JSONMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.StringUtils;

public class ChatViewFragment extends MessagingFragment implements MessageManager.Callbacks {

    private final int ERROR_SNACKBAR_DURATION = 5;
    private final int TYPE_HEADER = 0;
    private final int TYPE_CONTACT = 1;
    private final int TYPE_CONTACT_ATTACHMENT = 2;
    private final int TYPE_ATTACHMENT = 3;

    private String BUNDLE_IS_IN_EDIT_MODE = "bundleIsInEditMode";

    private boolean isBoundToConnectionService = false;
    private boolean isInEditMode = false;

    private String chatUuid;
    private String callbackUuid;

    private RecyclerView chatViewRecyclerView;
    private ChatViewAdapter chatViewAdapter;

    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver chatViewBroadcastReceiver = new BroadcastReceiver() {
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
                showErroredSnackBar(getString(R.string.new_message_error));
            }else if(intent.getAction().equals(weMessage.BROADCAST_SEND_MESSAGE_ERROR)){
                showErroredSnackBar(getString(R.string.send_message_error));
            }else if(intent.getAction().equals(weMessage.BROADCAST_MESSAGE_UPDATE_ERROR)) {
                showErroredSnackBar(getString(R.string.message_update_error));
            }else if(intent.getAction().equals(weMessage.BROADCAST_ACTION_PERFORM_ERROR)){
                if (intent.getExtras() != null){
                    showErroredSnackBar(intent.getStringExtra(weMessage.BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE));
                }else {
                    showErroredSnackBar(getString(R.string.action_perform_error_default));
                }
            }else if(intent.getAction().equals(weMessage.BROADCAST_RESULT_PROCESS_ERROR)){
                showErroredSnackBar(getString(R.string.result_process_error));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isServiceRunning(ConnectionService.class)){
            bindService();
        }

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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(chatViewBroadcastReceiver, broadcastIntentFilter);

        if (savedInstanceState == null) {
            Intent startingIntent = getActivity().getIntent();

            chatUuid = startingIntent.getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        }else {
            isInEditMode = savedInstanceState.getBoolean(BUNDLE_IS_IN_EDIT_MODE, isInEditMode);
            chatUuid = savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_view, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.chatViewToolbar);
        final ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.chatViewBackButton);
        final Button editButton = (Button) toolbar.findViewById(R.id.chatViewEditButton);
        final Button cancelButton = (Button) toolbar.findViewById(R.id.chatViewCancelButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInEditMode) {
                    returnToConversationScreen();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInEditMode){

                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInEditMode){

                }else {

                }
            }
        });

        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        chatViewRecyclerView = view.findViewById(R.id.chatViewRecyclerView);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(chatViewAdapter.getItemViewType(position)){
                    case TYPE_HEADER:
                        return 2;
                    case TYPE_CONTACT:
                        return 2;
                    case TYPE_CONTACT_ATTACHMENT:
                        return 2;
                    default:
                        return 1;
                }
            }
        });

        chatViewAdapter = new ChatViewAdapter();

        chatViewRecyclerView.setLayoutManager(layoutManager);
        chatViewRecyclerView.setAdapter(chatViewAdapter);

        chatViewAdapter.loadChat((GroupChat) weMessage.get().getMessageDatabase().getChatByUuid(chatUuid));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_IS_IN_EDIT_MODE, isInEditMode);
        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(chatViewBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) { }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {
        //TODO: Reload group chat
    }

    @Override
    public void onContactListRefresh(List<Contact> contacts) {
        //TODO: Reload group chat
    }

    @Override
    public void onChatAdd(Chat chat) { }

    @Override
    public void onChatUpdate(Chat oldData, Chat newData) {
        //TODO: Reload group chat
    }

    @Override
    public void onUnreadMessagesUpdate(Chat chat, boolean hasUnreadMessages) { }

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
    public void onChatListRefresh(List<Chat> chats) {

    }

    @Override
    public void onMessageAdd(Message message) { }

    @Override
    public void onMessageUpdate(Message oldData, Message newData) { }

    @Override
    public void onMessageDelete(Message message) { }

    @Override
    public void onMessagesQueueFinish(List<MessageBase> messages) { }

    @Override
    public void onMessagesRefresh() { }

    @Override
    public void onActionMessageAdd(ActionMessage message) { }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) { }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) {
        showActionFailureSnackbar(jsonAction, returnType);
    }

    public void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void launchContactView(String contactUuid){
        Intent launcherIntent = new Intent(weMessage.get(), ContactViewActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_CONTACT_VIEW_UUID, contactUuid);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void launchFullScreenImageActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageImageActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void launchFullScreenVideoActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageVideoActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, chatUuid);

        startActivity(launcherIntent);
        getActivity().finish();
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

    private void showErroredSnackBar(String message){
        if (getParentFragment().getView() != null) {
            final Snackbar snackbar = Snackbar.make(getParentFragment().getView(), message, ERROR_SNACKBAR_DURATION * 1000);

            snackbar.setAction(getString(R.string.dismiss_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.lightRed));

            snackbar.show();
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private class ChatViewAdapter extends RecyclerView.Adapter {

        public Integer showingDeletePosition;
        private GroupChat groupChat;

        private ArrayList<Contact> contacts = new ArrayList<>();
        private ArrayList<String> attachmentUris = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType){
                case TYPE_HEADER:
                    return new ChatViewHeaderHolder(LayoutInflater.from(getActivity()), parent);
                case TYPE_CONTACT:
                    return new ContactViewHolder(LayoutInflater.from(getActivity()), parent);
                case TYPE_CONTACT_ATTACHMENT:
                    return new ContactAttachmentViewHolder(LayoutInflater.from(getActivity()), parent);
                case TYPE_ATTACHMENT:
                    return new AttachmentHolder(LayoutInflater.from(getActivity()), parent);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()){

                case TYPE_HEADER:
                    ((ChatViewHeaderHolder) holder).bind(groupChat);
                    break;
                case TYPE_CONTACT:
                    ((ContactViewHolder) holder).bind(contacts.get(position - 1));
                    break;
                case TYPE_CONTACT_ATTACHMENT:
                    ((ContactAttachmentViewHolder) holder).bind();
                    break;
                case TYPE_ATTACHMENT:
                    ((AttachmentHolder) holder).bind(attachmentUris.get(position - contacts.size() - 2));
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            if (position == contacts.size() + 1) return TYPE_CONTACT_ATTACHMENT;

            boolean contactRange = (position > 0 && position <= contacts.size());

            if (contactRange) return TYPE_CONTACT;

            return TYPE_ATTACHMENT;
        }

        @Override
        public int getItemCount() {
            return contacts.size() + attachmentUris.size() + 2;
        }

        public void loadChat(GroupChat groupChat){
            this.groupChat = groupChat;
            contacts.clear();
            attachmentUris.clear();

            showingDeletePosition = null;

            for (Contact c : groupChat.getParticipants()){
                contacts.add(c);
            }

            loadAttachmentItems();
        }

        private void loadAttachmentItems(){
            new AsyncTask<Void, Void, ArrayList<String>>() {

                @Override
                protected ArrayList<String> doInBackground(Void... params) {
                    ArrayList<String> allUris = new ArrayList<>();

                    try {
                        for (Attachment a : weMessage.get().getMessageDatabase().getReversedAttachmentsInChat(groupChat.getUuid().toString(), 0, Integer.MAX_VALUE)) {
                            String fileLoc = a.getFileLocation().getFileLocation();

                            if (!StringUtils.isEmpty(fileLoc) && !allUris.contains(fileLoc)) {
                                MimeType mimeType = MimeType.getTypeFromString(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileLoc)));

                                if (mimeType == MimeType.IMAGE || mimeType == MimeType.VIDEO) {
                                    allUris.add(fileLoc);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        showErroredSnackBar(getString(R.string.media_fetch_error));
                        AppLogger.error("An error occurred while fetching media from the device.", ex);
                    }

                    return allUris;
                }

                @Override
                protected void onPostExecute(ArrayList<String> strings) {
                    if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                    attachmentUris = strings;
                    notifyDataSetChanged();
                    chatViewRecyclerView.scrollBy(0, 0);
                }
            }.execute();
        }
    }

    private class ChatViewHeaderHolder extends RecyclerView.ViewHolder {

        private boolean isInit = false;

        private LinearLayout chatViewPictureContainer;
        private ImageView chatViewPicture;
        private TextView chatViewEditPictureTextView;
        private ViewSwitcher chatViewNameSwitcher;
        private TextView chatViewName;
        private EditText chatViewEditName;
        private Switch chatDoNotDisturbSwitch;
        private Button chatLeaveButton;
        private TextView chatViewContactsTextView;

        public ChatViewHeaderHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_chat_view_header, parent, false));
        }

        public void bind(GroupChat chat){
            init();

            Glide.with(ChatViewFragment.this).load(AndroidIOUtils.getChatIconUri(chat)).into(chatViewPicture);
            chatViewName.setText(chat.getUIDisplayName(false));
            chatViewContactsTextView.setText(getString(R.string.participants, chat.getParticipants().size()));
        }

        private void init(){
            if (!isInit){
                isInit = true;

                chatViewPictureContainer = (LinearLayout) itemView.findViewById(R.id.chatViewPictureContainer);
                chatViewPicture = (ImageView) itemView.findViewById(R.id.chatViewPicture);
                chatViewEditPictureTextView = (TextView) itemView.findViewById(R.id.chatViewEditPictureTextView);
                chatViewNameSwitcher = (ViewSwitcher) itemView.findViewById(R.id.chatViewNameSwitcher);
                chatViewName = (TextView) itemView.findViewById(R.id.chatViewName);
                chatViewEditName = (EditText) itemView.findViewById(R.id.chatViewEditName);
                chatDoNotDisturbSwitch = (Switch) itemView.findViewById(R.id.chatViewDoNotDisturbSwitch);
                chatLeaveButton = (Button) itemView.findViewById(R.id.chatViewLeaveButton);
                chatViewContactsTextView = (TextView) itemView.findViewById(R.id.chatViewContactsTextView);
            }
        }
    }

    private class ContactViewHolder extends RecyclerView.ViewHolder {

        private boolean isDeleteButtonShowing = false;
        private boolean isInit = false;

        private SwipeLayout swipeLayout;
        private LinearLayout chatContactRemoveButtonLayout;
        private ImageView chatContactPictureView;
        private TextView chatContactDisplayNameView;

        public ContactViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_chat_view_contact, parent, false));
        }

        public void bind(Contact contact){
            init();

            final String contactUuid = contact.getUuid().toString();

            itemView.findViewById(R.id.chatContactLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchContactView(contactUuid);
                }
            });

            chatContactRemoveButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: if participants > 2
                    //TODO: Remove participant from chat, callback
                    Toast.makeText(getContext(), "We will delete soon", Toast.LENGTH_SHORT).show();
                }
            });

            chatContactDisplayNameView.setText(contact.getUIDisplayName());
            Glide.with(ChatViewFragment.this).load(AndroidIOUtils.getContactIconUri(contact)).into(chatContactPictureView);

            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.chatContactRemoveButtonLayout));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    isDeleteButtonShowing = true;
                    chatViewAdapter.showingDeletePosition = getAdapterPosition();
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {
                    isDeleteButtonShowing = false;
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });

        }

        public void closeUnderlyingView(){
            if (isDeleteButtonShowing) {
                isDeleteButtonShowing = false;
                swipeLayout.close();
            }
            chatViewAdapter.showingDeletePosition = null;
        }

        private void init(){
            if (!isInit) {
                isInit = true;

                swipeLayout = (SwipeLayout) itemView;
                chatContactRemoveButtonLayout = (LinearLayout) itemView.findViewById(R.id.chatContactRemoveButtonLayout);
                chatContactPictureView = (ImageView) itemView.findViewById(R.id.chatContactPictureView);
                chatContactDisplayNameView = (TextView) itemView.findViewById(R.id.chatContactDisplayNameView);
            }
        }
    }

    private class ContactAttachmentViewHolder extends RecyclerView.ViewHolder {

        public ContactAttachmentViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_chat_view_contact_attachments, parent, false));

            itemView.findViewById(R.id.chatViewAddParticipant).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: Add participant fragment do mroe stuff.

                    Toast.makeText(getContext(), "Add participant coming soon", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void bind(){

        }
    }

    private class AttachmentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String path;
        private RelativeLayout galleryViewLayout;
        private ImageView galleryImageView;
        private ImageView videoIndicatorView;

        public AttachmentHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_full_gallery_view, parent, false));

            galleryViewLayout = (RelativeLayout) itemView.findViewById(R.id.galleryViewLayout);
            galleryImageView = (ImageView) itemView.findViewById(R.id.galleryImageView);
            videoIndicatorView = (ImageView) itemView.findViewById(R.id.videoIndicatorView);

            itemView.setOnClickListener(this);
        }

        public void bind(String path){
            this.path = path;

            ViewGroup.LayoutParams layoutParams = galleryViewLayout.getLayoutParams();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            layoutParams.width = displayMetrics.widthPixels / 2;
            layoutParams.height = displayMetrics.widthPixels / 2;

            galleryViewLayout.setLayoutParams(layoutParams);
            videoIndicatorView.setVisibility(View.INVISIBLE);

            MimeType mimeType = MimeType.getTypeFromString(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path)));

            if (mimeType == MimeType.IMAGE) {
                Glide.with(itemView.getContext()).load(path).transition(DrawableTransitionOptions.withCrossFade()).into(galleryImageView);
            }else if (mimeType == MimeType.VIDEO){
                itemView.setAlpha(0.0f);

                new AsyncTask<String, Void, Bitmap>(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                        retriever.setDataSource(params[0]);
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(params[0], MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                        retriever.release();

                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                        galleryImageView.setImageBitmap(bitmap);
                        videoIndicatorView.setVisibility(View.VISIBLE);
                        itemView.animate().alpha(1.0f).setDuration(250);
                    }
                }.execute(path);
            }
        }

        @Override
        public void onClick(View v) {
            MimeType mimeType = MimeType.getTypeFromString(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path)));

            if (mimeType == MimeType.IMAGE){
                launchFullScreenImageActivity(path);
            }else if (mimeType == MimeType.VIDEO){
                launchFullScreenVideoActivity(path);
            }
        }
    }
}