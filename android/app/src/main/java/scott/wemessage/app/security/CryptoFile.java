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

public class CryptoFile {

    private final String key;
    private final byte[] encryptedBytes;
    private final byte[] iv;

    public CryptoFile(byte[] encryptedBytes, String key, byte[] iv){
        this.key = key;
        this.encryptedBytes = encryptedBytes;
        this.iv = iv;
    }

    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    public String getKey() {
        return key;
    }

    public byte[] getIv(){
        return iv;
    }

}