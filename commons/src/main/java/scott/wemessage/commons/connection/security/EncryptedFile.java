package scott.wemessage.commons.connection.security;

import java.io.Serializable;

public class EncryptedFile implements Serializable {

    private String uuid;
    private String transferName;
    private byte[] encryptedData;
    private String ivParams;
    private String key;

    public EncryptedFile(String uuid, String transferName, byte[] encryptedData, String key, String ivParams){
        this.uuid = uuid;
        this.transferName = transferName;
        this.encryptedData = encryptedData;
        this.key = key;
        this.ivParams = ivParams;
    }

    public String getUuid(){
        return uuid;
    }

    public String getTransferName() {
        return transferName;
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

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public void setTransferName(String transferName) {
        this.transferName = transferName;
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
