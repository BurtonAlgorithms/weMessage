package scott.wemessage.commons.crypto;

/**
 *
 * A Base64 Wrapper class. Since Android and native Java have their own different versions of Base64, this class
 * wraps around the encode and decode functions of those classes.
 *
 * Roman Scott
 *
 */
public abstract class Base64Wrapper {

    public abstract byte[] decodeString(String string);

    public abstract String encodeToString(byte[] bytes);
}
