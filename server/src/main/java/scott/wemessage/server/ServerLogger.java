package scott.wemessage.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
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

    public static void log(String prefix, String message){
        log(null, prefix, message);
    }

    public static void log(Level level, String prefix, String message){
        if (level == null){
            if(prefix == null) {
                System.out.println(message);
                logToFile(message);
            }else {
                System.out.println("[" + prefix + "] " + message);
                logToFile("[" + prefix + "] " + message);
            }
            return;
        }

        switch(level){
            case INFO:
                System.out.println("[INFO] [" + prefix + "] " + message);
                logToFile("[INFO] [" + prefix + "] " + message);
                break;
            case WARNING:
                System.out.println("[WARNING!] [" + prefix + "] " + message);
                logToFile("[WARNING!] [" + prefix + "] " + message);
                break;
            case SEVERE:
                System.out.println("[!! SEVERE !!] [" + prefix + "] " + message);
                logToFile("[!! SEVERE !!] [" + prefix + "] " + message);
                break;
            case ERROR:
                System.out.println("[!!! ERROR !!!] [" + prefix + "] " + message);
                logToFile("[!!! ERROR !!!] [" + prefix + "] " + message);
                break;
            default:
                if(prefix == null) {
                    System.out.println(message);
                    logToFile(message);
                }else {
                    System.out.println("[LOG] [" + prefix + "] " + message);
                    logToFile("[LOG] [" + prefix + "] " + message);
                }
        }
    }

    public static void error(String prefix, String message, Exception ex){
        if(prefix == null) {
            System.out.println("[!!! ERROR !!!] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile("[!!! ERROR !!!] " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
        }else {
            System.out.println("[!!! ERROR !!!] [" + prefix + "] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");

            logToFile("[!!! ERROR !!!] [" + prefix + "] " + message);
            logToFile(" ");
            logToFile(getStackTrace(ex));
        }
    }

    public static void error(String message, Exception ex){
        error(null, message, ex);
    }

    public static void emptyLine(){
        System.out.println(" ");
    }

    private static void logToFile(String text){
        if (messageServer != null){
            if (messageServer.getConfiguration().saveLogFiles()){
                try(PrintWriter out = new PrintWriter(weMessage.LOG_FILE_NAME)){
                    out.println("[ " + getCurrentTimeStamp() + " ] " + text);
                }catch (Exception ex){
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
        INFO,
        WARNING,
        SEVERE,
        ERROR
    }
}
