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

package scott.wemessage.commons.connection.security;

import java.io.Serializable;

public class EncryptedFile implements Serializable {

    private String uuid;
    private String transferName;
    private byte[] encryptedData;
    private byte[] ivParams;
    private String key;

    public EncryptedFile(String uuid, String transferName, byte[] encryptedData, String key, byte[] ivParams){
        this.uuid = uuid;
        this.transferName = transferName;
        this.encryptedData = encryptedData;
        this.key = key;
        this.ivParams = ivParams;
    }

    public String getUuid(){
        return uuid;
    }

    public String getTransferName() {
        return transferName;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public byte[] getIvParams(){
        return ivParams;
    }

    public String getKey() {
        return key;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public void setTransferName(String transferName) {
        this.transferName = transferName;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public void setIvParams(byte[] ivParams) {
        this.ivParams = ivParams;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
