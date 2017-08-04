package scott.wemessage.app.view.messages;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Calendar;
import java.util.Date;

import scott.wemessage.app.messages.objects.Message;

public class MessageView implements IMessage {

    private Message message;

    public MessageView(Message message){
        this.message = message;
    }

    public Message getMessage(){
        return message;
    }

    @Override
    public String getId() {
        return message.getUuid().toString();
    }

    @Override
    public String getText() {
        try {
            return trimORC(message);
        }catch(Exception ex){
            return null;
        }
    }

    @Override
    public ContactView getUser() {
        try {
            return new ContactView(message.getSender());
        }catch (Exception ex){
            return new ContactView(null);
        }
    }

    @Override
    public Date getCreatedAt() {
        try {
            return message.getModernDateSent();
        }catch(Exception ex){
            return Calendar.getInstance().getTime();
        }
    }

    private String trimORC(Message message){
        int attachmentCount = message.getAttachments().size();

        if (attachmentCount < 1){
            return message.getText();
        }

        return message.getText().substring(attachmentCount, message.getText().length() - 1);
    }
}