package scott.wemessage.server.configuration.json;

public class ConfigAccountJSON {

    private String email;
    private String password;
    private String secret;

    public ConfigAccountJSON(String email, String password, String secret){
        this.email = email;
        this.password = password;
        this.secret = secret;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSecret() {
        return secret;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}