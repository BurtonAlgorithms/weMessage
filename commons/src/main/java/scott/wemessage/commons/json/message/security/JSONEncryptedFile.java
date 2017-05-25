package scott.wemessage.commons.json.message.security;

public class JSONEncryptedFile {

    private byte[] encryptedData;
    private String ivParams;
    private String key;

    public JSONEncryptedFile(byte[] encryptedData, String key, String ivParams){
        this.encryptedData = encryptedData;
        this.key = key;
        this.ivParams = ivParams;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public String getIvParams(){
        return ivParams;
    }

    public String getKey() {
        return key;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public void setIvParams(String ivParams) {
        this.ivParams = ivParams;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
