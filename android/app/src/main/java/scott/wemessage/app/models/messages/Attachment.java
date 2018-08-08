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

package scott.wemessage.app.models.messages;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Attachment {

    private UUID uuid;
    private String macGuid;
    private String transferName;
    private FileLocationContainer fileLocation;
    private String fileType;
    private Long totalBytes;

    public Attachment(){

    }

    public Attachment(UUID uuid, String macGuid, String transferName, FileLocationContainer fileLocation, String fileType, long totalBytes){
        this.uuid = uuid;
        this.macGuid = macGuid;
        this.transferName = transferName;
        this.fileLocation = fileLocation;
        this.fileType = fileType;
        this.totalBytes = totalBytes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public String getTransferName() {
        return transferName;
    }

    public FileLocationContainer getFileLocation() {
        return fileLocation;
    }

    public String getFileType() {
        return fileType;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public Attachment setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Attachment setMacGuid(String macGuid) {
        this.macGuid = macGuid;
        return this;
    }

    public Attachment setTransferName(String transferName) {
        this.transferName = transferName;
        return this;
    }

    public Attachment setFileLocation(FileLocationContainer fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public Attachment setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public Attachment setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }
}