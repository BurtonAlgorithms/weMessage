package scott.wemessage.app.utils.media;

public interface MediaDownloadCallbacks {

    boolean canMediaDownloadTaskStart(String attachmentUri);

    void onMediaDownloadTaskStart(String attachmentUri);

    void onMediaDownloadTaskFinish(String attachmentUri);
}
