package scott.wemessage.app.view.messages;

import android.net.Uri;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.R;
import scott.wemessage.app.WeApp;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.AndroidIOUtils;

public class ContactView implements IUser {

    private MessageManager messageManager;
    private Contact contact;

    public ContactView(MessageManager messageManager, Contact contact){
        this.messageManager = messageManager;
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
                return AndroidIOUtils.getUriFromResource(WeApp.get(), R.drawable.ic_user_icon_v2).toString();
            }
            return Uri.fromFile(contact.getContactPictureFileLocation().getFile()).toString();
        }catch(Exception ex){
            return AndroidIOUtils.getUriFromResource(WeApp.get(), R.drawable.ic_user_icon_v2).toString();
        }
    }
}