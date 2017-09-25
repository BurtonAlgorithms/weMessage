package scott.wemessage.commons.connection.json.message;

public class JSONNotification {

    private String registrationToken;
    private String encryptedText;
    private String key;
    private String handleId;
    private String chatId;
    private String chatName;

    public JSONNotification(String registrationToken, String encryptedText, String key, String handleId, String chatId, String chatName) {
        this.registrationToken = registrationToken;
        this.encryptedText = encryptedText;
        this.key = key;
        this.handleId = handleId;
        this.chatId = chatId;
        this.chatName = chatName;
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
}