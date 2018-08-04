package scott.wemessage.app.models.users;

import java.util.UUID;

import scott.wemessage.app.weMessage;

public class Account {

    private UUID uuid;
    private String email;
    private String encryptedPassword;

    public Account(){

    }

    public Account(UUID uuid, String email, String encryptedPassword){
        this.uuid = uuid;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Handle getHandle(){
        return weMessage.get().getMessageDatabase().getHandleByHandleID(getEmail());
    }

    public Account setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    public Account setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }
}