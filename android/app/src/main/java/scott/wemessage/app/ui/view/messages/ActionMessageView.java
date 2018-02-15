package scott.wemessage.app.ui.view.messages;

import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import scott.wemessage.app.models.messages.ActionMessage;

public class ActionMessageView implements MessageContentType {

    private ActionMessage actionMessage;

    public ActionMessageView(ActionMessage actionMessage){
        this.actionMessage = actionMessage;
    }

    @Override
    public String getId() {
        return actionMessage.getUuid().toString();
    }

    @Override
    public String getText() {
        return actionMessage.getActionText();
    }

    @Override
    public IUser getUser() {
        return new IUser() {
            @Override
            public String getId() {
                return actionMessage.getUuid().toString();
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getAvatar() {
                return null;
            }
        };
    }

    @Override
    public Date getCreatedAt() {
        return actionMessage.getModernDate();
    }
}
