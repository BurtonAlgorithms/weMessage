package scott.wemessage.commons.json.message;

import scott.wemessage.commons.json.message.security.JSONEncryptedFile;

public class JSONAttachment {

    private String macGuid;
    private String transferName;
    private String fileType;
    private JSONEncryptedFile fileData;
    private int totalBytes;

    public JSONAttachment(String macGuid, String transferName, String fileType, JSONEncryptedFile fileData, int totalBytes){
        this.macGuid = macGuid;
        this.transferName = transferName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.totalBytes = totalBytes;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public String getTransferName() {
        return transferName;
    }

    public String getFileType() {
        return fileType;
    }

    public JSONEncryptedFile getFileData() {
        return fileData;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setTransferName(String transferName) {
        this.transferName = transferName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileData(JSONEncryptedFile fileData) {
        this.fileData = fileData;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }
}