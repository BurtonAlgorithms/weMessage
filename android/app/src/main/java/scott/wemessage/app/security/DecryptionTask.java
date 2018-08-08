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

public class DecryptionTask extends CryptoTask {

    private AtomicBoolean hasTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean hasTaskFinished = new AtomicBoolean(false);
    private final CryptoType cryptoType;
    private final KeyTextPair keyTextPair;
    private final Object decryptedTextLock = new Object();
    private String decryptedText = null;

    public DecryptionTask(KeyTextPair keyTextPair, CryptoType type){
        this.cryptoType = type;
        this.keyTextPair = keyTextPair;
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
        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
            getSleep();
        }
        synchronized (decryptedTextLock) {
            return decryptedText;
        }
    }
}