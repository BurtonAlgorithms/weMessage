package scott.wemessage.commons.connection.json.message;

public class JSONAttachment {

    private String uuid;
    private String macGuid;
    private String transferName;
    private String fileType;
    private int totalBytes;

    public JSONAttachment(String uuid, String macGuid, String transferName, String fileType, int totalBytes){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.transferName = transferName;
        this.fileType = fileType;
        this.totalBytes = totalBytes;
    }

    public String getUuid(){
        return uuid;
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

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
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

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }
}