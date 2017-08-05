package scott.wemessage.app.ui.view.messages;

import android.net.Uri;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.R;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

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
        try {
            if (contact.getContactPictureFileLocation() == null) {
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            } else if (StringUtils.isEmpty(contact.getContactPictureFileLocation().getFileLocation())) {
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            } else {
                return Uri.fromFile(contact.getContactPictureFileLocation().getFile()).toString();
            }
        }catch (Exception ex){
            return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
        }
    }
}