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

package scott.wemessage.server.messages;

import java.util.Date;

import scott.wemessage.commons.utils.DateUtils;

public class Attachment  {

    private long rowID;
    private String guid;
    private long createdDate;
    private String fileLocation;
    private String transferName;
    private String fileType;
    private long totalBytes;
    
    public Attachment(){
        this(null, -1L, -1L, null, null, null, -1L);
    }

    public Attachment(String guid, long rowID, long createdDate, String fileLocation, String transferName, String fileType, long totalBytes){
        this.rowID = rowID;
        this.guid = guid;
        this.createdDate = createdDate;
        this.fileLocation = fileLocation;
        this.transferName = transferName;
        this.fileType = fileType;
        this.totalBytes = totalBytes;
    }

    public String getGuid() {
        return guid;
    }

    public long getRowID() {
        return rowID;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public String getTransferName() {
        return transferName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public Date getModernCreatedDate() {
        return DateUtils.getDateUsing2001(createdDate);
    }

    public Attachment setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public Attachment setRowID(long rowID) {
        this.rowID = rowID;
        return this;
    }

    public Attachment setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Attachment setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public Attachment setTransferName(String transferName) {
        this.transferName = transferName;
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