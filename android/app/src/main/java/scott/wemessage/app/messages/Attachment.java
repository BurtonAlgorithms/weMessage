package scott.wemessage.app.messages;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Attachment {

    private UUID uuid;
    private String macGuid;
    private String transferName;
    private FileLocationContainer fileLocation;
    private String fileType;
    private int totalBytes;

    public Attachment(UUID uuid, String macGuid, String transferName, FileLocationContainer fileLocation, String fileType, int totalBytes){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.transferName = transferName;
        this.fileLocation = fileLocation;
        this.fileLocation = fileLocation;
        this.totalBytes = totalBytes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public String getTransferName() {
        return transferName;
    }

    public FileLocationContainer getFileLocation() {
        return fileLocation;
    }

    public String getFileType() {
        return fileType;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setTransferName(String transferName) {
        this.transferName = transferName;
    }

    public void setFileLocation(FileLocationContainer fileLocation) {
        this.fileLocation = fileLocation;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }
}