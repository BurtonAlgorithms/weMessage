package scott.wemessage.app.database.objects;

import java.util.UUID;

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