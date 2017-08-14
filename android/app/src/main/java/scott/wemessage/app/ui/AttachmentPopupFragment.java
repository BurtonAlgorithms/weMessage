package scott.wemessage.app.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.ui.view.dialog.DialogDisplayer;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;

public class AttachmentPopupFragment extends Fragment {

    private final int ERROR_SNACKBAR_DURATION = 5;
    private final int REQUEST_PERMISSION_READ_STORAGE = 22;

    private List<String> attachments = new ArrayList<>();
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private TextView mediaErrorView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null){
            attachments.addAll(getArguments().getStringArrayList(weMessage.ARG_ATTACHMENT_GALLERY_CACHE));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_popup_attachment, container, false);

        mediaErrorView = (TextView) view.findViewById(R.id.mediaErrorView);
        galleryRecyclerView = (RecyclerView) view.findViewById(R.id.galleryRecyclerView);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));

        mediaErrorView.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                DialogDisplayer.AlertDialogFragment alertDialogFragment = DialogDisplayer.generateAlertDialog(getString(R.string.permissions_error_title), getString(R.string.no_media_permission));

                alertDialogFragment.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION_READ_STORAGE);
                    }
                });
                alertDialogFragment.show(getFragmentManager(), "MediaReadPermissionsAlertFragment");
            } else {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION_READ_STORAGE);
            }
        }else {
            loadGalleryItems();
        }
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mediaErrorView.setVisibility(View.GONE);
                    loadGalleryItems();
                }else {
                    galleryRecyclerView.setVisibility(View.GONE);
                    mediaErrorView.setText(getString(R.string.no_media_permission));
                    mediaErrorView.setVisibility(View.VISIBLE);
                }
        }
    }

    @Override
    public void onDestroy() {
        ((AttachmentInputListener) getParentFragment()).setAttachmentsInput(attachments);

        super.onDestroy();
    }

    public List<String> getSelectedAttachments(){
        return attachments;
    }

    public void clearSelectedAttachments(){
        attachments.clear();
    }

    private void loadGalleryItems(){
        new AsyncTask<Void, Void, ArrayList<String>>(){

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> allUris = new ArrayList<>();

                try {
                    allUris.addAll(getAllImages());
                    allUris.addAll(getAllAudio());
                    allUris.addAll(getAllVideo());
                }catch (Exception ex){
                    generateErroredSnackBar(getParentFragment().getView(), getString(R.string.media_fetch_error)).show();
                    AppLogger.error("An error occurred while fetching media from the device.", ex);
                }

                return allUris;
            }

            @Override
            protected void onPostExecute(ArrayList<String> strings) {
                if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                onLoadGalleryItems(strings);

                if (galleryAdapter.getItemCount() == 0){
                    galleryRecyclerView.setVisibility(View.GONE);
                    mediaErrorView.setText(getString(R.string.no_media_found));
                    mediaErrorView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void onLoadGalleryItems(final List<String> filePaths){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                galleryAdapter = new GalleryAdapter(filePaths);
                galleryRecyclerView.setAdapter(galleryAdapter);
            }
        });
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

    private ArrayList<String> getAllAudio(){
        ArrayList<String> audio = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.DATE_ADDED, MediaStore.Audio.AudioColumns.MIME_TYPE };
        String orderBy = MediaStore.Audio.AudioColumns.DATE_ADDED + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, orderBy);
        int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int mimeIndexData = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (MimeType.getTypeFromString(cursor.getString(mimeIndexData)) == MimeType.AUDIO) {
                    String audioPath = cursor.getString(columnIndexData);
                    audio.add(audioPath);
                }
            }
        }
        cursor.close();

        return audio;
    }

    private ArrayList<String> getAllVideo(){
        ArrayList<String> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DATE_ADDED, MediaStore.Video.VideoColumns.MIME_TYPE };
        String orderBy = MediaStore.Video.VideoColumns.DATE_ADDED + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, orderBy);
        int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        int mimeIndexData = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (MimeType.getTypeFromString(cursor.getString(mimeIndexData)) == MimeType.VIDEO) {
                    String videoPath = cursor.getString(columnIndexData);
                    videos.add(videoPath);
                }
            }
        }
        cursor.close();

        return videos;
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


    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {

        private List<String> filePaths = new ArrayList<>();

        public GalleryAdapter(List<String> filePaths){
            this.filePaths = filePaths;
        }

        @Override
        public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new GalleryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(GalleryHolder holder, int position) {
            String path = filePaths.get(position);

            holder.bind(path);
        }

        @Override
        public int getItemCount() {
            return filePaths.size();
        }
    }


    private class GalleryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String path;
        private boolean isSelected;
        private ImageView galleryImageView;
        private ImageView checkmarkView;
        private ImageView videoIndicatorView;
        private TextView galleryFileName;

        public GalleryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_gallery_view, parent, false));

            galleryImageView = (ImageView) itemView.findViewById(R.id.galleryImageView);
            checkmarkView = (ImageView) itemView.findViewById(R.id.checkmarkView);
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
            }else if (mimeType == MimeType.AUDIO){
                String fileName = Uri.parse(path).getLastPathSegment();

                galleryImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_music));
                galleryImageView.setBackgroundColor(Color.WHITE);
                galleryFileName.setText(fileName);
                galleryFileName.setVisibility(View.VISIBLE);
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

            if (attachments.contains(path)){
                setSelected(true);
            } else {
                setSelected(false);
            }
        }

        public void setSelected(boolean selected){
            isSelected = selected;

            if (selected) {
                if (!attachments.contains(path)){
                    attachments.add(path);
                }
                checkmarkView.setVisibility(View.VISIBLE);
            }else {
                if (attachments.contains(path)){
                    attachments.remove(path);
                }
                checkmarkView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            setSelected(!isSelected);
        }
    }

    interface AttachmentInputListener {

        void setAttachmentsInput(List<String> attachments);
    }
}