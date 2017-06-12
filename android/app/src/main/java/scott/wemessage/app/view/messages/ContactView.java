package scott.wemessage.app.view.messages;

import android.net.Uri;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.commons.utils.StringUtils;

public class ContactView implements IUser {

    private Contact contact;

    public ContactView(Contact contact){
        this.contact = contact;
    }

    @Override
    public String getId() {
        return contact.getUuid().toString();
    }

    @Override
    public String getName() {
        String fullString = "";

        if (!StringUtils.isEmpty(contact.getFirstName())){
            fullString = contact.getFirstName();
        }

        if (!StringUtils.isEmpty(contact.getLastName())){
            fullString += " " + contact.getLastName();
        }

        if (StringUtils.isEmpty(fullString)){
            fullString = contact.getHandle().getHandleID();
        }

        return fullString;
    }

    @Override
    public String getAvatar() {
        return Uri.fromFile(contact.getContactPictureFileLocation().getFile()).toString();
    }
}