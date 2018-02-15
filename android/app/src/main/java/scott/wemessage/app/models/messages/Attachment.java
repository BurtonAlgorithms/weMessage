package scott.wemessage.app.models.messages;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Attachment {

    private UUID uuid;
    private String macGuid;
    private String transferName;
    private FileLocationContainer fileLocation;
    private String fileType;
    private Long totalBytes;
    private String boundSmsChat = null;
    private String boundSmsMessage = null;

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

    public String getBoundSmsChat() {
        return boundSmsChat;
    }

    public String getBoundSmsMessage(){
        return boundSmsMessage;
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

    public Attachment setBoundSmsChat(String boundSmsChat) {
        this.boundSmsChat = boundSmsChat;
        return this;
    }

    public Attachment setBoundSmsMessage(String smsMessage){
        this.boundSmsMessage = smsMessage;
        return this;
    }
}