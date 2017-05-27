package scott.wemessage.server;

import scott.wemessage.commons.crypto.BCrypt;
import scott.wemessage.commons.utils.AuthenticationUtils;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.commands.AppleScriptExecutor;
import scott.wemessage.server.commands.Command;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.configuration.Authenticator;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.configuration.json.ConfigJSON;
import scott.wemessage.server.connection.DeviceManager;
import scott.wemessage.server.database.DatabaseManager;
import scott.wemessage.server.database.MessagesDatabase;
import scott.wemessage.server.events.EventManager;
import scott.wemessage.server.listeners.connection.ClientMessageReceivedListener;
import scott.wemessage.server.listeners.connection.DeviceJoinListener;
import scott.wemessage.server.listeners.connection.DeviceQuitListener;
import scott.wemessage.server.listeners.database.ErrorWatcher;
import scott.wemessage.server.listeners.database.MessagesDatabaseListener;

import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MessageServer {

    public final String TAG = "weServer";

    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private ServerConfiguration serverConfiguration;
    private Authenticator authenticator;
    private DatabaseManager databaseManager;
    private MessagesDatabase messagesDatabase;
    private CommandManager commandManager;
    private DeviceManager deviceManager;
    private EventManager eventManager;
    private AppleScriptExecutor appleScriptExecutor;

    private final Object databaseManagerLock = new Object();
    private final Object messageDatabaseLock = new Object();
    private final Object deviceManagerLock = new Object();
    private final Object eventManagerLock = new Object();
    private final Object serverConfigurationLock = new Object();
    private final Object scriptExecutorLock = new Object();

    protected MessageServer() {
        if (init()) {
            try {
                this.serverConfiguration = new ServerConfiguration(this);
                this.authenticator = new Authenticator(this, serverConfiguration);
                this.appleScriptExecutor = new AppleScriptExecutor(this, serverConfiguration);
                this.databaseManager = new DatabaseManager(this, serverConfiguration);
                this.messagesDatabase = new MessagesDatabase(this, databaseManager);
                this.commandManager = new CommandManager(this);
                this.deviceManager = new DeviceManager(this);
                this.eventManager = new EventManager(this);

                isRunning.set(true);
            } catch (Exception e) {
                ServerLogger.error(TAG, "An error occurred while initializing MessageServer. Shutting down!", e);
                shutdown(-1, false);
            }
        }else {
            System.exit(-1);
        }
    }

    public DatabaseManager getDatabaseManager() {
        synchronized (databaseManagerLock){
            return databaseManager;
        }
    }

    public MessagesDatabase getMessagesDatabase(){
        synchronized (messageDatabaseLock) {
            return messagesDatabase;
        }
    }

    public DeviceManager getDeviceManager(){
        synchronized (deviceManagerLock) {
            return deviceManager;
        }
    }

    public EventManager getEventManager(){
        synchronized (eventManagerLock){
            return eventManager;
        }
    }

    public ServerConfiguration getConfiguration() {
        synchronized (serverConfigurationLock) {
            return serverConfiguration;
        }
    }

    public AppleScriptExecutor getScriptExecutor(){
        synchronized (scriptExecutorLock) {
            return appleScriptExecutor;
        }
    }

    void launch() {
        if (!getScriptExecutor().isSetup()){
            ServerLogger.log(ServerLogger.Level.ERROR, TAG, "weMessage Server is not configured to run yet.");
            ServerLogger.log(ServerLogger.Level.ERROR, TAG, "Make sure that assistive access is enabled!");
            shutdown(-1, true);
            return;
        }

        try {
            if (!authenticator.isAccountValid()) {
                synchronized (serverConfigurationLock) {
                    ServerLogger.log("The email and password provided in " + serverConfiguration.getConfigFileName() + " are invalid!");
                    ServerLogger.emptyLine();
                    ServerLogger.log("Please enter a new email and password for devices to connect with!");
                    ServerLogger.log("Your email must be the same as the one you are using iMessage with.");
                    ServerLogger.emptyLine();

                    boolean isEmailNotAuthenticated = true;
                    Scanner emailScanner = new Scanner(System.in);

                    while (isEmailNotAuthenticated){
                        String email = emailScanner.nextLine();

                        if (!Authenticator.isValidEmailFormat(email) || email.equalsIgnoreCase(weMessage.DEFAULT_EMAIL)) {
                            ServerLogger.log("The email you provided is not a valid address.");
                            ServerLogger.emptyLine();
                        } else {
                            try {
                                ConfigJSON configJSON = serverConfiguration.getConfigJSON();
                                configJSON.getConfig().getAccountInfo().setEmail(email);

                                serverConfiguration.writeJsonToConfig(configJSON);
                                isEmailNotAuthenticated = false;
                            } catch (Exception ex) {
                                ServerLogger.error("An error occurred while trying to set a login email address. Shutting down!", ex);
                                shutdown(-1, false);
                            }
                        }
                    }

                    ServerLogger.emptyLine();
                    ServerLogger.log("Please enter a new password for devices to connect with!");
                    ServerLogger.emptyLine();

                    Scanner passwordScanner = new Scanner(System.in);
                    boolean isPasswordNotAuthenticated = true;

                    while (isPasswordNotAuthenticated) {
                        String password = passwordScanner.nextLine();
                        AuthenticationUtils.PasswordValidateType validateType = AuthenticationUtils.isValidPasswordFormat(password);

                        if (validateType == AuthenticationUtils.PasswordValidateType.LENGTH_TOO_SMALL){
                            ServerLogger.log("The password you have provided is too short.");
                            ServerLogger.log("Please provide a password that is at least " + weMessage.MINIMUM_PASSWORD_LENGTH + " characters in length.");
                            ServerLogger.emptyLine();
                            continue;
                        }

                        if (validateType == AuthenticationUtils.PasswordValidateType.PASSWORD_TOO_EASY){
                            ServerLogger.log("The password you have provided is too easy to guess.");
                            ServerLogger.log("Please provide a more complex password.");
                            ServerLogger.emptyLine();
                            continue;
                        }

                        if (validateType == AuthenticationUtils.PasswordValidateType.MUST_HAVE_DIGITS_AND_LETTERS){
                            ServerLogger.log("Make sure your password contains both numbers and letters.");
                            ServerLogger.emptyLine();
                            continue;
                        }

                        if (validateType == AuthenticationUtils.PasswordValidateType.VALID) {
                            try {
                                String secret = BCrypt.generateSalt();
                                String passwordHash = BCrypt.hashPassword(password, secret);

                                ConfigJSON configJSON = serverConfiguration.getConfigJSON();
                                configJSON.getConfig().getAccountInfo().setSecret(secret);
                                configJSON.getConfig().getAccountInfo().setPassword(passwordHash);

                                serverConfiguration.writeJsonToConfig(configJSON);
                                isPasswordNotAuthenticated = false;
                            } catch (Exception ex) {
                                ServerLogger.error("An error occurred while trying to set a password. Shutting down!", ex);
                                shutdown(-1, false);
                            }
                        }
                    }
                }
            }
        }catch(Exception ex){
            ServerLogger.error(TAG, "There was an error starting the server. Shutting down!", ex);
            shutdown(-1, false);
            return;
        }

        try {
            ServerLogger.log(ServerLogger.Level.INFO, "weMessage Server", "Starting weMessage Server on port " + getConfiguration().getPort());

            synchronized (databaseManagerLock) {
                databaseManager.start();
            }
            synchronized (messageDatabaseLock) {
                messagesDatabase.start();
            }
            synchronized (deviceManagerLock){
                deviceManager.start();
            }
            synchronized (scriptExecutorLock){
                appleScriptExecutor.start();
            }
            synchronized (eventManagerLock){
                eventManager.start();
                eventManager.registerListener(new ErrorWatcher());
                eventManager.registerListener(new MessagesDatabaseListener());
                eventManager.registerListener(new DeviceJoinListener());
                eventManager.registerListener(new DeviceQuitListener());
                eventManager.registerListener(new ClientMessageReceivedListener());
            }
            commandManager.startService();

            Thread.sleep(500);
            ServerLogger.emptyLine();
            ServerLogger.log("weServer Started!");
            ServerLogger.log("Version: " + getConfiguration().getVersion());
            ServerLogger.emptyLine();
        } catch(Exception e){
            ServerLogger.error(TAG, "There was an error starting the server. Shutting down!", e);
            shutdown(-1, false);
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while(isRunning.get()){
            String commandString = scanner.nextLine();
            Command command;
            String[] commandArgs = {};

            try {
                String[] nameAndArgs = commandString.split(" ", 2);
                List<String> commandArgsList = StringUtils.getStringListFromString(nameAndArgs[1]);
                commandArgs = commandArgsList.toArray(new String[commandArgsList.size()]);
                command = commandManager.getCommand(nameAndArgs[0]);
            } catch(Exception ex){
                command = commandManager.getCommand(commandString);
            }
            if(command != null) {
                try {
                    ServerLogger.emptyLine();
                    command.execute(commandArgs);

                    if (!command.getName().equals("stop")) {
                        ServerLogger.emptyLine();
                    }
                }catch(Exception ex){
                    ServerLogger.error(TAG, "An error occurred while running the command " + command.getName() + "!", ex);
                }
            }else {
                ServerLogger.emptyLine();
                ServerLogger.log(ServerLogger.Level.WARNING, TAG, "Command " + commandString.split(" ")[0] + " was not found! Are you sure you typed it in correctly?");
                ServerLogger.emptyLine();
            }
        }
    }

    private boolean init(){
        ServerLogger.emptyLine();

        if(!System.getProperty("os.name").toLowerCase().startsWith("mac")){
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "weServer can only run on macOS. Shutting down!");
            shutdown(-1, false);
            return false;
        }

        if(!System.getProperty("os.version").startsWith("10.12")){
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "As of now, weServer only supports macOS Sierra and higher. Shutting down!");
            shutdown(-1, false);
            return false;
        }

        System.setProperty("apple.awt.UIElement", "true");
        isInitialized.set(true);
        return true;
    }

    public synchronized void shutdown(int returnCode, boolean showCloseMessage){
        if(showCloseMessage) {
            ServerLogger.log(ServerLogger.Level.INFO, TAG, "weServer is shutting down. Goodbye!");
        }
        isRunning.set(false);
        try {
            if (isInitialized.get()) {
                databaseManager.stopService();
                messagesDatabase.stopService();
                commandManager.stopService();
                deviceManager.stopService();
                appleScriptExecutor.stopService();
                eventManager.stopService();
                isInitialized.set(false);
            }
            ServerLogger.emptyLine();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(returnCode);
                }
            }, 2000);
        }catch(Exception ex){
            ServerLogger.error(TAG, "An error occurred while shutting down the server.", ex);
            System.exit(returnCode);
        }
    }
}