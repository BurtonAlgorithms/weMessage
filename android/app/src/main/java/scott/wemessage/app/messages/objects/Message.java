package scott.wemessage.app.messages.objects;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import scott.wemessage.app.chats.objects.Chat;
import scott.wemessage.commons.utils.DateUtils;

public class Message {

    private UUID uuid;
    private String macGuid;
    private Chat chat;
    private Contact sender;
    private List<Attachment> attachments;
    private String text;
    private Integer dateSent, dateDelivered, dateRead;
    private Boolean errored, isSent, isDelivered, isRead, isFinished, isFromMe;

    public Message(){

    }

    public Message(UUID uuid, String macGuid, Chat chat, Contact sender, List<Attachment> attachments, String text, Integer dateSent, Integer dateDelivered, Integer dateRead,
                   Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.chat = chat;
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

    public Chat getChat() {
        return chat;
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

    public Message setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Message setMacGuid(String macGuid) {
        this.macGuid = macGuid;
        return this;
    }

    public Message setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    public Message setSender(Contact sender) {
        this.sender = sender;
        return this;
    }

    public Message setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public Message setDateSent(Integer dateSent) {
        this.dateSent = dateSent;
        return this;
    }

    public Message setDateDelivered(Integer dateDelivered) {
        this.dateDelivered = dateDelivered;
        return this;
    }

    public Message setDateRead(Integer dateRead) {
        this.dateRead = dateRead;
        return this;
    }

    public Message setHasErrored(Boolean errored) {
        this.errored = errored;
        return this;
    }

    public Message setIsSent(Boolean sent) {
        isSent = sent;
        return this;
    }

    public Message setDelivered(Boolean delivered) {
        isDelivered = delivered;
        return this;
    }

    public Message setRead(Boolean read) {
        isRead = read;
        return this;
    }

    public Message setFinished(Boolean finished) {
        isFinished = finished;
        return this;
    }

    public Message setFromMe(Boolean fromMe) {
        isFromMe = fromMe;
        return this;
    }
}