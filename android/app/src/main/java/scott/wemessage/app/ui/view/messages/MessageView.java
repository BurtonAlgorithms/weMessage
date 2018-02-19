package scott.wemessage.app.ui.view.messages;

import com.stfalcon.chatkit.commons.models.IMessage;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

import scott.wemessage.app.models.messages.Message;
import scott.wemessage.commons.utils.StringUtils;

public class MessageView implements IMessage {

    private Message message;
    private String overrideText;

    public MessageView(Message message){
        this.message = message;
    }

    public Message getMessage(){
        return message;
    }

    @Override
    public String getId() {
        return message.getIdentifier();
    }

    @Override
    public String getText() {
        if (!StringUtils.isEmpty(overrideText)) return overrideText;

        try {
            return StringUtils.trimORC(message.getText());
        }catch(Exception ex){
            return null;
        }
    }

    @Override
    public UserView getUser() {
        try {
            return new UserView(message.getSender());
        }catch (Exception ex){
            return new UserView(null);
        }
    }

    @Override
    public Date getCreatedAt() {
        try {
            Date date = message.getModernDateSent();
            if (date == null || new DateTime(date.getTime()).getYear() < 2000) return message.getModernDateDelivered();

            return date;
        }catch(Exception ex){
            return Calendar.getInstance().getTime();
        }
    }

    public void setText(String text){
        this.overrideText = text;
    }

    public boolean hasErrored(){
        return message.hasErrored();
    }
}