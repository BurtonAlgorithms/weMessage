package scott.wemessage.app.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

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
import scott.wemessage.app.ui.activities.ChatListActivity;
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
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.StringUtils;

public class ContactViewFragment extends MessagingFragment implements MessageManager.Callbacks {

    private final int ERROR_SNACKBAR_DURATION = 5;
    private final int TYPE_HEADER = 0;
    private final int TYPE_ITEM = 1;
    private final int TYPE_MEDIA_ERROR = 2;

    private boolean isBoundToConnectionService = false;
    private String previousChatId;
    private String contactUuid;
    private String callbackUuid;

    private boolean isInEditMode = false;
    private String editedFirstName;
    private String editedLastName;

    private RecyclerView contactViewRecyclerView;
    private ContactViewRecyclerAdapter contactViewRecyclerAdapter;
    private ConnectionServiceConnection serviceConnection = new ConnectionServiceConnection();

    private BroadcastReceiver contactViewBroadcastReceiver = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(contactViewBroadcastReceiver, broadcastIntentFilter);

        if (savedInstanceState == null){
            Intent startingIntent = getActivity().getIntent();

            contactUuid = startingIntent.getStringExtra(weMessage.BUNDLE_CONTACT_VIEW_UUID);
            previousChatId = startingIntent.getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        } else {
            contactUuid = savedInstanceState.getString(weMessage.BUNDLE_CONTACT_VIEW_UUID);
            previousChatId = savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT);
        }

        Contact c = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

        editedFirstName = c.getFirstName();
        editedLastName = c.getLastName();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contact_view, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.contactViewToolbar);
        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.contactViewBackButton);
        final Button editButton = (Button) toolbar.findViewById(R.id.contactViewEditButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToConversationScreen();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInEditMode){
                    if (contactViewRecyclerAdapter != null){
                        isInEditMode = true;
                        contactViewRecyclerAdapter.toggleEditMode(true);
                        editButton.setText(getString(R.string.word_done));
                    }
                }else {
                    if (contactViewRecyclerAdapter != null){
                        isInEditMode = false;
                        contactViewRecyclerAdapter.dispatchKeys();

                        Contact oldVal = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

                        if (!editedFirstName.equals(oldVal.getFirstName())){
                            oldVal.setFirstName(editedFirstName);
                        }
                        if (!editedLastName.equals(oldVal.getLastName())){
                            oldVal.setLastName(editedLastName);
                        }

                        weMessage.get().getMessageManager().updateContact(contactUuid, oldVal, true);

                        contactViewRecyclerAdapter.toggleEditMode(false);
                        editButton.setText(getString(R.string.word_edit));
                    }
                }
            }
        });

        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(contactViewRecyclerAdapter.getItemViewType(position)){
                    case TYPE_HEADER:
                        return 2;
                    case TYPE_MEDIA_ERROR:
                        return 2;
                    default:
                        return 1;
                }
            }
        });

        contactViewRecyclerAdapter = new ContactViewRecyclerAdapter();
        contactViewRecyclerView = (RecyclerView) view.findViewById(R.id.contactViewRecyclerView);

        contactViewRecyclerView.setLayoutManager(layoutManager);
        contactViewRecyclerView.setAdapter(contactViewRecyclerAdapter);

        loadItems();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(weMessage.BUNDLE_CONTACT_VIEW_UUID, contactUuid);
        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        weMessage.get().getMessageManager().unhookCallbacks(callbackUuid);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(contactViewBroadcastReceiver);

        if (isBoundToConnectionService){
            unbindService();
        }
        
        super.onDestroy();
    }

    @Override
    public void onContactCreate(Contact contact) { }

    @Override
    public void onContactUpdate(Contact oldData, Contact newData) {
        if (newData.getUuid().toString().equals(contactUuid)) {
            if (contactViewRecyclerAdapter != null) {
                contactViewRecyclerAdapter.updateContact(newData);
            }
        }
    }

    @Override
    public void onContactListRefresh(List<Contact> contacts) {
        for (Contact c : contacts){
            if (c.getUuid().toString().equals(contactUuid)){
                if (contactViewRecyclerAdapter != null) {
                    contactViewRecyclerAdapter.updateContact(c);
                }
            }
        }
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
    public void onParticipantAdd(Chat chat, Contact contact) { }

    @Override
    public void onParticipantRemove(Chat chat, Contact contact) { }

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
    public void onMessagesRefresh() { }

    @Override
    public void onActionMessageAdd(ActionMessage message) { }

    @Override
    public void onMessageSendFailure(JSONMessage jsonMessage, ReturnType returnType) { }

    @Override
    public void onActionPerformFailure(JSONAction jsonAction, ReturnType returnType) { }

    public void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void loadItems(){
        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> allUris = new ArrayList<>();

                try {
                    for (Attachment a : weMessage.get().getMessageDatabase().getReversedAttachmentsInChat(previousChatId, 0, Integer.MAX_VALUE)) {
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

                onLoadGalleryItems(strings);
            }
        }.execute();
    }

    private void onLoadGalleryItems(final List<String> filePaths){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactViewRecyclerAdapter.addAttachments(filePaths);
            }
        });
    }

    private void launchFullScreenImageActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageImageActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_IMAGE_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void launchFullScreenVideoActivity(String imageUri){
        Intent launcherIntent = new Intent(weMessage.get(), MessageVideoActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_FULL_SCREEN_VIDEO_URI, imageUri);
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        getActivity().finish();
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

    private void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
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

    private void goToLauncher(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            launcherIntent.putExtra(weMessage.BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT, true);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private class ContactViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<String> attachmentUris = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new ContactViewHeader(LayoutInflater.from(getActivity()), parent);
            } else if (viewType == TYPE_MEDIA_ERROR){
                return new GalleryMediaErrorHolder(LayoutInflater.from(getActivity()), parent);
            } else {
                return new GalleryHolder(LayoutInflater.from(getActivity()), parent);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof GalleryHolder) {
                String attachmentUri = getItem(position);

                ((GalleryHolder) holder).bind(attachmentUri);
            } else if (holder instanceof ContactViewHeader) {
                ((ContactViewHeader) holder).bind(weMessage.get().getMessageDatabase().getContactByUuid(contactUuid));
            }
        }

        @Override
        public int getItemCount() {
            return attachmentUris.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            if (position == 1) return TYPE_MEDIA_ERROR;
            return TYPE_ITEM;
        }

        public void updateContact(Contact contact){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).bind(contact);

                    contactViewRecyclerView.scrollBy(0, 0);
                    notifyItemChanged(0);
                }
            }catch (Exception ex){ }
        }

        public void toggleEditMode(boolean value){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).toggleEditMode(value);

                    contactViewRecyclerView.scrollBy(0, 0);
                    notifyItemChanged(0);
                }
            }catch (Exception ex){ }
        }

        public void dispatchKeys(){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).dispatchKeys();
                }
            }catch (Exception ex){ }
        }

        public void addAttachments(List<String> uris){
            attachmentUris.addAll(uris);

            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(1));

                if (viewHolder instanceof GalleryMediaErrorHolder) {
                    if (attachmentUris.size() == 0) {
                        ((GalleryMediaErrorHolder) viewHolder).show();
                    }else {
                        ((GalleryMediaErrorHolder) viewHolder).hide();
                    }
                }
            }catch(Exception ex){}

            contactViewRecyclerView.scrollBy(0, 0);
            notifyDataSetChanged();
        }

        private String getItem(int position) {
            return attachmentUris.get(position - 2);
        }
    }

    private class ContactViewHeader extends RecyclerView.ViewHolder {

        private boolean isInit = false;

        private ImageView contactPicture;
        private TextView contactName;
        private TextView contactHandleHeader;
        private TextView contactHandleTextView;
        private ViewSwitcher contactViewNameSwitcher;
        private EditText contactViewEditFirstName;
        private EditText contactViewEditLastName;
        private Switch doNotDisturbSwitch;
        private Button blockButton;

        public ContactViewHeader(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_contact_view_header, parent, false));
        }

        public void bind(Contact contact){
            init();

            String handleID = contact.getHandle().getHandleID();

            contactName.setText(contact.getUIDisplayName());

            if (AuthenticationUtils.isValidEmailFormat(handleID)) {
                contactHandleHeader.setText(getString(R.string.word_email));
            }else {
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                if (phoneNumberUtil.isPossibleNumber(handleID, Resources.getSystem().getConfiguration().locale.getCountry())){
                    contactHandleHeader.setText(getString(R.string.word_phone));
                } else {
                    contactHandleHeader.setText(getString(R.string.word_mobile));
                }
            }
            contactHandleTextView.setText(handleID);
            Glide.with(ContactViewFragment.this).load(AndroidIOUtils.getContactIconUri(contact)).into(contactPicture);
            toggleEditMode(isInEditMode);
        }

        public void toggleEditMode(boolean value){
            if (value) {
                if (contactViewNameSwitcher.getNextView().getId() == R.id.contactViewEditLayout) {
                    contactViewNameSwitcher.showNext();
                }
            }

            if (!value) {
                if (contactViewNameSwitcher.getNextView().getId() == R.id.contactViewName) {
                    contactViewNameSwitcher.showNext();
                }

                closeKeyboard();
                clearEditText(contactViewEditFirstName, false);
                clearEditText(contactViewEditLastName, false);

                contactViewEditFirstName.setText(editedFirstName);
                contactViewEditLastName.setText(editedLastName);
            }
        }

        public void dispatchKeys(){
            contactViewEditFirstName.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
            contactViewEditLastName.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
        }

        private void init(){
            if (!isInit){
                isInit = true;

                contactPicture = (ImageView) itemView.findViewById(R.id.contactViewPicture);
                contactName = (TextView) itemView.findViewById(R.id.contactViewName);
                contactHandleHeader = (TextView) itemView.findViewById(R.id.contactViewHandleHeader);
                contactHandleTextView = (TextView) itemView.findViewById(R.id.contactViewHandleTextView);
                contactViewNameSwitcher = (ViewSwitcher) itemView.findViewById(R.id.contactViewNameSwitcher);
                contactViewEditFirstName = (EditText) itemView.findViewById(R.id.contactViewEditFirstName);
                contactViewEditLastName = (EditText) itemView.findViewById(R.id.contactViewEditLastName);
                doNotDisturbSwitch = (Switch) itemView.findViewById(R.id.contactViewDoNotDisturbSwitch);
                blockButton = (Button) itemView.findViewById(R.id.contactViewBlockButton);

                contactViewEditFirstName.setText(editedFirstName);
                contactViewEditLastName.setText(editedLastName);

                contactViewEditFirstName.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            editedFirstName = contactViewEditFirstName.getText().toString();
                            clearEditText(contactViewEditFirstName, true);
                            return true;
                        }
                        return false;
                    }
                });

                contactViewEditLastName.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            editedLastName = contactViewEditLastName.getText().toString();
                            clearEditText(contactViewEditFirstName, true);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    private class GalleryMediaErrorHolder extends RecyclerView.ViewHolder {

        public GalleryMediaErrorHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.list_item_no_media_text_view, parent, false));
        }

        public void show(){
            itemView.findViewById(R.id.mediaErrorTextView).setVisibility(View.VISIBLE);
        }

        public void hide(){
            itemView.findViewById(R.id.mediaErrorTextView).setVisibility(View.GONE);
        }
    }

    private class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String path;
        private RelativeLayout galleryViewLayout;
        private ImageView galleryImageView;
        private ImageView videoIndicatorView;

        public GalleryHolder(LayoutInflater inflater, ViewGroup parent) {
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