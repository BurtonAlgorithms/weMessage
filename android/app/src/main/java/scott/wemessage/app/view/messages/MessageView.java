package scott.wemessage.app.view.messages;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Message;

public class MessageView implements IMessage {

    private MessageManager messageManager;
    private Message message;

    //TODO: Attachments

    public MessageView(MessageManager messageManager, Message message){
        this.messageManager = messageManager;
        this.message = message;
    }

    @Override
    public String getId() {
        return message.getUuid().toString();
    }

    @Override
    public String getText() {
        return message.getText();
    }

    @Override
    public IUser getUser() {
        return new ContactView(messageManager, message.getSender());
    }

    @Override
    public Date getCreatedAt() {
        return message.getModernDateSent();
    }
}