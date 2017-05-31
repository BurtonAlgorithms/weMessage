package scott.wemessage.app.security;

import scott.wemessage.commons.json.message.security.JSONEncryptedText;

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

    public static JSONEncryptedText toEncryptedJSON(KeyTextPair keyTextPair){
        return new JSONEncryptedText(keyTextPair.getEncryptedText(), keyTextPair.getKey());
    }
}