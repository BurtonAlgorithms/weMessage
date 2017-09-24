package scott.wemessage.app.security;

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

}