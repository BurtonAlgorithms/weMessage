package scott.wemessage.app.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.ui.activities.ChatListActivity;
import scott.wemessage.app.ui.activities.ConversationActivity;
import scott.wemessage.app.ui.activities.MessageImageActivity;
import scott.wemessage.app.ui.activities.MessageVideoActivity;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.AuthenticationUtils;

public class ContactViewFragment extends MessagingFragment {

    private final int ERROR_SNACKBAR_DURATION = 5;
    private boolean hasPermission = false;

    private String previousChatId;
    private String contactUuid;

    private RecyclerView contactViewRecyclerView;
    private ContactViewRecyclerAdapter contactViewRecyclerAdapter;

    //TODO: Fix media error and rest of code

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            Intent startingIntent = getActivity().getIntent();

            contactUuid = startingIntent.getStringExtra(weMessage.BUNDLE_CONTACT_VIEW_UUID);
            previousChatId = startingIntent.getStringExtra(weMessage.BUNDLE_CONVERSATION_CHAT);
        } else {
            contactUuid = savedInstanceState.getString(weMessage.BUNDLE_CONTACT_VIEW_UUID);
            previousChatId = savedInstanceState.getString(weMessage.BUNDLE_CONVERSATION_CHAT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_contact_view, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.contactViewToolbar);
        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.contactViewBackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToConversationScreen();
            }
        });
        toolbar.setTitle(null);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        contactViewRecyclerAdapter = new ContactViewRecyclerAdapter();
        contactViewRecyclerView = (RecyclerView) view.findViewById(R.id.contactViewRecyclerView);

        contactViewRecyclerView.setLayoutManager(layoutManager);
        contactViewRecyclerView.setAdapter(contactViewRecyclerAdapter);

        hasPermission = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.no_media_permission), "MediaReadPermissionAlertFragment", weMessage.REQUEST_PERMISSION_READ_STORAGE);
        contactViewRecyclerAdapter.setHasPermission(hasPermission);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case weMessage.REQUEST_PERMISSION_READ_STORAGE:
                if (isGranted(grantResults)){
                    hasPermission = true;
                    contactViewRecyclerAdapter.setHasPermission(true);
                    loadItems();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        //TODO: Do something here

        super.onDestroy();
    }

    public void returnToConversationScreen() {
        Intent launcherIntent = new Intent(weMessage.get(), ConversationActivity.class);

        launcherIntent.putExtra(weMessage.BUNDLE_RETURN_POINT, ChatListActivity.class.getName());
        launcherIntent.putExtra(weMessage.BUNDLE_CONVERSATION_CHAT, previousChatId);

        startActivity(launcherIntent);
        getActivity().finish();
    }

    private void loadItems(){
        if (hasPermission) {
            new AsyncTask<Void, Void, ArrayList<String>>() {

                @Override
                protected ArrayList<String> doInBackground(Void... params) {
                    ArrayList<String> allUris = new ArrayList<>();

                    try {
                        for (Attachment a : weMessage.get().getMessageDatabase().getAttachmentsInChat(previousChatId, 0, Integer.MAX_VALUE)) {
                            allUris.add(a.getFileLocation().getFileLocation());
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

        private int TYPE_HEADER = 0;
        private int TYPE_ITEM = 1;
        private int TYPE_MEDIA_ERROR = 2;

        private ArrayList<String> attachmentUris = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new ContactViewHeader(LayoutInflater.from(getActivity()), parent);
            } else if (viewType == TYPE_MEDIA_ERROR){
                return new GalleryMediaErrorHolder(LayoutInflater.from(getActivity()), parent, (attachmentUris.size() == 0), hasPermission);
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
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);

                ((ContactViewHeader) holder).bind(weMessage.get().getMessageDatabase().getContactByUuid(contactUuid));
            }else if (holder instanceof GalleryMediaErrorHolder){
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);

                ((GalleryMediaErrorHolder) holder).bind();
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

        public void setHasPermission(boolean hasPermission){
            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(1));

                if (viewHolder instanceof GalleryMediaErrorHolder) {
                    ((GalleryMediaErrorHolder) viewHolder).updateViewPermission(hasPermission);
                    notifyItemChanged(1);
                }
            }catch(Exception ex){ }
        }

        public void addAttachments(List<String> uris){
            attachmentUris.addAll(uris);

            try {
                RecyclerView.ViewHolder viewHolder = contactViewRecyclerView.getChildViewHolder(contactViewRecyclerView.getChildAt(1));

                if (viewHolder instanceof GalleryMediaErrorHolder) {
                    ((GalleryMediaErrorHolder) viewHolder).updateViewVisibility(uris.size() == 0);
                    notifyItemChanged(1);
                }
            }catch(Exception ex){}

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
        private Switch doNotDisturbSwitch;
        private Button blockButton;

        ContactViewHeader(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_contact_view_header, parent, false));
        }

        void bind(Contact contact){
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
        }

        private void init(){
            if (!isInit){
                isInit = true;

                contactPicture = (ImageView) itemView.findViewById(R.id.contactViewPicture);
                contactName = (TextView) itemView.findViewById(R.id.contactViewName);
                contactHandleHeader = (TextView) itemView.findViewById(R.id.contactViewHandleHeader);
                contactHandleTextView = (TextView) itemView.findViewById(R.id.contactViewHandleTextView);
                doNotDisturbSwitch = (Switch) itemView.findViewById(R.id.contactViewDoNotDisturbSwitch);
                blockButton = (Button) itemView.findViewById(R.id.contactViewBlockButton);
            }
        }
    }

    private class GalleryMediaErrorHolder extends RecyclerView.ViewHolder {

        private TextView mediaErrorMessage;
        private boolean isShown;
        private boolean hasPermission;
        private boolean isInit = false;

        public GalleryMediaErrorHolder(LayoutInflater inflater, ViewGroup parent, boolean isShown, boolean hasPermission){
            super(inflater.inflate(R.layout.list_item_no_media_text_view, parent, false));

            this.hasPermission = hasPermission;
            this.isShown = isShown;
        }

        public void bind(){
            init();

            if (hasPermission){
                mediaErrorMessage.setText(getString(R.string.no_media_found));
            }else {
                mediaErrorMessage.setText(getString(R.string.no_media_permission));
            }

            if (!isShown){
                itemView.setVisibility(View.GONE);
            }else {
                itemView.setVisibility(View.VISIBLE);
            }
        }

        public void updateViewPermission(boolean hasPermission){
            if (hasPermission){
                mediaErrorMessage.setText(getString(R.string.no_media_found));
            }else {
                mediaErrorMessage.setText(getString(R.string.no_media_permission));
            }
        }

        public void updateViewVisibility(boolean isShown){
            if (!isShown){
                itemView.setVisibility(View.GONE);
                mediaErrorMessage.setVisibility(View.GONE);
            }else {
                mediaErrorMessage.setVisibility(View.GONE);
                itemView.setVisibility(View.VISIBLE);
            }
        }

        private void init(){
            if (!isInit){
                isInit = true;

                mediaErrorMessage = (TextView) itemView.findViewById(R.id.mediaErrorView);
            }
        }
    }

    private class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String path;
        private ImageView galleryImageView;
        private ImageView videoIndicatorView;
        private TextView galleryFileName;

        GalleryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_gallery_view, parent, false));

            galleryImageView = (ImageView) itemView.findViewById(R.id.galleryImageView);
            videoIndicatorView = (ImageView) itemView.findViewById(R.id.videoIndicatorView);
            galleryFileName = (TextView) itemView.findViewById(R.id.galleryFileName);

            itemView.setOnClickListener(this);
        }

        public void bind(String path){
            this.path = path;

            videoIndicatorView.setVisibility(View.INVISIBLE);
            galleryFileName.setVisibility(View.INVISIBLE);

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