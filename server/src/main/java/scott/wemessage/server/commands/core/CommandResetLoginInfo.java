package scott.wemessage.server.commands.core;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.configuration.json.ConfigJSON;
import scott.wemessage.server.configuration.Authenticator;
import scott.wemessage.commons.crypto.BCrypt;
import scott.wemessage.server.utils.LoggingUtils;
import scott.wemessage.server.weMessage;

import java.util.Scanner;

public class CommandResetLoginInfo extends CoreCommand {

    public CommandResetLoginInfo(CommandManager manager){
        super(manager, "resetlogininfo", "Resets the login information needed for weMessage Clients to connect to Server.", new String[]{ "newpassword", "resetpassword", "resetlogin", "resetpass", "resetaccountinfo", "newlogin" });
    }

    public void execute(String[] args){
        LoggingUtils.log("In order to change your account login information, please enter the password you are currently using.");

        ServerConfiguration configuration = getCommandManager().getMessageServer().getConfiguration();
        ConfigJSON configJSON;
        boolean isEmailNotAuthenticated = true;
        boolean isPasswordNotAuthenticated = true;
        boolean hasNotProvidedPastPassword = true;
        boolean pastPasscodeWrong = false;

        try {
            configJSON = configuration.getConfigJSON();
        }catch (Exception ex){
            LoggingUtils.error("Could not get previous account login info.", ex);
            return;
        }

        Scanner lastPassScanner = new Scanner(System.in);

        while(hasNotProvidedPastPassword){
            String pastPassword = lastPassScanner.nextLine();
            if (!BCrypt.checkPassword(pastPassword, configJSON.getConfig().getAccountInfo().getPassword())){
                LoggingUtils.log("The password entered does not match the current password. Exiting configuration.");
                hasNotProvidedPastPassword = false;
                pastPasscodeWrong = true;
            }else {
                hasNotProvidedPastPassword = false;
            }
        }

        if(pastPasscodeWrong) return;

        Scanner emailScanner = new Scanner(System.in);

        LoggingUtils.emptyLine();
        LoggingUtils.log("Please enter a new email for devices to connect with!");
        LoggingUtils.log("Your email must be the same as the one you are using iMessage with.");
        LoggingUtils.emptyLine();

        while (isEmailNotAuthenticated){
            String email = emailScanner.nextLine();

            if (!Authenticator.isValidEmailFormat(email) || email.equalsIgnoreCase(weMessage.DEFAULT_EMAIL)) {
                LoggingUtils.log("The email you provided is not a valid address.");
                LoggingUtils.emptyLine();
            } else {
                try {
                    configJSON.getConfig().getAccountInfo().setEmail(email);
                    isEmailNotAuthenticated = false;
                } catch (Exception ex) {
                    LoggingUtils.error("An error occurred while trying to set a login email address. Shutting down!", ex);
                    getCommandManager().getMessageServer().shutdown(-1, false);
                    return;
                }
            }
        }

        LoggingUtils.emptyLine();
        LoggingUtils.log("Please enter a new password for devices to connect with!");
        LoggingUtils.emptyLine();

        Scanner passwordScanner = new Scanner(System.in);

        while (isPasswordNotAuthenticated) {
            String password = passwordScanner.nextLine();

            if (password.length() < weMessage.MINIMUM_CONNECT_PASSWORD_LENGTH) {
                LoggingUtils.log("The password you have provided is too short.");
                LoggingUtils.log("Please provide a password that is at least " + weMessage.MINIMUM_CONNECT_PASSWORD_LENGTH + " characters in length.");
                LoggingUtils.emptyLine();
            } else {
                try {
                    String secret = BCrypt.generateSalt();
                    String passwordHash = BCrypt.hashPassword(password, secret);

                    configJSON.getConfig().getAccountInfo().setSecret(secret);
                    configJSON.getConfig().getAccountInfo().setPassword(passwordHash);

                    configuration.writeJsonToConfig(configJSON);
                    isPasswordNotAuthenticated = false;
                    LoggingUtils.log("Password successfully updated.");
                } catch (Exception ex) {
                    LoggingUtils.error("An error occurred while trying to set a password. Shutting down!", ex);
                    getCommandManager().getMessageServer().shutdown(-1, false);
                }
            }
        }
    }
}