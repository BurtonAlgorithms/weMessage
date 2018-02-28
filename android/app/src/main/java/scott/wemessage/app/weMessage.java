package scott.wemessage.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.FontRequest;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.fabric.sdk.android.Fabric;

import scott.wemessage.R;
import scott.wemessage.app.jobs.JobsCreator;
import scott.wemessage.app.jobs.SyncMessagesJob;
import scott.wemessage.app.messages.MessageDatabase;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.notifications.NotificationManager;
import scott.wemessage.app.models.users.Account;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.models.users.Session;
import scott.wemessage.app.security.util.AesPrngHelper;
import scott.wemessage.app.security.util.AndroidBase64Wrapper;
import scott.wemessage.app.sms.MmsDatabase;
import scott.wemessage.app.sms.MmsManager;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.commons.Constants;
import scott.wemessage.commons.connection.ClientMessage;
import scott.wemessage.commons.connection.ServerMessage;
import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.utils.ByteArrayAdapter;
import scott.wemessage.commons.utils.StringUtils;

public final class weMessage extends Application implements Constants {

    public static final int DATABASE_VERSION = 3;
    public static final int CONNECTION_TIMEOUT_WAIT = 5;
    public static final int MAX_CHAT_ICON_SIZE = 26214400;
    public static int MAX_MMS_ATTACHMENT_SIZE = 10485760;

    public static final String DATABASE_NAME = "weMessage.db";
    public static final String APP_IDENTIFIER = "scott.wemessage.app";
    public static final String IDENTIFIER_PREFIX = "scott.wemessage.app.";
    public static final String ATTACHMENT_FOLDER_NAME = "attachments";
    public static final String CHAT_ICONS_FOLDER_NAME = "chaticons";
    public static final String NOTIFICATION_CHANNEL_NAME = "weMessage-Notifications";
    public static final String NOTIFICATION_TAG = "weMessage-Notification-";

    public static final int REQUEST_PERMISSION_READ_STORAGE = 5000;
    public static final int REQUEST_PERMISSION_CAMERA = 5002;
    public static final int REQUEST_PERMISSION_RECORD_AUDIO = 5003;
    public static final int REQUEST_PERMISSION_WRITE_STORAGE = 5004;
    public static final int REQUEST_PERMISSION_READ_CONTACTS = 5005;
    public static final int REQUEST_PERMISSION_SMS = 5006;

    public static final int REQUEST_CODE_CAMERA = 6000;

    public static final String BUNDLE_HOST = IDENTIFIER_PREFIX + "bundleHost";
    public static final String BUNDLE_EMAIL = IDENTIFIER_PREFIX + "bundleEmail";
    public static final String BUNDLE_PASSWORD = IDENTIFIER_PREFIX + "bundlePassword";
    public static final String BUNDLE_FAILOVER_IP = IDENTIFIER_PREFIX + "bundleFailoverIP";
    public static final String BUNDLE_ALERT_TITLE = IDENTIFIER_PREFIX + "bundleAlertTitle";
    public static final String BUNDLE_FAST_CONNECT = IDENTIFIER_PREFIX + "bundleFastConnect";
    public static final String BUNDLE_ALERT_MESSAGE = IDENTIFIER_PREFIX + "bundleAlertMessage";
    public static final String BUNDLE_ALERT_POSITIVE_BUTTON = IDENTIFIER_PREFIX + "bundleAlertPositiveButton";
    public static final String BUNDLE_DIALOG_ANIMATION = IDENTIFIER_PREFIX + "bundleDialogAnimation";
    public static final String BUNDLE_IS_LAUNCHER_STILL_CONNECTING = IDENTIFIER_PREFIX + "bundleIsLauncherStillConnecting";
    public static final String BUNDLE_DISCONNECT_REASON_ALTERNATE_MESSAGE = IDENTIFIER_PREFIX + "bundleDisconnectReasonAlternateMessage";
    public static final String BUNDLE_ACTION_PERFORM_ALTERNATE_ERROR_MESSAGE = IDENTIFIER_PREFIX + "bundleActionPerformAlternateErrorMessage";
    public static final String BUNDLE_LAUNCHER_LAST_HASHED_PASS = IDENTIFIER_PREFIX + "bundleLauncherLastHashedPass";
    public static final String BUNDLE_LAUNCHER_DO_NOT_TRY_RECONNECT = IDENTIFIER_PREFIX + "bundleLauncherDoNotTryReconnect";
    public static final String BUNDLE_LAUNCHER_GO_TO_CONVERSATION_UUID = IDENTIFIER_PREFIX + "bundleLauncherGoToConversationUuid";

    public static final String BUNDLE_RETURN_POINT = IDENTIFIER_PREFIX + "bundleReturnPoint";
    public static final String BUNDLE_CONVERSATION_CHAT = IDENTIFIER_PREFIX + "bundleConversationChat";
    public static final String BUNDLE_CONVERSATION_GO_BACK_REASON = IDENTIFIER_PREFIX + "bundleConversationGoBackReason";
    public static final String BUNDLE_FULL_SCREEN_IMAGE_URI = IDENTIFIER_PREFIX + "bundleFullScreenImageUri";
    public static final String BUNDLE_FULL_SCREEN_VIDEO_URI = IDENTIFIER_PREFIX + "bundleFullScreenVideoUri";
    public static final String BUNDLE_SELECTED_GALLERY_STORE = IDENTIFIER_PREFIX + "bundleSelectedGalleryStore";
    public static final String BUNDLE_GALLERY_FRAGMENT_OPEN = IDENTIFIER_PREFIX + "bundleGalleryFragmentOpen";
    public static final String BUNDLE_CAMERA_ATTACHMENT_FILE = IDENTIFIER_PREFIX + "bundleCameraAttachmentFile";
    public static final String BUNDLE_VOICE_MESSAGE_INPUT_FILE = IDENTIFIER_PREFIX + "bundleVoiceMessageInputFile";
    public static final String BUNDLE_CREATE_CHAT_CONTACT_UUIDS = IDENTIFIER_PREFIX + "bundleCreateChatContactUuids";
    public static final String BUNDLE_CREATE_CHAT_UNKNOWN_HANDLES = IDENTIFIER_PREFIX + "bundleCreateChatUnknownHandles";
    public static final String BUNDLE_CONTACT_VIEW_UUID = IDENTIFIER_PREFIX + "bundleContactViewUuid";
    public static final String BUNDLE_HANDLE_UUID = IDENTIFIER_PREFIX + "bundleHandleUuid";
    public static final String BUNDLE_GO_TO_CONTACT_LIST = IDENTIFIER_PREFIX + "bundleGoToContactList";
    public static final String BUNDLE_SET_SMS_PERMISSION_ERROR = IDENTIFIER_PREFIX + "bundleSetSmsPermissionError";
    public static final String BUNDLE_SET_SMS_FROM_SETTINGS = IDENTIFIER_PREFIX + "bundleSetSmsFromSettings";
    public static final String BUNDLE_EDIT_NUMBER_FROM_SETTINGS = IDENTIFIER_PREFIX + "bundleEditNumberFromSettings";
    public static final String BUNDLE_SWITCH_ACCOUNTS_MODE = IDENTIFIER_PREFIX + "bundleSwitchAccountsMode";

    public static final String ARG_HOST = IDENTIFIER_PREFIX + "hostArg";
    public static final String ARG_PORT = IDENTIFIER_PREFIX + "portArg";
    public static final String ARG_EMAIL = IDENTIFIER_PREFIX + "emailArg";
    public static final String ARG_PASSWORD = IDENTIFIER_PREFIX + "passwordArg";
    public static final String ARG_FAILOVER_IP = IDENTIFIER_PREFIX + "failoverIpArg";
    public static final String ARG_PASSWORD_ALREADY_HASHED = IDENTIFIER_PREFIX + "passwordAlreadyHashed";
    public static final String ARG_ATTACHMENT_GALLERY_CACHE = IDENTIFIER_PREFIX + "attachmentGalleryCacheArg";
    public static final String ARG_CAMERA_ATTACHMENT_FILE = IDENTIFIER_PREFIX + "argCameraAttachmentFile";
    public static final String ARG_ATTACHMENT_POPUP_CAMERA_RESULT_CODE = IDENTIFIER_PREFIX + "argAttachmentPopupCameraResultCode";
    public static final String ARG_ATTACHMENT_POPUP_CAMERA_INTENT = IDENTIFIER_PREFIX + "argAttachmentPopupCameraIntent";
    public static final String ARG_VOICE_RECORDING_FILE = IDENTIFIER_PREFIX + "argVoiceRecordingFile";

    public static final String BROADCAST_LOGIN_TIMEOUT = IDENTIFIER_PREFIX + "LoginTimeout";
    public static final String BROADCAST_LOGIN_ERROR = IDENTIFIER_PREFIX + "LoginError";
    public static final String BROADCAST_LOGIN_CONNECTION_ERROR = IDENTIFIER_PREFIX + "LoginEstablishConnectionError";
    public static final String BROADCAST_CONNECTION_SERVICE_STOPPED = IDENTIFIER_PREFIX + "ConnectionServiceStopped";

    public static final String BROADCAST_LOGIN_SUCCESSFUL = IDENTIFIER_PREFIX + "LoginSuccessful";
    public static final String BROADCAST_DISCONNECT_REASON_ALREADY_CONNECTED = IDENTIFIER_PREFIX + "DisconnectReasonAlreadyConnected";
    public static final String BROADCAST_DISCONNECT_REASON_INVALID_LOGIN = IDENTIFIER_PREFIX + "DisconnectReasonInvalidLogin";
    public static final String BROADCAST_DISCONNECT_REASON_SERVER_CLOSED = IDENTIFIER_PREFIX + "DisconnectReasonServerClosed";
    public static final String BROADCAST_DISCONNECT_REASON_ERROR = IDENTIFIER_PREFIX + "DisconnectReasonError";
    public static final String BROADCAST_DISCONNECT_REASON_FORCED = IDENTIFIER_PREFIX + "DisconnectReasonForced";
    public static final String BROADCAST_DISCONNECT_REASON_CLIENT_DISCONNECTED = IDENTIFIER_PREFIX + "DisconnectReasonClientDisconnected";
    public static final String BROADCAST_DISCONNECT_REASON_INCORRECT_VERSION = IDENTIFIER_PREFIX + "DisconnectReasonIncorrectVersion";

    public static final String BROADCAST_SEND_MESSAGE_ERROR = IDENTIFIER_PREFIX + "SendMessageError";
    public static final String BROADCAST_NEW_MESSAGE_ERROR = IDENTIFIER_PREFIX + "NewMessageError";
    public static final String BROADCAST_MESSAGE_UPDATE_ERROR = IDENTIFIER_PREFIX + "MessageUpdateError";
    public static final String BROADCAST_ACTION_PERFORM_ERROR = IDENTIFIER_PREFIX + "ActionPerformError";
    public static final String BROADCAST_RESULT_PROCESS_ERROR = IDENTIFIER_PREFIX + "ResultProcessError";
    public static final String BROADCAST_CONTACT_SYNC_SUCCESS = IDENTIFIER_PREFIX + "ContactSyncSuccess";
    public static final String BROADCAST_CONTACT_SYNC_FAILED = IDENTIFIER_PREFIX + "ContactSyncFailed";
    public static final String BROADCAST_NO_ACCOUNTS_FOUND_NOTIFICATION = IDENTIFIER_PREFIX + "NoAccountsFoundNotification";
    public static final String BROADCAST_SMS_MODE_ENABLED = IDENTIFIER_PREFIX + "SmsModeEnabled";
    public static final String BROADCAST_SMS_MODE_DISABLED = IDENTIFIER_PREFIX + "SmsModeDisabled";
    public static final String BROADCAST_COMPOSE_SMS_LAUNCH = IDENTIFIER_PREFIX + "ComposeSmsLaunch";

    public static final String BROADCAST_LOAD_ATTACHMENT_ERROR = IDENTIFIER_PREFIX + "LoadAttachmentError";
    public static final String BROADCAST_PLAY_AUDIO_ATTACHMENT_ERROR = IDENTIFIER_PREFIX + "PlayAudioAttachmentError";
    public static final String BROADCAST_IMAGE_FULLSCREEN_ACTIVITY_START = IDENTIFIER_PREFIX + "ImageFullScreenActivityStart";
    public static final String BROADCAST_VIDEO_FULLSCREEN_ACTIVITY_START = IDENTIFIER_PREFIX + "VideoFullScreenActivityStart";

    public static final String SHARED_PREFERENCES_VERSION = IDENTIFIER_PREFIX + "version";
    public static final String SHARED_PREFERENCES_LAST_VERSION = IDENTIFIER_PREFIX + "lastVersion";
    public static final String SHARED_PREFERENCES_SHOW_SETUP_INFO = IDENTIFIER_PREFIX + "showSetupInfo";
    public static final String SHARED_PREFERENCES_SHOW_UPDATE_DIALOG = IDENTIFIER_PREFIX + "showUpdateDialog";
    public static final String SHARED_PREFERENCES_LAST_HOST = IDENTIFIER_PREFIX + "lastHost";
    public static final String SHARED_PREFERENCES_LAST_EMAIL = IDENTIFIER_PREFIX + "lastEmail";
    public static final String SHARED_PREFERENCES_LAST_HASHED_PASSWORD = IDENTIFIER_PREFIX + "lastHashedPassword";
    public static final String SHARED_PREFERENCES_LAST_FAILOVER_IP = IDENTIFIER_PREFIX + "lastFailoverIp";
    public static final String SHARED_PREFERENCES_SIGNED_OUT = IDENTIFIER_PREFIX + "signedOut";
    public static final String SHARED_PREFERENCES_SIGNED_OUT_EMAIL = IDENTIFIER_PREFIX + "signedOutEmail";
    public static final String SHARED_PREFERENCES_DEVICE_INFO = IDENTIFIER_PREFIX + "deviceInfo";
    public static final String SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW = IDENTIFIER_PREFIX + "contactSyncPermissionShow";
    public static final String SHARED_PREFERENCES_MANUAL_PHONE_NUMBER = IDENTIFIER_PREFIX + "manualPhoneNumber";
    public static final String SHARED_PREFERENCES_PROMPT_FOR_SMS = IDENTIFIER_PREFIX + "promptForSms";

    private static weMessage instance;
    private MessageDatabase messageDatabase;
    private MessageManager messageManager;
    private MmsDatabase mmsDatabase;
    private MmsManager mmsManager;
    private Session currentSession;
    private NotificationManager notificationManager;
    private File attachmentFolder;
    private File chatIconsFolder;

    private final Object mmsDatabaseLock = new Object();
    private final Object mmsManagerLock = new Object();

    public AtomicBoolean isDefaultSmsApplication = new AtomicBoolean(false);
    private AtomicBoolean isEmojiInitialized = new AtomicBoolean(false);

    public static weMessage get(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (AppLogger.USE_CRASHLYTICS) Fabric.with(this, new Crashlytics());

        updateDatabase();
        generateSharedPreferences();

        File attachmentFolder = new File(getFilesDir(), weMessage.ATTACHMENT_FOLDER_NAME);
        File chatIconsFolder = new File(getFilesDir(), weMessage.CHAT_ICONS_FOLDER_NAME);

        AndroidBase64Wrapper base64Wrapper = new AndroidBase64Wrapper();
        ByteArrayAdapter byteArrayAdapter = new ByteArrayAdapter(base64Wrapper);
        EmojiCompat.Config emojiConfig = new FontRequestEmojiCompatConfig(this,
                new FontRequest("com.google.android.gms.fonts", "com.google.android.gms", "Noto Color Emoji Compat", R.array.com_google_android_gms_fonts_certs))
                .registerInitCallback(new EmojiCompat.InitCallback() {
                    @Override
                    public void onInitialized() {
                        isEmojiInitialized.set(true);
                    }
                });

        AESCrypto.setMemoryAvailabilityCheck(true);
        AESCrypto.setBase64Wrapper(base64Wrapper);
        AESCrypto.setPrngHelper(new AesPrngHelper());

        ClientMessage.setByteArrayAdapter(byteArrayAdapter);
        ServerMessage.setByteArrayAdapter(byteArrayAdapter);

        EmojiCompat.init(emojiConfig);
        JobManager.create(this).addJobCreator(new JobsCreator());

        attachmentFolder.mkdir();
        chatIconsFolder.mkdir();

        this.attachmentFolder = attachmentFolder;
        this.chatIconsFolder = chatIconsFolder;
        this.currentSession = new Session();
        this.messageDatabase = new MessageDatabase(this);
        this.messageManager = new MessageManager(this);
        this.notificationManager = new NotificationManager(this);

        if (isSignedIn(true) && !StringUtils.isEmpty(getSharedPreferences().getString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, ""))){
            getCurrentSession().setAccount(getMessageDatabase().getAccountByEmail(getSharedPreferences().getString(weMessage.SHARED_PREFERENCES_LAST_EMAIL, "")));
        }

        getMessageManager().initialize();
        if (MmsManager.isDefaultSmsApp()) enableSmsMode(getMessageDatabase().getSmsChats().isEmpty());
        else disableSmsMode();

        IOUtils.setDeviceName();
    }

    public synchronized MessageDatabase getMessageDatabase(){
        return messageDatabase;
    }

    public synchronized MessageManager getMessageManager(){
        return messageManager;
    }

    public MmsDatabase getMmsDatabase(){
        synchronized (mmsDatabaseLock) {
            return mmsDatabase;
        }
    }

    public MmsManager getMmsManager(){
        synchronized (mmsManagerLock) {
            return mmsManager;
        }
    }

    public synchronized Session getCurrentSession(){
        return currentSession;
    }

    public synchronized NotificationManager getNotificationManager(){
        return notificationManager;
    }

    public SharedPreferences getSharedPreferences(){
        return getSharedPreferences(weMessage.APP_IDENTIFIER, Context.MODE_PRIVATE);
    }

    public synchronized File getAttachmentFolder(){
        return attachmentFolder;
    }

    public synchronized File getChatIconsFolder(){
        return chatIconsFolder;
    }

    public synchronized boolean isSignedIn(boolean iMessage){
        if (iMessage){
            return !getSharedPreferences().getBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL, true);
        }else {
            return !getSharedPreferences().getBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT, true);
        }
    }

    public boolean isEmojiCompatInitialized(){
        return isEmojiInitialized.get();
    }

    public synchronized void signIn(Account account){
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        if (account != null){
            getCurrentSession().setAccount(account);
            getMessageManager().initialize();
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL, false);
        }

        if (getMmsManager() != null) getMmsManager().initialize();

        editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT, false);
        editor.apply();
    }

    public synchronized void signOut(boolean fullSignOut){
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        if (fullSignOut){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT, true);
        }

        editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL, true);
        editor.apply();

        if (fullSignOut && getMmsManager() != null) getMmsManager().dumpMessages();

        notificationManager.cancelAll();
        getMessageManager().dumpMessages();
        getCurrentSession().setAccount(null);
    }

    public synchronized void enableSmsMode(boolean performResync){
        isDefaultSmsApplication.set(true);

        if (getCurrentSession().getSmsHandle() == null && !StringUtils.isEmpty(MmsManager.getPhoneNumber())){
            Handle handle = getMessageDatabase().getHandleByHandleID(MmsManager.getPhoneNumber());

            if (handle == null){
                handle = new Handle(UUID.randomUUID(), MmsManager.getPhoneNumber(), Handle.HandleType.ME, false, false);
                getMessageDatabase().addHandle(handle);
            }
            getCurrentSession().setSmsHandle(handle);
        }

        getMessageDatabase().configureSmsMode();

        synchronized (mmsDatabaseLock) {
            mmsDatabase = new MmsDatabase(this);
        }

        synchronized (mmsManagerLock) {
            mmsManager = new MmsManager(this);
        }

        getMmsManager().initialize();

        if (performResync){
            SyncMessagesJob.performSync();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(weMessage.BROADCAST_SMS_MODE_ENABLED));
    }

    public synchronized void disableSmsMode(){
        isDefaultSmsApplication.set(false);
        getMessageDatabase().configureSmsMode();

        if (getMmsManager() != null) getMmsManager().dumpMessages();

        synchronized (mmsManagerLock) {
            mmsManager = null;
        }

        synchronized (mmsDatabaseLock) {
            mmsDatabase = null;
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(weMessage.BROADCAST_SMS_MODE_DISABLED));
    }

    private void updateDatabase(){
        new MessageDatabase(this).getWritableDatabase().close();
    }

    private void generateSharedPreferences(){
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        int prefVersion = preferences.getInt(weMessage.SHARED_PREFERENCES_VERSION, -1);

        if (prefVersion != -1 && prefVersion != weMessage.WEMESSAGE_BUILD_VERSION){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, true);
            editor.putInt(weMessage.SHARED_PREFERENCES_LAST_VERSION, prefVersion);
            editor.putInt(weMessage.SHARED_PREFERENCES_VERSION, weMessage.WEMESSAGE_BUILD_VERSION);
        }else if (prefVersion == -1){
            if (!StringUtils.isEmpty(preferences.getString(weMessage.SHARED_PREFERENCES_DEVICE_INFO, ""))) {
                editor.putBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, true);
                editor.putInt(weMessage.SHARED_PREFERENCES_LAST_VERSION, 10);
                editor.putInt(weMessage.SHARED_PREFERENCES_VERSION, weMessage.WEMESSAGE_BUILD_VERSION);
            }else {
                editor.putBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, false);
                editor.putInt(weMessage.SHARED_PREFERENCES_LAST_VERSION, weMessage.WEMESSAGE_BUILD_VERSION);
                editor.putInt(weMessage.SHARED_PREFERENCES_VERSION, weMessage.WEMESSAGE_BUILD_VERSION);
            }
        }

        if (!preferences.contains(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW)){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_CONTACT_SYNC_PERMISSION_SHOW, true);
        }

        if (!preferences.contains(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER)){
            editor.putString(weMessage.SHARED_PREFERENCES_MANUAL_PHONE_NUMBER, "");
        }

        if (!preferences.contains(weMessage.SHARED_PREFERENCES_PROMPT_FOR_SMS)){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_PROMPT_FOR_SMS, MmsManager.isPhone());
        }

        if (!preferences.contains(weMessage.SHARED_PREFERENCES_SIGNED_OUT)){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT, true);
        }

        if (!preferences.contains(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL)){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL, true);
        }

        if (preferences.getBoolean(weMessage.SHARED_PREFERENCES_SHOW_UPDATE_DIALOG, false)){
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT, true);
            editor.putBoolean(weMessage.SHARED_PREFERENCES_SIGNED_OUT_EMAIL, true);
        }

        editor.apply();
    }
}