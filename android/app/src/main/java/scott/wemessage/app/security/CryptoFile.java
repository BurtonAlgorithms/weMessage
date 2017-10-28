package scott.wemessage.app.security;

public class CryptoFile {

    private final String key;
    private final byte[] encryptedBytes;
    private final byte[] iv;

    public CryptoFile(byte[] encryptedBytes, String key, byte[] iv){
        this.key = key;
        this.encryptedBytes = encryptedBytes;
        this.iv = iv;
    }

    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    public String getKey() {
        return key;
    }

    public byte[] getIv(){
        return iv;
    }

}