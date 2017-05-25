package scott.wemessage.commons.json.message;

import scott.wemessage.commons.json.message.security.JSONEncryptedText;

import java.util.List;

public class JSONMessage {

    private String macGuid;
    private JSONChat chat;
    private String handle;
    private List<JSONAttachment> attachments;
    private JSONEncryptedText encryptedText;
    private Integer dateSent, dateDelivered, dateRead;
    private Boolean errored, isSent, isDelivered, isRead, isFinished, isFromMe;

    public JSONMessage(String macGuid, JSONChat chat, String handle, List<JSONAttachment> attachments, JSONEncryptedText encryptedText, Integer dateSent, Integer dateDelivered, Integer dateRead,
                       Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe){
        this.macGuid = macGuid;
        this.chat = chat;
        this.handle = handle;
        this.attachments = attachments;
        this.encryptedText = encryptedText;
        this.attachments = attachments;
        this.dateSent = dateSent;
        this.dateDelivered = dateDelivered;
        this.dateRead = dateRead;
        this.errored = errored;
        this.isSent = isSent;
        this.isDelivered = isDelivered;
        this.isRead = isRead;
        this.isFinished = isFinished;
        this.isFromMe = isFromMe;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public JSONChat getChat() {
        return chat;
    }

    public String getHandle() {
        return handle;
    }

    public List<JSONAttachment> getAttachments() {
        return attachments;
    }

    public JSONEncryptedText getEncryptedText() {
        return encryptedText;
    }

    public Integer getDateSent() {
        return dateSent;
    }

    public Integer getDateDelivered() {
        return dateDelivered;
    }

    public Integer getDateRead() {
        return dateRead;
    }

    public Boolean getErrored() {
        return errored;
    }

    public Boolean isSent() {
        return isSent;
    }

    public Boolean isDelivered() {
        return isDelivered;
    }

    public Boolean isRead() {
        return isRead;
    }

    public Boolean isFinished() {
        return isFinished;
    }

    public Boolean isFromMe() {
        return isFromMe;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setChat(JSONChat chat) {
        this.chat = chat;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setAttachments(List<JSONAttachment> attachments) {
        this.attachments = attachments;
    }

    public void setEncryptedText(JSONEncryptedText encryptedText) {
        this.encryptedText = encryptedText;
    }

    public void setDateSent(Integer dateSent) {
        this.dateSent = dateSent;
    }

    public void setDateDelivered(Integer dateDelivered) {
        this.dateDelivered = dateDelivered;
    }

    public void setDateRead(Integer dateRead) {
        this.dateRead = dateRead;
    }

    public void setErrored(Boolean errored) {
        this.errored = errored;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
    }

    public void setDelivered(Boolean delivered) {
        isDelivered = delivered;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public void setFinished(Boolean finished) {
        isFinished = finished;
    }

    public void setFromMe(Boolean fromMe) {
        isFromMe = fromMe;
    }
}