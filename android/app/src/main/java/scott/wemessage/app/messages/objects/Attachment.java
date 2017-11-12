package scott.wemessage.app.messages.objects;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Attachment {

    private UUID uuid;
    private String macGuid;
    private String transferName;
    private FileLocationContainer fileLocation;
    private String fileType;
    private long totalBytes;

    public Attachment(){

    }

    public Attachment(UUID uuid, String macGuid, String transferName, FileLocationContainer fileLocation, String fileType, long totalBytes){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.transferName = transferName;
        this.fileLocation = fileLocation;
        this.fileType = fileType;
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

    public long getTotalBytes() {
        return totalBytes;
    }

    public Attachment setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Attachment setMacGuid(String macGuid) {
        this.macGuid = macGuid;
        return this;
    }

    public Attachment setTransferName(String transferName) {
        this.transferName = transferName;
        return this;
    }

    public Attachment setFileLocation(FileLocationContainer fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public Attachment setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public Attachment setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }
}