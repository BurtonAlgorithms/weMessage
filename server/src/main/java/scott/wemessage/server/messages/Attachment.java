package scott.wemessage.server.messages;

import java.util.Date;

import scott.wemessage.commons.utils.DateUtils;

public class Attachment  {

    private int rowID;
    private String guid;
    private int createdDate;
    private String fileLocation;
    private String transferName;
    private String fileType;
    private int totalBytes;
    
    public Attachment(){
        this(null, -1, -1, null, null, null, -1);
    }

    public Attachment(String guid, int rowID, int createdDate, String fileLocation, String transferName, String fileType, int totalBytes){
        this.rowID = rowID;
        this.guid = guid;
        this.createdDate = createdDate;
        this.fileLocation = fileLocation;
        this.transferName = transferName;
        this.fileType = fileType;
        this.totalBytes = totalBytes;
    }

    public String getGuid() {
        return guid;
    }

    public int getRowID() {
        return rowID;
    }

    public int getCreatedDate() {
        return createdDate;
    }

    public String getFileLocation() {
        return fileLocation;
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

    public Date getModernCreatedDate() {
        return DateUtils.getDateUsing2001(createdDate);
    }

    public Attachment setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public Attachment setRowID(int rowID) {
        this.rowID = rowID;
        return this;
    }

    public Attachment setCreatedDate(int createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Attachment setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public Attachment setTransferName(String transferName) {
        this.transferName = transferName;
        return this;
    }

    public Attachment setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public Attachment setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }
}