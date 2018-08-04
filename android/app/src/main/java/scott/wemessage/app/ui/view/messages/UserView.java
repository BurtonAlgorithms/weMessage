package scott.wemessage.app.ui.view.messages;

import com.stfalcon.chatkit.commons.models.IUser;

import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.utils.IOUtils;

public class UserView implements IUser {

    private Handle handle;

    public UserView(Handle handle){
        this.handle = handle;
    }

    @Override
    public String getId() {
        try {
            return handle.getUuid().toString();
        }catch(Exception ex){
            return "";
        }
    }

    @Override
    public String getName() {
        return handle.getDisplayName();
    }

    @Override
    public String getAvatar() {
        return IOUtils.getContactIconUri(handle, IOUtils.IconSize.NORMAL);
    }
}