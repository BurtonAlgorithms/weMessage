package scott.wemessage.app.security;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.security.util.CryptoException;
import scott.wemessage.commons.crypto.AESCrypto;

public class FileDecryptionTask extends Thread {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final CryptoFile cryptoFile;
    private final Object decryptedBytesLock = new Object();
    private byte[] decryptedBytes = null;

    public FileDecryptionTask(CryptoFile cryptoFile, CryptoType type){
        this.cryptoType = type;
        this.cryptoFile = cryptoFile;
    }

    @Override
    public void run() {
        hasTaskStarted.set(true);
        try {
            if (cryptoType == CryptoType.AES) {
                if (cryptoFile.getKey() == null) {
                    hasTaskFinished.set(true);
                    throw new CryptoException("Key cannot be null");
                }
                if (cryptoFile.getIvMac() == null){
                    hasTaskFinished.set(true);
                    throw new CryptoException("Iv Mac cannot be null");
                }
                synchronized (decryptedBytesLock) {
                    decryptedBytes = AESCrypto.decryptBytes(new AESCrypto.CipherByteArrayIvMac(cryptoFile.getEncryptedBytes(), cryptoFile.getIvMac()), cryptoFile.getKey());
                }
                hasTaskFinished.set(true);
            } else {
                hasTaskFinished.set(true);
                throw new CryptoException("The Crypto Type asked for is unsupported for file decryption");
            }
        }catch(Exception ex) {
            hasTaskFinished.set(true);
            throw new CryptoException("An error occurred while performing a decryption", ex);
        }
    }

    public void runDecryptTask() throws CryptoException {
        start();
    }

    public byte[] getDecryptedBytes(){
        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
        }
        synchronized (decryptedBytesLock) {
            return decryptedBytes;
        }
    }
}
