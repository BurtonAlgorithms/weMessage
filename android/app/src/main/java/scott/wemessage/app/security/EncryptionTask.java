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
import scott.wemessage.commons.crypto.BCrypt;

public class EncryptionTask extends CryptoTask {

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
        boolean loop = true;
        while (loop){
            if (hasTaskFinished.get()){
                loop = false;
            }
            getSleep();
        }
        synchronized (keyTextPairLock) {
            return keyTextPair;
        }
    }
}