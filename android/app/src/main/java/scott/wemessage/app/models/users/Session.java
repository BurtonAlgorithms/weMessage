package scott.wemessage.app.models.users;

import java.util.UUID;

public class Session {

    private Account account;
    private Handle smsHandle;

    public Session(){

    }

    public Account getAccount() {
        return account;
    }

    public Handle getSmsHandle() {
        return smsHandle;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setSmsHandle(Handle smsHandle) {
        this.smsHandle = smsHandle;
    }

    public boolean isMe(ContactInfo contactInfo){
        if (getAccount() != null && getAccount().getHandle().equals(contactInfo)) return true;
        else if (getSmsHandle() != null && getSmsHandle().equals(contactInfo)) return true;
        else return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof ContactInfo){
            ContactInfo contact = (ContactInfo) obj;

            if (getAccount() != null && getAccount().getHandle().equals(contact)) return true;
            if (getSmsHandle() != null && getSmsHandle().equals(contact)) return true;
        }

        if (obj instanceof Account){
            Account account = (Account) obj;

            if (getAccount() != null && getAccount().getEmail().equals(account.getEmail())) return true;
        }

        if (obj instanceof UUID){
            UUID uuid = (UUID) obj;

            if (getAccount() != null && getAccount().getUuid().toString().equals(uuid.toString())) return true;
        }

        return false;
    }
}