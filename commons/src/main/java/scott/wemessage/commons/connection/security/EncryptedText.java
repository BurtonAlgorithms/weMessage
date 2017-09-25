package scott.wemessage.commons.connection.security;

public class EncryptedText {

    private String encryptedText;
    private String key;

    public EncryptedText(String encryptedText, String key){
        this.encryptedText = encryptedText;
        this.key = key;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public String getKey() {
        return key;
    }

    public void setEncryptedText(String encryptedText) {
        this.encryptedText = encryptedText;
    }

    public void setKey(String key) {
        this.key = key;
    }
}