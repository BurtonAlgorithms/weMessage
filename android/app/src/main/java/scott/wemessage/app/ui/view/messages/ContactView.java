package scott.wemessage.app.ui.view.messages;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.AndroidIOUtils;

public class ContactView implements IUser {

    private Contact contact;

    public ContactView(Contact contact){
        this.contact = contact;
    }

    @Override
    public String getId() {
        try {
            return contact.getUuid().toString();
        }catch(Exception ex){
            return "";
        }
    }

    @Override
    public String getName() {
        return contact.getUIDisplayName();
    }

    @Override
    public String getAvatar() {
        return AndroidIOUtils.getContactIconUri(contact);
    }
}