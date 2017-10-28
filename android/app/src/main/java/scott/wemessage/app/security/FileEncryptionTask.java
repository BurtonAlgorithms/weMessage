package scott.wemessage.app.security;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.security.util.CryptoException;
import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.types.FailReason;

public class FileEncryptionTask extends CryptoTask {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final File file;
    private final String key;
    private final Object cryptoFileLock = new Object();
    private CryptoFile cryptoFile = null;

    public FileEncryptionTask(File file, String key, CryptoType type){
        this.cryptoType = type;
        this.file = file;
        this.key = key;
    }

    @Override
    public void run() {
        hasTaskStarted.set(true);
        try {
            String returnKey;
            AESCrypto.CipherByteArrayIv byteArrayIv;

            if (cryptoType == CryptoType.AES) {
                if (key == null) {
                    returnKey = AESCrypto.keysToString(AESCrypto.generateKeys());
                } else {
                    returnKey = key;
                }
                byteArrayIv = AESCrypto.encryptFile(file, returnKey);
            } else {
                hasTaskFinished.set(true);
                throw new CryptoException("The Crypto Type asked for is unsupported for file encryption");
            }

            synchronized (cryptoFileLock) {
                if (byteArrayIv instanceof AESCrypto.OOMCipherByteArray){
                    cryptoFile = new FailedCryptoFile(FailReason.MEMORY);
                }else {
                    cryptoFile = new CryptoFile(byteArrayIv.getCipherBytes(), returnKey, byteArrayIv.getIv());
                }
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
            getSleep();
        }
        synchronized (cryptoFileLock) {
            return cryptoFile;
        }
    }
}