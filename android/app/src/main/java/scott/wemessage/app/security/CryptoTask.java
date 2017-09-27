package scott.wemessage.app.security;

public abstract class CryptoTask extends Thread {

    private int SLEEP_DURATION = 25;

    void getSleep(){
        try {
            Thread.sleep(SLEEP_DURATION);
        }catch (Exception ex){ }
    }
}
