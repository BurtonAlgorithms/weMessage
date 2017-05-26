package scott.wemessage.server;

public final class ServerLauncher {

    private ServerLauncher() {}

    public static void main(String[] args){
        MessageServer messageServer = new MessageServer();

        messageServer.launch();
    }
}