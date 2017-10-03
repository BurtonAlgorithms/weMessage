package scott.wemessage.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class ServerLogger {

    private static MessageServer messageServer;

    private ServerLogger() { }

    public static void setServerHook(MessageServer server){
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
            logToFile(level.getPrefix() + " " + message);
            return;
        }

        logToFile(level.getPrefix() + " [" + prefix + "] " + message);
    }

    public static void error(String message, Exception ex){
        error(null, message, ex);
    }

    public static void error(String prefix, String message, Exception ex){
        if(prefix == null) {
            System.out.println(Level.ERROR.getPrefix() + " " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile(Level.ERROR.getPrefix() + " " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
            logToFile(" ");
        }else {
            System.out.println(Level.ERROR.getPrefix() + " [" + prefix + "] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile(Level.ERROR.getPrefix() + " [" + prefix + "] " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
            logToFile(" ");
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
    }
}
