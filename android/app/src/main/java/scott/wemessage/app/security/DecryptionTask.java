package scott.wemessage.app.security;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.commons.crypto.AESCrypto;

public class DecryptionTask extends Thread {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final KeyTextPair keyTextPair;
    private final Object decryptedTextLock = new Object();
    private String decryptedText = null;

    public DecryptionTask(KeyTextPair keyTextPair, CryptoType type){
        this.cryptoType = type;
        this.keyTextPair = keyTextPair;

        if (cryptoType == CryptoType.AES){
            AESCrypto.setPrngHelper(new AesPrngHelper());
        }
    }

    @Override
    public void run() {
        hasTaskStarted.set(true);
        try {
            if (cryptoType == CryptoType.AES) {
                if (keyTextPair.getKey() == null) {
                    hasTaskFinished.set(true);
                    throw new CryptoException("Key cannot be null");
                }
                synchronized (decryptedTextLock) {
                    decryptedText = AESCrypto.decryptString(keyTextPair.getEncryptedText(), keyTextPair.getKey());
                }
                hasTaskFinished.set(true);
            } else if (cryptoType == CryptoType.BCRYPT) {
                hasTaskFinished.set(true);
                throw new CryptoException("You cannot decrypt a hash");
            } else {
                hasTaskFinished.set(true);
                throw new CryptoException("The Crypto Type asked for is unsupported");
            }
        }catch(Exception ex) {
            hasTaskFinished.set(true);
            throw new CryptoException("An error occurred while performing a decryption", ex);
        }
    }

    public void runDecryptTask() throws CryptoException {
        start();
    }

    public String getDecryptedText(){
        if (!hasTaskStarted.get()){
            return null;
        }

        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
        }
        synchronized (decryptedTextLock) {
            return decryptedText;
        }
    }
}