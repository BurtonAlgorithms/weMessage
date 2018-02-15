package scott.wemessage.app.models.sms.chats;

import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.users.Handle;

public class SmsPeerChat extends PeerChat implements SmsChat {

    public SmsPeerChat(String identifier, Handle handle, boolean hasUnreadMessages){
        super(identifier, "", "", "", true, hasUnreadMessages, handle);
    }

    @Override
    public String getMacGuid() {
        return "";
    }

    @Override
    public String getMacGroupID() {
        return "";
    }

    @Override
    public String getMacChatIdentifier() {
        return "";
    }

    @Override
    public boolean isInChat() {
        return true;
    }

    @Override
    public SmsPeerChat setMacGuid(String macGuid) {
        return this;
    }

    @Override
    public SmsPeerChat setMacGroupID(String macGroupID) {
        return this;
    }

    @Override
    public SmsPeerChat setMacChatIdentifier(String macChatIdentifier) {
        return this;
    }

    @Override
    public SmsPeerChat setIsInChat(boolean isInChat) {
        return this;
    }
}