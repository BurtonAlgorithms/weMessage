package scott.wemessage.server.configuration;

import scott.wemessage.commons.crypto.BCrypt;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.server.MessageServer;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.configuration.json.ConfigAccountJSON;
import scott.wemessage.server.configuration.json.ConfigJSON;
import scott.wemessage.server.weMessage;

public final class Authenticator {

    private final String TAG = "Authenticator";

    private MessageServer messageServer;
    private ServerConfiguration configuration;

    public Authenticator(MessageServer messageServer, ServerConfiguration configuration){
        this.messageServer = messageServer;
        this.configuration = configuration;
    }

    public boolean isAccountValid() throws Exception {
        return isValidEmail() && isPasswordValid();
    }

    public boolean isPasswordValid() throws Exception {
        ConfigJSON configJSON = configuration.getConfigJSON();
        ConfigAccountJSON accountInfo = configJSON.getConfig().getAccountInfo();
        String storedPassword = accountInfo.getPassword();
        String storedSecret = accountInfo.getSecret();

        if (accountInfo.getPassword().equalsIgnoreCase(weMessage.DEFAULT_PASSWORD) || accountInfo.getSecret().equalsIgnoreCase(weMessage.DEFAULT_SECRET)) {
            return false;
        }
        try {
            BCrypt.hashPassword("test", storedSecret);
        } catch (Exception ex) {
            ServerLogger.log(ServerLogger.Level.ERROR, TAG, "The secret key stored in " + configuration.getConfigFileName() + " is not valid!");
            return false;
        }
        try {
            BCrypt.checkPassword("test", storedPassword);
        } catch (Exception ex) {
            ServerLogger.log(ServerLogger.Level.ERROR, TAG, "The password stored in " + configuration.getConfigFileName() + " is not valid!");
            return false;
        }

        return true;
    }

    public boolean isValidEmail() throws Exception {
        ConfigAccountJSON accountInfo = configuration.getConfigJSON().getConfig().getAccountInfo();
        String email = accountInfo.getEmail();

        if (!isValidEmailFormat(email)){
            return false;
        }

        if (email.equalsIgnoreCase(weMessage.DEFAULT_EMAIL)){
            return false;
        }
        return true;
    }

    public static boolean isValidEmailFormat(String email){
        return AuthenticationUtils.isValidEmailFormat(email);
    }
}