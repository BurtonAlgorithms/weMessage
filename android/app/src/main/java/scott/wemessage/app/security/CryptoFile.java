package scott.wemessage.app.security;

import scott.wemessage.commons.json.message.security.JSONEncryptedFile;

public class CryptoFile {

    private final String key;
    private final byte[] encryptedBytes;
    private final String ivMac;

    public CryptoFile(byte[] encryptedBytes, String key, String ivMac){
        this.key = key;
        this.encryptedBytes = encryptedBytes;
        this.ivMac = ivMac;
    }

    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    public String getKey() {
        return key;
    }

    public String getIvMac(){
        return ivMac;
    }

    public static JSONEncryptedFile toEncryptedJSON(CryptoFile cryptoFile){
        return new JSONEncryptedFile(cryptoFile.getEncryptedBytes(), cryptoFile.getKey(), cryptoFile.getIvMac());
    }
}
