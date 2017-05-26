package scott.wemessage.server;

public final class ServerLogger {

    private ServerLogger() { }

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
            }else {
                System.out.println("[" + prefix + "] " + message);
            }
            return;
        }

        switch(level){
            case INFO:
                System.out.println("[INFO] [" + prefix + "] " + message);
                break;
            case WARNING:
                System.out.println("[WARNING!] [" + prefix + "] " + message);
                break;
            case SEVERE:
                System.out.println("[!! SEVERE !!] [" + prefix + "] " + message);
                break;
            case ERROR:
                System.out.println("[!!! ERROR !!!] [" + prefix + "] " + message);
                break;
            default:
                if(prefix == null) {
                    System.out.println(message);
                }else {
                    System.out.println("[LOG] [" + prefix + "] " + message);
                }
        }
    }

    public static void error(String prefix, String message, Exception ex){
        if(prefix == null) {
            System.out.println("[!!! ERROR !!!] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");
        }else {
            System.out.println("[!!! ERROR !!!] [" + prefix + "] " + message);
            System.out.println(" ");
            ex.printStackTrace();
            System.out.println(" ");
        }
    }

    public static void error(String message, Exception ex){
        error(null, message, ex);
    }

    public static void emptyLine(){
        System.out.println(" ");
    }

    public enum Level {
        INFO,
        WARNING,
        SEVERE,
        ERROR
    }
}
