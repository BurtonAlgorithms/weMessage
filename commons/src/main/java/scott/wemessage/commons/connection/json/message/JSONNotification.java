package scott.wemessage.commons.connection.json.message;

import com.google.gson.annotations.SerializedName;

public class JSONNotification {

    @SerializedName("notificationVersion")
    private String notificationVersion;

    @SerializedName("registrationToken")
    private String registrationToken;

    @SerializedName("encryptedText")
    private String encryptedText;

    @SerializedName("key")
    private String key;

    @SerializedName("handleId")
    private String handleId;

    @SerializedName("chatId")
    private String chatId;

    @SerializedName("chatName")
    private String chatName;

    @SerializedName("attachmentNumber")
    private String attachmentNumber;

    @SerializedName("accountLogin")
    private String accountLogin;

    public JSONNotification(){

    }

    public JSONNotification(String notificationVersion, String registrationToken, String encryptedText, String key, String handleId, String chatId, String chatName, String attachmentNumber, String accountLogin) {
        this.notificationVersion = notificationVersion;
        this.registrationToken = registrationToken;
        this.encryptedText = encryptedText;
        this.key = key;
        this.handleId = handleId;
        this.chatId = chatId;
        this.chatName = chatName;
        this.attachmentNumber = attachmentNumber;
        this.accountLogin = accountLogin;
    }

    public String getNotificationVersion() {
        return notificationVersion;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public String getKey() {
        return key;
    }

    public String getHandleId() {
        return handleId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public String getAttachmentNumber() {
        return attachmentNumber;
    }

    public String getAccountLogin() {
        return accountLogin;
    }

    public void setNotificationVersion(String notificationVersion) {
        this.notificationVersion = notificationVersion;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public void setEncryptedText(String encryptedText) {
        this.encryptedText = encryptedText;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setHandleId(String handleId) {
        this.handleId = handleId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setAttachmentNumber(String attachmentNumber) {
        this.attachmentNumber = attachmentNumber;
    }

    public void setAccountLogin(String accountLogin) {
        this.accountLogin = accountLogin;
    }
}