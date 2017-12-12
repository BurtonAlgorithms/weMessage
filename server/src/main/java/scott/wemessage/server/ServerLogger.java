package scott.wemessage.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.configuration.ServerConfiguration;
import scott.wemessage.server.utils.SentryConfig;
import scott.wemessage.server.utils.SentryEventHelper;

public final class ServerLogger {

    private static boolean USE_SENTRY = true;
    private static final long MINIMUM_INTERVAL = 30 * 60 * 1000;

    private static MessageServer messageServer;
    private static boolean isSentryInitialized = false;
    private static Long lastSentryExecution;

    private ServerLogger() { }

    static void setServerHook(MessageServer server, ServerConfiguration serverConfiguration){
        try {
            if (USE_SENTRY && serverConfiguration.getConfigJSON().getConfig().getSendCrashReports()) {
                Sentry.init(new SentryConfig(weMessage.SENTRY_DSN, weMessage.WEMESSAGE_VERSION, "production").build());
                Sentry.getStoredClient().addBuilderHelper(new SentryEventHelper());

                isSentryInitialized = true;
            }
        } catch (Exception ex){
            isSentryInitialized = false;
        }

        messageServer = server;
    }

    public static void log(String message){
        log(null, null, message);
    }

    public static void log(Level level, String message){
        log(level, null, message);
    }

    public static void log(String prefix, String message){
        log(null, prefix, message);
    }
    
    public static void log(Level level, String prefix, String message){
        if (level == null){
            if (prefix == null){
                performLog(message);
            }else {
                performLog("[" + prefix + "] " + message);
            }
            return;
        }

        if (prefix == null){
            performLog(level.getPrefix() + " " + message);
            return;
        }

        performLog(level.getPrefix() + " [" + prefix + "] " + message);
    }

    public static void error(String message, Exception ex){
        error(null, message, ex);
    }

    public static void error(String prefix, String message, Exception ex){
        error(prefix, message, ex, true);
    }

    public static void error(String message, Exception ex, boolean withSentry){
        error(null, message, ex, withSentry);
    }

    public static void error(String prefix, String message, Exception ex, boolean withSentry){
        if(prefix == null) {
            System.out.println(Level.ERROR.getPrefix() + " " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile(Level.ERROR.getPrefix() + " " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
            logToFile(" ");

            if (withSentry) {
                logToSentry(message, ex);
            }
        }else {
            System.out.println(Level.ERROR.getPrefix() + " [" + prefix + "] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile(Level.ERROR.getPrefix() + " [" + prefix + "] " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
            logToFile(" ");

            if (withSentry) {
                logToSentry(prefix, message, ex);
            }
        }
    }

    public static void emptyLine(){
        performLog(" ");
    }

    private static void performLog(String text){
        System.out.println(text);
        logToFile(text);
    }

    private static void logToFile(String text){
        if (messageServer != null && messageServer.getConfiguration() != null) {
            if (messageServer.getConfiguration().saveLogFiles()) {
                try {
                    if (text.equals(" ") || text.equals("")){
                        Files.write(messageServer.getConfiguration().getLogFile().toPath(), ("\n" + text).getBytes(), StandardOpenOption.APPEND);
                    }else {
                        Files.write(messageServer.getConfiguration().getLogFile().toPath(), ("\n[" + getCurrentTimeStamp() + "] " + text).getBytes(), StandardOpenOption.APPEND);
                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void logToSentry(Level level, String message){
        logToSentry(level, null, message, null);
    }

    private static void logToSentry(String message, Exception ex){
        logToSentry(null, null, message, ex);
    }

    private static void logToSentry(Level level, String message, Exception ex){
        logToSentry(level, null, message, ex);
    }

    private static void logToSentry(String tag, String message, Exception ex){
        logToSentry(null, tag, message, ex);
    }

    private static void logToSentry(Level level, String tag, String message, Exception ex){
        if (!isSentryInitialized) return;

        Long previousTime = lastSentryExecution;
        long currentTimestamp = System.currentTimeMillis();

        lastSentryExecution = currentTimestamp;

        if (previousTime == null || (currentTimestamp - previousTime > MINIMUM_INTERVAL)){
            EventBuilder eventBuilder = new EventBuilder();

            if (level != null){
                eventBuilder.withLevel(level.sentryLevel());
            }

            if (!StringUtils.isEmpty(tag)){
                eventBuilder.withLogger(tag);
            }

            if (!StringUtils.isEmpty(message)){
                eventBuilder.withMessage(message);
            }

            if (ex != null) {
                eventBuilder.withSentryInterface(new ExceptionInterface(ex));
            }

            Sentry.capture(eventBuilder);
        }
    }

    private static String getCurrentTimeStamp(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        return sdfDate.format(now);
    }

    private static String getStackTrace(Exception ex){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        ex.printStackTrace(printStream);
        printStream.close();
        return outputStream.toString();
    }

    public enum Level {
        INFO(0),
        WARNING(1),
        SEVERE(2),
        ERROR(3);

        int value;

        Level(int value){
            this.value = value;
        }

        public String getPrefix() {
            switch (value){
                case 0:
                    return "[INFO]";
                case 1:
                    return "[! WARNING !]";
                case 2:
                    return "[!! SEVERE !!]";
                case 3:
                    return "[!!! ERROR !!!]";
                default:
                    return "[LOG]";
            }
        }

        public Event.Level sentryLevel(){
            switch (value){
                case 0:
                    return Event.Level.INFO;
                case 1:
                    return Event.Level.WARNING;
                case 2:
                    return Event.Level.ERROR;
                case 3:
                    return Event.Level.ERROR;
                default:
                    return Event.Level.DEBUG;
            }
        }
    }
}