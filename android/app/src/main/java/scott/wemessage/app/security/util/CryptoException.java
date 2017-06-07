package scott.wemessage.app.security.util;

public class CryptoException extends SecurityException {

    public CryptoException() {
        super();
    }

    public CryptoException(String s) {
        super(s);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }
}