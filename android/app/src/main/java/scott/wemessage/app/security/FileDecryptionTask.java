/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.security;

import java.util.concurrent.atomic.AtomicBoolean;

import scott.wemessage.app.security.util.CryptoException;
import scott.wemessage.commons.crypto.AESCrypto;

public class FileDecryptionTask extends CryptoTask {

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
                if (cryptoFile.getIv() == null){
                    hasTaskFinished.set(true);
                    throw new CryptoException("Iv cannot be null");
                }
                synchronized (decryptedBytesLock) {
                    decryptedBytes = AESCrypto.decryptFileBytes(new AESCrypto.CipherByteArrayIv(cryptoFile.getEncryptedBytes(), cryptoFile.getIv()), cryptoFile.getKey());
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
            getSleep();
        }
        synchronized (decryptedBytesLock) {
            return decryptedBytes;
        }
    }
}