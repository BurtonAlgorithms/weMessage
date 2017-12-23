package scott.wemessage.server.messages;

import org.apache.commons.io.FilenameUtils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import scott.wemessage.commons.connection.json.message.JSONAttachment;
import scott.wemessage.commons.connection.json.message.JSONChat;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.connection.security.EncryptedFile;
import scott.wemessage.commons.connection.security.EncryptedText;
import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.FileUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.connection.Device;
import scott.wemessage.server.messages.chat.ChatBase;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;
import scott.wemessage.server.scripts.AppleScriptExecutor;
import scott.wemessage.server.weMessage;

public class Message {

    private String guid;
    private long rowID;
    private ChatBase chat;
    private Handle handle;
    private String text;
    private List<Attachment> attachments;
    private Long dateSent, dateDelivered, dateRead;
    private Boolean errored, isSent, isDelivered, isRead, isFinished, isFromMe;
    private MessageEffect messageEffect;

    public Message(){
        this(null, -1L, null, null, null, null, null, null, null, null, null, null, null, null, null, MessageEffect.NONE);
    }

    public Message(String guid, long rowID, ChatBase chat, Handle handle, List<Attachment> attachments, String text, Long dateSent, Long dateDelivered, Long dateRead,
                   Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe, MessageEffect messageEffect){
        this.guid = guid;
        this.rowID = rowID;
        this.chat = chat;
        this.handle = handle;
        this.text = text;
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
        this.messageEffect = messageEffect;
    }

    public String getGuid() {
        return guid;
    }

    public long getRowID() {
        return rowID;
    }

    public ChatBase getChat() {
        return chat;
    }

    public Handle getHandle() {
        return handle;
    }

    public String getText() {
        return text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
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

    public Boolean hasAttachments(){
        return !attachments.isEmpty();
    }

    public MessageEffect getMessageEffect() {
        return messageEffect;
    }

    public Message setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public Message setRowID(long rowID) {
        this.rowID = rowID;
        return this;
    }

    public Message setChat(ChatBase chat) {
        this.chat = chat;
        return this;
    }

    public Message setHandle(Handle handle) {
        this.handle = handle;
        return this;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public Message setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
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

    public Message setErrored(boolean errored) {
        this.errored = errored;
        return this;
    }

    public Message setSent(boolean sent) {
        isSent = sent;
        return this;
    }

    public Message setDelivered(boolean delivered) {
        isDelivered = delivered;
        return this;
    }

    public Message setRead(boolean read) {
        isRead = read;
        return this;
    }

    public Message setFinished(boolean finished) {
        isFinished = finished;
        return this;
    }

    public Message setFromMe(boolean fromMe) {
        isFromMe = fromMe;
        return this;
    }

    public Message setMessageEffect(MessageEffect messageEffect) {
        this.messageEffect = messageEffect;
        return this;
    }

    public JSONMessage toJson(Device device, ServerConfiguration serverConfiguration, AppleScriptExecutor appleScriptExecutor, boolean sendAttachments) throws IOException, GeneralSecurityException {
        ChatBase chat = getChat();
        List<String> participants = new ArrayList<>();
        List<JSONAttachment> attachments = new ArrayList<>();
        String displayName;
        String handle;

        if(chat instanceof PeerChat){
            displayName = null;
            participants.add(((PeerChat) chat).getPeer().getHandleID());
        } else {
            GroupChat groupChat = (GroupChat) chat;

            displayName = groupChat.getDisplayName();

            for (Handle h : groupChat.getParticipants()){
                participants.add(h.getHandleID());
            }
        }

        if (getHandle() == null){
            handle = null;
        }else {
            handle = getHandle().getHandleID();
        }

        for (Attachment attachment : getAttachments()) {
            String filePath = attachment.getFileLocation().replace("~", System.getProperty("user.home"));

            if (new File(filePath).length() > weMessage.MAX_FILE_SIZE) {
                ServerLogger.log(ServerLogger.Level.WARNING, "Could not send attachment: " + attachment.getTransferName()
                        + " because it exceeds the maximum file size of " + FileUtils.getFileSizeString(weMessage.MAX_FILE_SIZE));
            } else {
                String attachmentUuid = UUID.randomUUID().toString();

                if (MimeType.MimeExtension.getExtensionFromString(attachment.getFileType()) == MimeType.MimeExtension.MOV) {
                    if (!serverConfiguration.getConfigJSON().getConfig().getTranscodeVideos()) continue;

                    FFmpeg ffmpeg = new FFmpeg(serverConfiguration.getConfigJSON().getConfig().getFfmpegLocation());
                    String outputName = FilenameUtils.removeExtension(attachment.getTransferName()) + ".webm";

                    ServerLogger.log(ServerLogger.Level.INFO, "An encoding process has just started for file: " + outputName + ". This allows videos to be played on Android devices.");
                    ServerLogger.log(ServerLogger.Level.INFO, "Please do not turn off your weServer, or the message will not send.");
                    ServerLogger.emptyLine();

                    FFmpegBuilder ffmpegBuilder = new FFmpegBuilder()
                            .setInput(filePath)
                            .overrideOutputFiles(true)
                            .addOutput(appleScriptExecutor.getTempFolder().toString() + "/" + outputName)
                            .setFormat("webm")
                            .setVideoCodec("libvpx-vp9")
                            .setAudioCodec("libopus")
                            .addExtraArgs("-b:v", "1400K")
                            .addExtraArgs("-crf", "26")
                            .addExtraArgs("-threads", "8")
                            .addExtraArgs("-speed", "4")
                            .addExtraArgs("-tile-columns", "6")
                            .addExtraArgs("-frame-parallel", "1")
                            .addExtraArgs("-auto-alt-ref", "1")
                            .addExtraArgs("-lag-in-frames", "25")
                            .done();

                    long startTime = System.nanoTime();

                    FFmpegExecutor ffmpegExecutor = new FFmpegExecutor(ffmpeg);
                    ffmpegExecutor.createJob(ffmpegBuilder).run();

                    long finishTime = System.nanoTime();

                    ServerLogger.log(ServerLogger.Level.INFO, "Finished encoding process. Time: " + (((finishTime - startTime) / 1000000L) / 1000L) + " seconds.");
                    ServerLogger.emptyLine();

                    File encodedFile = new File(appleScriptExecutor.getTempFolder().toString(), outputName);

                    if (sendAttachments) {
                        String keys = AESCrypto.keysToString(AESCrypto.generateKeys());
                        AESCrypto.CipherByteArrayIv byteArrayIv = AESCrypto.encryptFile(encodedFile, keys);

                        EncryptedFile encryptedFile = new EncryptedFile(
                                attachmentUuid,
                                outputName,
                                byteArrayIv.getCipherBytes(),
                                keys,
                                byteArrayIv.getIv()
                        );
                        device.sendOutgoingFile(encryptedFile);

                        byteArrayIv = null;
                        encryptedFile = null;
                    }

                    JSONAttachment jsonAttachment = new JSONAttachment(
                            attachmentUuid,
                            attachment.getGuid(),
                            outputName,
                            MimeType.MimeExtension.WEBM.getTypeString(),
                            encodedFile.length()
                    );

                    attachments.add(jsonAttachment);
                } else {
                    if (sendAttachments) {
                        String keys = AESCrypto.keysToString(AESCrypto.generateKeys());
                        AESCrypto.CipherByteArrayIv byteArrayIv = AESCrypto.encryptFile(new File(filePath), keys);

                        EncryptedFile encryptedFile = new EncryptedFile(
                                attachmentUuid,
                                attachment.getTransferName(),
                                byteArrayIv.getCipherBytes(),
                                keys,
                                byteArrayIv.getIv()
                        );

                        device.sendOutgoingFile(encryptedFile);

                        byteArrayIv = null;
                        encryptedFile = null;
                    }

                    JSONAttachment jsonAttachment = new JSONAttachment(
                            attachmentUuid,
                            attachment.getGuid(),
                            attachment.getTransferName(),
                            attachment.getFileType(),
                            attachment.getTotalBytes()
                    );

                    attachments.add(jsonAttachment);
                }
            }
        }

        String keys = AESCrypto.keysToString(AESCrypto.generateKeys());
        String encryptedText = AESCrypto.encryptString(getText(), keys);

        return new JSONMessage(
                getGuid(),
                new JSONChat(
                        getChat().getGuid(),
                        getChat().getGroupID(),
                        getChat().getChatIdentifier(),
                        displayName,
                        participants
                ),
                handle,
                attachments,
                new EncryptedText(
                        encryptedText,
                        keys
                ),
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