package scott.wemessage.app.ui.view.messages;

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
            return trimORC(message.getText());
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

    private String trimORC(String string){
        char[] chars = string.toCharArray();
        String finalString = "";

        for (char c : chars){
            String hex = String.format("%04x", (int) c);

            if (!hex.equals("fffc")){
                finalString += c;
            }
        }
        return finalString;
    }
}