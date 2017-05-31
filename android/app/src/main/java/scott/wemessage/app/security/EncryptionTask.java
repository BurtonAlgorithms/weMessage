package scott.wemessage.app.security;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.crypto.BCrypt;

public class EncryptionTask extends Thread {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final String plainText;
    private final String key;
    private final Object keyTextPairLock = new Object();
    private KeyTextPair keyTextPair = null;

    public EncryptionTask(String plainText, String key, CryptoType type){
        this.cryptoType = type;
        this.plainText = plainText;
        this.key = key;

        if (cryptoType == CryptoType.AES){
            AESCrypto.setPrngHelper(new AesPrngHelper());
        }
    }

    @Override
    public void run() {
        hasTaskStarted.set(true);
        try {
            String returnKey;
            String encryptedText;

            if (cryptoType == CryptoType.AES) {
                if (key == null) {
                    returnKey = AESCrypto.keysToString(AESCrypto.generateKeys());
                } else {
                    returnKey = key;
                }
                encryptedText = AESCrypto.encryptString(plainText, returnKey);
            } else if (cryptoType == CryptoType.BCRYPT) {
                if (key == null) {
                    returnKey = BCrypt.generateSalt();
                } else {
                    returnKey = key;
                }
                encryptedText = BCrypt.hashPassword(plainText, returnKey);
            } else {
                hasTaskFinished.set(true);
                throw new CryptoException("The Crypto Type asked for is unsupported");
            }

            synchronized (keyTextPairLock) {
                keyTextPair = new KeyTextPair(encryptedText, returnKey);
            }
            hasTaskFinished.set(true);
        }catch(Exception ex) {
            hasTaskFinished.set(true);
            throw new CryptoException("An error occurred while performing an encryption", ex);
        }
    }

    public void runEncryptTask() {
        start();
    }

    public KeyTextPair getEncryptedText(){
        if (!hasTaskStarted.get()){
            return null;
        }

        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
        }
        synchronized (keyTextPairLock) {
            return keyTextPair;
        }
    }
}