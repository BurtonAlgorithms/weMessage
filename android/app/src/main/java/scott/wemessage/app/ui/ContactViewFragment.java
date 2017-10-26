package scott.wemessage.app.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.afollestad.materialcamera.MaterialCamera;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.connection.ConnectionService;
import scott.wemessage.app.connection.ConnectionServiceConnection;
import scott.wemessage.app.messages.MessageCallbacks;
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
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.connection.json.action.JSONAction;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.types.ReturnType;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.commons.utils.StringUtils;

public class ContactViewFragment extends MessagingFragment implements MessageCallbacks {

    private final int ERROR_SNACKBAR_DURATION = 5;
    private final int TYPE_HEADER = 0;
    private final int TYPE_ITEM = 1;
    private final int TYPE_MEDIA_ERROR = 2;

    private String BUNDLE_IS_IN_EDIT_MODE = "bundleIsInEditMode";
    private String BUNDLE_IS_CHOOSE_PHOTO_LAYOUT_SHOWN = "bundleIsChoosePhotoLayoutShown";
    private String BUNDLE_EDITED_FIRST_NAME = "bundleEditedFirstName";
    private String BUNDLE_EDITED_LAST_NAME = "bundleEditedLastName";
    private String BUNDLE_EDITED_CONTACT_PICTURE = "bundleEditedContactPicture";

    private boolean isBoundToConnectionService = false;
    private boolean isInEditMode = false;
    private boolean isChoosePhotoLayoutShown = false;

    private String previousChatId;
    private String contactUuid;
    private String callbackUuid;

    private String editedFirstName;
    private String editedLastName;
    private String editedContactPicture;

    private RecyclerView contactViewRecyclerView;
    private BottomSheetLayout bottomSheetLayout;
    private ContactViewRecyclerAdapter contactViewRecyclerAdapter;

    private RelativeLayout contactViewChoosePhotoLayout;
    private TextView contactViewChoosePhotoErrorTextView;
    private RecyclerView contactViewChoosePhotoRecyclerView;
    private ChoosePhotoAdapter choosePhotoAdapter;

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

            Contact c = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

            editedFirstName = c.getFirstName();
            editedLastName = c.getLastName();
        } else {
            contactUuid = savedInstanceState.getString(weMessage.BUNDLE_CONTACT_VIEW_UUID);
            previousChatId = savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT);

            isInEditMode = savedInstanceState.getBoolean(BUNDLE_IS_IN_EDIT_MODE);
            isChoosePhotoLayoutShown = savedInstanceState.getBoolean(BUNDLE_IS_CHOOSE_PHOTO_LAYOUT_SHOWN);
            editedFirstName = savedInstanceState.getString(BUNDLE_EDITED_FIRST_NAME);
            editedLastName = savedInstanceState.getString(BUNDLE_EDITED_LAST_NAME);
            editedContactPicture = savedInstanceState.getString(BUNDLE_EDITED_CONTACT_PICTURE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contact_view, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.contactViewToolbar);
        final ImageButton backButton = toolbar.findViewById(R.id.contactViewBackButton);
        final Button editButton = toolbar.findViewById(R.id.contactViewEditButton);
        final Button cancelButton = toolbar.findViewById(R.id.contactViewCancelButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInEditMode) {
                    returnToConversationScreen();
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickWaitListener(500L) {
            @Override
            public void onWaitClick(View view) {
                if (isInEditMode){
                    isInEditMode = false;

                    if (isChoosePhotoLayoutShown){
                        toggleChoosePhotoLayout(false);
                    }
                    cancelChanges();
                    contactViewRecyclerAdapter.toggleEditMode(false);

                    editButton.setText(R.string.word_edit);
                    backButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.GONE);
                }
            }
        });

        editButton.setOnClickListener(new OnClickWaitListener(500L) {
            @Override
            public void onWaitClick(View view) {
                if (!isInEditMode){
                    if (contactViewRecyclerAdapter != null){
                        isInEditMode = true;
                        contactViewRecyclerAdapter.toggleEditMode(true);

                        editButton.setText(R.string.word_done);
                        backButton.setVisibility(View.INVISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                    }
                }else {
                    if (contactViewRecyclerAdapter != null){
                        isInEditMode = false;
                        contactViewRecyclerAdapter.dispatchKeys();

                        try {
                            Contact oldVal = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

                            if (!editedFirstName.equals(oldVal.getFirstName())) {
                                oldVal.setFirstName(editedFirstName);
                            }
                            if (!editedLastName.equals(oldVal.getLastName())) {
                                oldVal.setLastName(editedLastName);
                            }

                            if (!StringUtils.isEmpty(editedContactPicture)) {
                                if (editedContactPicture.equals("DELETE")) {
                                    if (oldVal.getContactPictureFileLocation() != null && oldVal.getContactPictureFileLocation().getFile().exists()){
                                        oldVal.getContactPictureFileLocation().getFile().delete();
                                    }

                                    oldVal.setContactPictureFileLocation(null);
                                } else {
                                    File srcFile = new File(editedContactPicture);
                                    File newFile = new File(weMessage.get().getChatIconsFolder(), contactUuid + srcFile.getName());

                                    FileUtils.copy(srcFile, newFile);

                                    if (oldVal.getContactPictureFileLocation() != null && oldVal.getContactPictureFileLocation().getFile().exists()){
                                        oldVal.getContactPictureFileLocation().getFile().delete();
                                    }
                                    oldVal.setContactPictureFileLocation(new FileLocationContainer(newFile));
                                }
                            }

                            weMessage.get().getMessageManager().updateContact(contactUuid, oldVal, true);
                        }catch (Exception ex){
                            showErroredSnackBar(getString(R.string.contact_update_error));
                            AppLogger.error("An error occurred while updating a contact", ex);
                        }
                        contactViewRecyclerAdapter.toggleEditMode(false);

                        editButton.setText(R.string.word_edit);
                        backButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.GONE);

                        if (isChoosePhotoLayoutShown){
                            toggleChoosePhotoLayout(false);
                        }
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
        contactViewRecyclerView = view.findViewById(R.id.contactViewRecyclerView);
        bottomSheetLayout = view.findViewById(R.id.contactViewBottomSheetLayout);

        contactViewRecyclerView.setLayoutManager(layoutManager);
        contactViewRecyclerView.setAdapter(contactViewRecyclerAdapter);

        contactViewChoosePhotoLayout = view.findViewById(R.id.contactViewChoosePhotoLayout);
        contactViewChoosePhotoErrorTextView = view.findViewById(R.id.contactViewChoosePhotoErrorTextView);
        contactViewChoosePhotoRecyclerView = view.findViewById(R.id.contactViewChoosePhotoRecyclerView);

        ViewGroup.LayoutParams layoutParams = contactViewChoosePhotoLayout.getLayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams.height = displayMetrics.heightPixels / 2;

        contactViewChoosePhotoLayout.setLayoutParams(layoutParams);
        contactViewChoosePhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false));

        contactViewRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isChoosePhotoLayoutShown){
                    toggleChoosePhotoLayout(false);
                }
                return false;
            }
        });

        if (isInEditMode){
            editButton.setText(R.string.word_done);
            backButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }

        loadAttachmentItems();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == weMessage.REQUEST_CODE_CAMERA){
            if (resultCode == Activity.RESULT_OK){
                editedContactPicture = data.getData().getPath();

                if (contactViewRecyclerAdapter != null){
                    contactViewRecyclerAdapter.updatePicture(editedContactPicture);
                }

            }else if (data != null){
                AppLogger.error("An error occurred while trying to get Camera data.", (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA));
                showErroredSnackBar(getString(R.string.camera_capture_error));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case weMessage.REQUEST_PERMISSION_CAMERA:
                if (isGranted(grantResults)){
                    launchCamera();
                }
                break;
            case weMessage.REQUEST_PERMISSION_READ_STORAGE:
                if (isGranted(grantResults)){
                    contactViewChoosePhotoErrorTextView.setVisibility(View.GONE);
                    loadChoosePhotoItems();
                } else {
                    contactViewChoosePhotoRecyclerView.setVisibility(View.GONE);
                    contactViewChoosePhotoErrorTextView.setText(getString(R.string.no_media_permission));
                    contactViewChoosePhotoErrorTextView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(weMessage.BUNDLE_CONTACT_VIEW_UUID, contactUuid);
        outState.putString(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        outState.putBoolean(BUNDLE_IS_IN_EDIT_MODE, isInEditMode);
        outState.putBoolean(BUNDLE_IS_CHOOSE_PHOTO_LAYOUT_SHOWN, isChoosePhotoLayoutShown);
        outState.putString(BUNDLE_EDITED_FIRST_NAME, editedFirstName);
        outState.putString(BUNDLE_EDITED_LAST_NAME, editedLastName);
        outState.putString(BUNDLE_EDITED_CONTACT_PICTURE, editedContactPicture);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        if (!isServiceRunning(ConnectionService.class)){
            goToLauncherReconnect();
        }

        super.onResume();
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
    public void onContactUpdate(Contact oldData, final Contact newData) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (newData.getUuid().toString().equals(contactUuid)) {
                    if (contactViewRecyclerAdapter != null) {
                        contactViewRecyclerAdapter.updateContact(newData);
                    }
                }
            }
        });
    }

    @Override
    public void onContactListRefresh(final List<Contact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Contact c : contacts){
                    if (c.getUuid().toString().equals(contactUuid)){
                        if (contactViewRecyclerAdapter != null) {
                            contactViewRecyclerAdapter.updateContact(c);
                        }
                    }
                }
            }
        });
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

    public void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void loadAttachmentItems(){
        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> allUris = new ArrayList<>();

                try {
                    Chat handleChat = weMessage.get().getMessageDatabase().getChatByHandle(weMessage.get().getMessageDatabase().getContactByUuid(contactUuid).getHandle());
                    if (handleChat == null) return allUris;

                    String previousChatId = handleChat.getUuid().toString();

                    for (Attachment a : weMessage.get().getMessageDatabase().getReversedAttachmentsInChat(previousChatId, 0, Integer.MAX_VALUE)) {
                        String fileLoc = a.getFileLocation().getFileLocation();

                        if (!StringUtils.isEmpty(fileLoc) && !allUris.contains(fileLoc)) {
                            MimeType mimeType = AndroidUtils.getMimeTypeFromPath(fileLoc);

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

                onLoadAttachmentItems(strings);
            }
        }.execute();
    }



    private void onLoadAttachmentItems(final List<String> filePaths){
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

    private void showContactPictureEditSheet(){
        bottomSheetLayout.showWithSheetView(LayoutInflater.from(getContext()).inflate(R.layout.sheet_contact_view_edit_picture, bottomSheetLayout, false));

        bottomSheetLayout.findViewById(R.id.contactViewEditPictureTake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetLayout.dismissSheet();

                if (isInEditMode) {
                    launchCamera();
                }
            }
        });

        bottomSheetLayout.findViewById(R.id.contactViewEditPictureChoose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetLayout.dismissSheet();

                if (isInEditMode) {
                    toggleChoosePhotoLayout(true);
                }
            }
        });

        bottomSheetLayout.findViewById(R.id.contactViewEditPictureDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInEditMode) {
                    deleteContactPicture();
                }
                bottomSheetLayout.dismissSheet();
            }
        });

        bottomSheetLayout.findViewById(R.id.contactViewEditPictureCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetLayout.dismissSheet();
            }
        });
    }

    private void launchCamera(){
        if (hasPermission(Manifest.permission.CAMERA, getString(R.string.no_camera_permission), "CameraPermissionAlertFragment", weMessage.REQUEST_PERMISSION_CAMERA)) {
            new MaterialCamera(this)
                    .allowRetry(true)
                    .autoSubmit(false)
                    .saveDir(weMessage.get().getAttachmentFolder())
                    .showPortraitWarning(true)
                    .defaultToFrontFacing(false)
                    .retryExits(false)
                    .labelRetry(R.string.word_redo)
                    .labelConfirm(R.string.ok_button)
                    .stillShot()
                    .start(weMessage.REQUEST_CODE_CAMERA);
        }
    }

    private void toggleChoosePhotoLayout(boolean value){
        if (isChoosePhotoLayoutShown != value){
            if (value){
                isChoosePhotoLayoutShown = true;

                contactViewChoosePhotoLayout.animate().alpha(1.0f).translationY(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        contactViewChoosePhotoLayout.setVisibility(View.VISIBLE);
                    }
                });

                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.no_media_permission), "MediaReadPermissionAlertFragment", weMessage.REQUEST_PERMISSION_READ_STORAGE)){
                    loadChoosePhotoItems();
                }
            }else {
                isChoosePhotoLayoutShown = false;

                int height = contactViewChoosePhotoLayout.getHeight();

                contactViewChoosePhotoLayout.animate().alpha(0.f).translationY(height).setDuration(250).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        contactViewChoosePhotoLayout.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void deleteContactPicture() {
        editedContactPicture = "DELETE";
        contactViewRecyclerAdapter.updatePicture("DELETE");
    }

    private void loadChoosePhotoItems(){
        new AsyncTask<Void, Void, ArrayList<String>>(){

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> allUris = new ArrayList<>();

                try {
                    allUris.addAll(getAllImages());
                }catch (Exception ex){
                    showErroredSnackBar(getString(R.string.media_fetch_error));
                    AppLogger.error("An error occurred while fetching media from the device.", ex);
                }

                return allUris;
            }

            @Override
            protected void onPostExecute(ArrayList<String> strings) {
                if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                onLoadChoosePhotoItems(strings);

                if (choosePhotoAdapter.getItemCount() == 0){
                    contactViewChoosePhotoRecyclerView.setVisibility(View.GONE);
                    contactViewChoosePhotoErrorTextView.setText(getString(R.string.no_media_found));
                    contactViewChoosePhotoErrorTextView.setVisibility(View.VISIBLE);
                }else {
                    contactViewChoosePhotoRecyclerView.setVisibility(View.VISIBLE);
                    contactViewChoosePhotoErrorTextView.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private void onLoadChoosePhotoItems(final List<String> filePaths){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                choosePhotoAdapter = new ChoosePhotoAdapter(filePaths);
                contactViewChoosePhotoRecyclerView.setAdapter(choosePhotoAdapter);
            }
        });
    }

    private void cancelChanges(){
        Contact c = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

        editedFirstName = c.getFirstName();
        editedLastName = c.getLastName();
        editedContactPicture = null;
    }

    private void showErroredSnackBar(String message){
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

    private void goToLauncherReconnect(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent launcherIntent = new Intent(weMessage.get(), LaunchActivity.class);

            startActivity(launcherIntent);
            getActivity().finish();
        }
    }

    private void goToChatList(){
        if (isAdded() || (getActivity() != null && !getActivity().isFinishing())) {
            Intent returnIntent = new Intent(weMessage.get(), ChatListActivity.class);

            startActivity(returnIntent);
            getActivity().finish();
        }
    }

    private ArrayList<String> getAllImages(){
        ArrayList<String> images = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.MIME_TYPE };
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, orderBy);
        int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int mimeIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (MimeType.getTypeFromString(cursor.getString(mimeIndexData)) == MimeType.IMAGE) {
                    String imagePath = cursor.getString(columnIndexData);
                    images.add(imagePath);
                }
            }
        }
        cursor.close();

        return images;
    }

    private void showDisconnectReasonDialog(Intent bundledIntent, String defaultMessage, Runnable runnable){
        DialogDisplayer.showDisconnectReasonDialog(getContext(), getFragmentManager(), bundledIntent, defaultMessage, runnable);
    }

    private boolean hasPermission(final String permission, String rationaleString, String alertTagId, final int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)){
                DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), rationaleString);

                alertDialogFragment.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        requestPermissions(new String[] { permission }, requestCode);
                    }
                });
                alertDialogFragment.show(getFragmentManager(), alertTagId);
                return false;
            } else {
                requestPermissions(new String[] { permission }, requestCode);
                return false;
            }
        }
        return true;
    }

    private boolean isGranted(int[] grantResults){
        return (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
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
            }else if (holder instanceof GalleryMediaErrorHolder) {
                ((GalleryMediaErrorHolder) holder).bind(attachmentUris.isEmpty());
            }else if (holder instanceof ContactViewHeader) {
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

        public void updatePicture(String path){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).updatePicture(path);

                    notifyItemChanged(0);
                    contactViewRecyclerView.scrollBy(0, 0);
                }
            }catch (Exception ex){ }
        }

        public void updateContact(Contact contact){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).bind(contact);

                    notifyItemChanged(0);
                    contactViewRecyclerView.scrollBy(0, 0);
                }
            }catch (Exception ex){ }
        }

        public void toggleEditMode(boolean value){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(0));

                if (viewHolder instanceof ContactViewHeader) {
                    ((ContactViewHeader) viewHolder).toggleEditMode(value);

                    notifyItemChanged(0);
                    contactViewRecyclerView.scrollBy(0, 0);
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

            notifyDataSetChanged();
            contactViewRecyclerView.scrollBy(0, 0);
        }

        private String getItem(int position) {
            return attachmentUris.get(position - 2);
        }
    }

    private class ContactViewHeader extends RecyclerView.ViewHolder {

        private boolean isInit = false;

        private LinearLayout contactPictureContainer;
        private ImageView contactPicture;
        private TextView contactPictureEditTextView;
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
            String handleText;

            contactName.setText(contact.getUIDisplayName());

            if (AuthenticationUtils.isValidEmailFormat(handleID)) {
                contactHandleHeader.setText(getString(R.string.word_email));
                handleText = handleID;
            }else {
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                if (phoneNumberUtil.isPossibleNumber(handleID, Resources.getSystem().getConfiguration().locale.getCountry())){
                    contactHandleHeader.setText(getString(R.string.word_phone));

                    try {
                        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(handleID, Resources.getSystem().getConfiguration().locale.getCountry());
                        handleText = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                    }catch (Exception ex){
                        handleText = handleID;
                    }

                } else {
                    contactHandleHeader.setText(getString(R.string.word_mobile));
                    handleText = handleID;
                }
            }
            contactHandleTextView.setText(handleText);

            if (StringUtils.isEmpty(editedContactPicture)) {
                Glide.with(ContactViewFragment.this).load(IOUtils.getContactIconUri(contact, IOUtils.IconSize.LARGE)).into(contactPicture);
            }else if (editedContactPicture.equals("DELETE")) {
                Glide.with(ContactViewFragment.this).load(IOUtils.getDefaultContactUri(IOUtils.IconSize.LARGE)).into(contactPicture);
            }else {
                Glide.with(ContactViewFragment.this).load(editedContactPicture).into(contactPicture);
            }

            doNotDisturbSwitch.setChecked(contact.isDoNotDisturb());
            doNotDisturbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Contact c = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);
                    weMessage.get().getMessageManager().updateContact(c.getUuid().toString(), c.setDoNotDisturb(b), true);
                }
            });

            blockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Contact c = weMessage.get().getMessageDatabase().getContactByUuid(contactUuid);

                    weMessage.get().getMessageManager().updateContact(c.getUuid().toString(), c.setBlocked(true), true);
                    goToChatList();
                }
            });

            toggleEditMode(isInEditMode);
        }

        public void toggleEditMode(boolean value){
            if (value) {
                if (contactViewNameSwitcher.getNextView().getId() == R.id.contactViewEditLayout) {
                    contactViewNameSwitcher.showNext();
                }
                contactPictureEditTextView.setVisibility(View.VISIBLE);
                contactPictureContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isChoosePhotoLayoutShown) {
                            showContactPictureEditSheet();
                        }
                    }
                });
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
                contactPictureEditTextView.setVisibility(View.GONE);
                contactPictureContainer.setOnClickListener(null);
                contactPictureContainer.setClickable(false);
            }
        }

        public void updatePicture(String path){
            if (path.equals("DELETE")) {
                Glide.with(ContactViewFragment.this).load(IOUtils.getDefaultContactUri(IOUtils.IconSize.LARGE)).into(contactPicture);
            }else {
                Glide.with(ContactViewFragment.this).load(path).into(contactPicture);
            }
        }

        public void dispatchKeys(){
            contactViewEditFirstName.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
            contactViewEditLastName.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0));
        }

        private void init(){
            if (!isInit){
                isInit = true;

                contactPictureContainer = itemView.findViewById(R.id.contactViewPictureContainer);
                contactPicture = itemView.findViewById(R.id.contactViewPicture);
                contactPictureEditTextView = itemView.findViewById(R.id.contactViewEditPictureTextView);
                contactName = itemView.findViewById(R.id.contactViewName);
                contactHandleHeader = itemView.findViewById(R.id.contactViewHandleHeader);
                contactHandleTextView = itemView.findViewById(R.id.contactViewHandleTextView);
                contactViewNameSwitcher = itemView.findViewById(R.id.contactViewNameSwitcher);
                contactViewEditFirstName = itemView.findViewById(R.id.contactViewEditFirstName);
                contactViewEditLastName = itemView.findViewById(R.id.contactViewEditLastName);
                doNotDisturbSwitch = itemView.findViewById(R.id.contactViewDoNotDisturbSwitch);
                blockButton = itemView.findViewById(R.id.contactViewBlockButton);

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

        public void bind(boolean show){
            if (show) show();
            else hide();
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

            galleryViewLayout = itemView.findViewById(R.id.galleryViewLayout);
            galleryImageView = itemView.findViewById(R.id.galleryImageView);
            videoIndicatorView = itemView.findViewById(R.id.videoIndicatorView);

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

            MimeType mimeType = AndroidUtils.getMimeTypeFromPath(path);

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
            MimeType mimeType = AndroidUtils.getMimeTypeFromPath(path);

            if (mimeType == MimeType.IMAGE){
                launchFullScreenImageActivity(path);
            }else if (mimeType == MimeType.VIDEO){
                launchFullScreenVideoActivity(path);
            }
        }
    }

    private class ChoosePhotoAdapter extends RecyclerView.Adapter<ChoosePhotoHolder> {

        private List<String> filePaths = new ArrayList<>();

        public ChoosePhotoAdapter(List<String> filePaths){
            this.filePaths = filePaths;
        }

        @Override
        public ChoosePhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new ChoosePhotoHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ChoosePhotoHolder holder, int position) {
            String path = filePaths.get(position);
            int size = contactViewChoosePhotoRecyclerView.getWidth() / 2;

            holder.bind(path, size);
        }

        @Override
        public int getItemCount() {
            return filePaths.size();
        }
    }

    private class ChoosePhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String path;
        private RelativeLayout galleryViewLayout;
        private ImageView galleryImageView;

        public ChoosePhotoHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_full_gallery_view, parent, false));

            galleryViewLayout = itemView.findViewById(R.id.galleryViewLayout);
            galleryImageView = itemView.findViewById(R.id.galleryImageView);

            itemView.setOnClickListener(this);
        }

        public void bind(String path, int imageSize){
            this.path = path;

            ViewGroup.LayoutParams layoutParams = galleryViewLayout.getLayoutParams();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            layoutParams.width = imageSize;
            layoutParams.height = imageSize;

            galleryViewLayout.setLayoutParams(layoutParams);

            Glide.with(itemView.getContext()).load(path).transition(DrawableTransitionOptions.withCrossFade()).into(galleryImageView);
        }

        @Override
        public void onClick(View v) {
            if (contactViewRecyclerAdapter != null){
                editedContactPicture = path;
                contactViewRecyclerAdapter.updatePicture(editedContactPicture);
                toggleChoosePhotoLayout(false);
            }
        }
    }
}