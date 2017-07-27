package scott.wemessage.app.security;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.security.util.AesPrngHelper;
import scott.wemessage.app.security.util.AndroidBase64Wrapper;
import scott.wemessage.app.security.util.CryptoException;
import scott.wemessage.commons.crypto.AESCrypto;

public class FileEncryptionTask extends Thread {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final byte[] bytes;
    private final String key;
    private final Object cryptoFileLock = new Object();
    private CryptoFile cryptoFile = null;

    public FileEncryptionTask(byte[] bytes, String key, CryptoType type){
        this.cryptoType = type;
        this.bytes = bytes;
        this.key = key;

        if (cryptoType == CryptoType.AES){
            AESCrypto.setBase64Wrapper(new AndroidBase64Wrapper());
            AESCrypto.setPrngHelper(new AesPrngHelper());
        }
    }

    @Override
    public void run() {
        hasTaskStarted.set(true);
        try {
            String returnKey;
            AESCrypto.CipherByteArrayIvMac byteArrayIvMac;

            if (cryptoType == CryptoType.AES) {
                if (key == null) {
                    returnKey = AESCrypto.keysToString(AESCrypto.generateKeys());
                } else {
                    returnKey = key;
                }
                byteArrayIvMac = AESCrypto.encryptBytes(bytes, returnKey);
            } else {
                hasTaskFinished.set(true);
                throw new CryptoException("The Crypto Type asked for is unsupported for file encryption");
            }

            synchronized (cryptoFileLock) {
                cryptoFile = new CryptoFile(byteArrayIvMac.getCipherBytes(), returnKey, byteArrayIvMac.joinedIvAndMac());
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

    public CryptoFile getEncryptedFile(){
        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
        }
        synchronized (cryptoFileLock) {
            return cryptoFile;
        }
    }
}
