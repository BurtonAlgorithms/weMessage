package scott.wemessage.app.models.sms.messages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import scott.wemessage.app.connection.ConnectionHandler;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.messages.Message;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.types.FailReason;
import scott.wemessage.commons.types.MessageEffect;
import scott.wemessage.commons.utils.DateUtils;

public class MmsMessage extends Message {

    public MmsMessage(String identifier, Chat chat, Handle sender, List<Attachment> attachments, String text, Date dateSent, Date dateDelivered, Boolean errored, Boolean isDelivered, Boolean isFromMe){
        super(identifier, "", chat, sender, attachments, text, DateUtils.convertDateTo2001Time(dateSent), DateUtils.convertDateTo2001Time(dateDelivered), null, errored, true, isDelivered, false, true, isFromMe, MessageEffect.NONE, true);
    }

    @Override
    public String getMacGuid() {
        return "";
    }

    @Override
    public HashMap<Attachment, FailReason> getFailedAttachments() {
        return new HashMap<>();
    }

    @Override
    public Long getDateRead() {
        return null;
    }

    @Override
    public Boolean isRead() {
        return false;
    }

    @Override
    public Boolean isSent() {
        return true;
    }

    @Override
    public Boolean isFinished() {
        return true;
    }

    @Override
    public MessageEffect getMessageEffect() {
        return null;
    }

    @Override
    public Boolean getEffectFinished() {
        return true;
    }

    @Override
    public MmsMessage setMacGuid(String macGuid) {
        return this;
    }

    @Override
    public MmsMessage setDateRead(Long dateRead) {
        return this;
    }

    @Override
    public MmsMessage setRead(Boolean read) {
        return this;
    }

    @Override
    public Message setIsSent(Boolean sent) {
        return this;
    }

    @Override
    public MmsMessage setFinished(Boolean finished) {
        return this;
    }

    @Override
    public MmsMessage setMessageEffect(MessageEffect messageEffect) {
        return this;
    }

    @Override
    public MmsMessage setEffectFinished(Boolean effectFinished) {
        return this;
    }

    @Override
    public JSONMessage toJson(ConnectionHandler connectionHandler) throws IOException, GeneralSecurityException {
        return null;
    }
}