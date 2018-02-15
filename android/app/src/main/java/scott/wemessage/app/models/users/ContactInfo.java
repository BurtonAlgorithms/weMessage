package scott.wemessage.app.models.users;

import java.util.UUID;

public abstract class ContactInfo {

    public abstract UUID getUuid();

    public abstract String getDisplayName();

    public abstract ContactInfo findRoot();

    public abstract Handle pullHandle(boolean iMessage);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof Handle){
            Handle objectHandle = (Handle) obj;

            if (this instanceof Handle){
                if (objectHandle.getHandleID().equals(((Handle) this).getHandleID())) return true;
            }else if (this instanceof Contact){
                for (Handle h : ((Contact) this).getHandles()){
                    if (h.getHandleID().equals(objectHandle.getHandleID())) return true;
                }
            }
        }else if (obj instanceof Contact){
            Contact objectContact = (Contact) obj;

            if (this instanceof Handle){
                for (Handle h : objectContact.getHandles()){
                    if (h.getHandleID().equals(((Handle) this).getHandleID())) return true;
                }
            }else if (this instanceof Contact){
                if (objectContact.getUuid().toString().equals(getUuid().toString())) return true;
            }
        }
        return false;
    }
}