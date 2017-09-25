package scott.wemessage.app.security;

import scott.wemessage.commons.connection.security.EncryptedText;

public class KeyTextPair {

    private final String key;
    private final String encryptedText;

    public KeyTextPair(String encryptedText, String key){
        this.key = key;
        this.encryptedText = encryptedText;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public String getKey() {
        return key;
    }

    public static EncryptedText toEncryptedText(KeyTextPair keyTextPair){
        return new EncryptedText(keyTextPair.getEncryptedText(), keyTextPair.getKey());
    }
}