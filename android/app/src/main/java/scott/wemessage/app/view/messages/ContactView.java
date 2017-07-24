package scott.wemessage.app.view.messages;

import android.net.Uri;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.R;
import scott.wemessage.app.messages.MessageManager;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.WeApp;
import scott.wemessage.commons.utils.StringUtils;

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
        try {
            String fullString = "";

            if (!StringUtils.isEmpty(contact.getFirstName())) {
                fullString = contact.getFirstName();
            }

            if (!StringUtils.isEmpty(contact.getLastName())) {
                fullString += " " + contact.getLastName();
            }

            if (StringUtils.isEmpty(fullString)) {
                fullString = contact.getHandle().getHandleID();
            }

            return fullString;
        }catch(Exception ex){
            return "";
        }
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