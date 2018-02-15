package scott.wemessage.app.models.messages;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import scott.wemessage.app.connection.ConnectionHandler;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.security.CryptoFile;
import scott.wemessage.app.security.CryptoType;
import scott.wemessage.app.security.EncryptionTask;
import scott.wemessage.app.security.FailedCryptoFile;
import scott.wemessage.app.security.FileEncryptionTask;
import scott.wemessage.app.security.KeyTextPair;
import scott.wemessage.commons.connection.json.message.JSONAttachment;
import scott.wemessage.commons.connection.json.message.JSONChat;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.connection.security.EncryptedFile;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.utils.DateUtils;

public class Message extends MessageBase {

    private String identifier;
    private String macGuid;
    private Chat chat;
    private Handle sender;
    private List<Attachment> attachments;
    private String text;
    private Long dateSent, dateDelivered, dateRead;
    private Boolean errored, isSent, isDelivered, isRead, isFinished, isFromMe, isEffectFinished;
    private HashMap<Attachment, FailReason> failedAttachments = new HashMap<>();
    private MessageEffect messageEffect = MessageEffect.NONE;

    public Message(){

    }

    public Message(String identifier, String macGuid, Chat chat, Handle sender, List<Attachment> attachments, String text, Long dateSent, Long dateDelivered, Long dateRead,
                   Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe, MessageEffect messageEffect, Boolean isEffectFinished){
        this.identifier = identifier;
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
        this.messageEffect = messageEffect;
        this.isEffectFinished = isEffectFinished;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public Chat getChat() {
        return chat;
    }

    public Handle getSender() {
        return sender;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public HashMap<Attachment, FailReason> getFailedAttachments(){
        return failedAttachments;
    }

    public String getText() {
        return text;
    }

    public Long getDateSent() {
        return dateSent;
    }

    public Long getDateDelivered() {
        return dateDelivered;
    }

    public Long getDateRead() {
        return dateRead;
    }

    @Override
    public Long getTimeIdentifier() {
        return dateSent;
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

    public MessageEffect getMessageEffect() {
        return messageEffect;
    }

    public Boolean getEffectFinished() {
        return isEffectFinished;
    }

    public Message setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Message setSender(Handle sender) {
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

    public Message setDateSent(Long dateSent) {
        this.dateSent = dateSent;
        return this;
    }

    public Message setDateDelivered(Long dateDelivered) {
        this.dateDelivered = dateDelivered;
        return this;
    }

    public Message setDateRead(Long dateRead) {
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

    public Message setMessageEffect(MessageEffect messageEffect) {
        this.messageEffect = messageEffect;
        return this;
    }

    public Message setEffectFinished(Boolean effectFinished) {
        isEffectFinished = effectFinished;
        return this;
    }

    public JSONMessage toJson(ConnectionHandler connectionHandler) throws IOException, GeneralSecurityException {
        Chat chat = getChat();
        List<String> participants = new ArrayList<>();
        List<JSONAttachment> attachments = new ArrayList<>();
        String displayName;
        String handle;

        if(chat instanceof PeerChat){
            displayName = null;
            participants.add(((PeerChat) chat).getHandle().getHandleID());
        } else {
            GroupChat groupChat = (GroupChat) chat;

            displayName = groupChat.getDisplayName();

            for (Handle h : groupChat.getParticipants()){
                participants.add(h.getHandleID());
            }
        }

        if (getSender() == null || getSender().getHandleType() == Handle.HandleType.ME){
            handle = null;
        }else {
            handle = getSender().getHandleID();
        }

        for (Attachment attachment : getAttachments()){
            FileEncryptionTask fileEncryptionTask = new FileEncryptionTask(new File(attachment.getFileLocation().getFileLocation()), null, CryptoType.AES);
            fileEncryptionTask.runEncryptTask();

            CryptoFile cryptoFile = fileEncryptionTask.getEncryptedFile();

            if (cryptoFile instanceof FailedCryptoFile){
                failedAttachments.put(attachment, ((FailedCryptoFile) cryptoFile).getFailReason());

                fileEncryptionTask = null;
                cryptoFile = null;
                continue;
            }

            EncryptedFile encryptedFile = new EncryptedFile(
                    attachment.getUuid().toString(),
                    attachment.getTransferName(),
                    cryptoFile.getEncryptedBytes(),
                    cryptoFile.getKey(),
                    cryptoFile.getIv()
            );

            JSONAttachment jsonAttachment = new JSONAttachment(
                    attachment.getUuid().toString(),
                    attachment.getMacGuid(),
                    attachment.getTransferName(),
                    attachment.getFileType(),
                    attachment.getTotalBytes()
            );

            connectionHandler.sendOutgoingFile(encryptedFile, attachment);
            attachments.add(jsonAttachment);

            fileEncryptionTask = null;
            cryptoFile = null;
            encryptedFile = null;
        }

        EncryptionTask encryptionTask = new EncryptionTask(getText(), null, CryptoType.AES);
        encryptionTask.runEncryptTask();

        return new JSONMessage(
                getMacGuid(),
                new JSONChat(
                        getChat().getMacGuid(),
                        getChat().getMacGroupID(),
                        getChat().getMacChatIdentifier(),
                        displayName,
                        participants
                ),
                handle,
                attachments,
                KeyTextPair.toEncryptedText(encryptionTask.getEncryptedText()),
                getDateSent(),
                getDateDelivered(),
                getDateRead(),
                hasErrored(),
                isSent(),
                isDelivered(),
                isRead(),
                isFinished(),
                isFromMe(),
                getMessageEffect().getEffectName()
        );
    }
}