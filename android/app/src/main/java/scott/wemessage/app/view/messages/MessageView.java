package scott.wemessage.app.view.messages;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import scott.wemessage.app.messages.objects.Message;

public class MessageView implements IMessage {

    private Message message;

    //TODO: Attachments

    public MessageView(Message message){
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
        return new ContactView(message.getSender());
    }

    @Override
    public Date getCreatedAt() {
        return message.getModernDateSent();
    }
}