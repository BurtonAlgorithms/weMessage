package scott.wemessage.app.models.messages;

import java.util.Date;
import java.util.UUID;

import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.commons.utils.DateUtils;

public class ActionMessage extends MessageBase {

    private UUID uuid;
    private Chat chat;
    private String actionText;
    private Long date;

    public ActionMessage(){

    }

    public ActionMessage(UUID uuid, Chat chat, String actionText, Long date){
        this.uuid = uuid;
        this.chat = chat;
        this.actionText = actionText;
        this.date = date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Chat getChat() {
        return chat;
    }

    public String getActionText() {
        return actionText;
    }

    public Long getDate() {
        return date;
    }

    @Override
    public Long getTimeIdentifier() {
        return date;
    }

    public Date getModernDate(){
        if (date == null || date == -1) return null;

        return DateUtils.getDateUsing2001(date);
    }

    public ActionMessage setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ActionMessage setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    public ActionMessage setActionText(String actionText) {
        this.actionText = actionText;
        return this;
    }

    public ActionMessage setDate(Long date) {
        this.date = date;
        return this;
    }
}