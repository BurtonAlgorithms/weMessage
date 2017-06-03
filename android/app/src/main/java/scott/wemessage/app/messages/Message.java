package scott.wemessage.app.messages;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.messages.chat.Conversation;
import scott.wemessage.commons.utils.DateUtils;

public class Message {

    private UUID uuid;
    private String macGuid;
    private Conversation conversation;
    private Contact sender;
    private List<Attachment> attachments;
    private String text;
    private Integer dateSent, dateDelivered, dateRead;
    private Boolean errored, isSent, isDelivered, isRead, isFinished, isFromMe;

    public Message(UUID uuid, String macGuid, Conversation conversation, Contact sender, List<Attachment> attachments, String text, Integer dateSent, Integer dateDelivered, Integer dateRead,
                   Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.conversation = conversation;
        this.sender = sender;
        this.attachments = attachments;
        this.text = text;
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

    public UUID getUuid() {
        return uuid;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Contact getSender() {
        return sender;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getText() {
        return text;
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

    public Date getModernDateSent(){
        if (dateSent == null || dateSent == -1) return null;

        return DateUtils.getDateUsing2001(dateSent);
    }

    public Date getModernDateDelivered(){
        if (dateDelivered == null || dateDelivered == -1) return null;

        return DateUtils.getDateUsing2001(dateDelivered);
    }

    public Date getModernDateRead(){
        if (dateRead == null || dateRead == -1) return null;

        return DateUtils.getDateUsing2001(dateRead);
    }

    public Boolean hasErrored() {
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

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setSender(Contact sender) {
        this.sender = sender;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void setText(String text) {
        this.text = text;
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

    public void setHasErrored(Boolean errored) {
        this.errored = errored;
    }

    public void setIsSent(Boolean sent) {
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