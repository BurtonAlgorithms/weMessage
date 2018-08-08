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

package scott.wemessage.commons.connection.json.message;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import scott.wemessage.commons.connection.security.EncryptedText;

public class JSONMessage {

    @SerializedName("macGuid")
    private String macGuid;

    @SerializedName("chat")
    private JSONChat chat;

    @SerializedName("handle")
    private String handle;

    @SerializedName("attachments")
    private List<JSONAttachment> attachments;

    @SerializedName("encryptedText")
    private EncryptedText encryptedText;

    @SerializedName("dateSent")
    private Long dateSent;

    @SerializedName("dateDelivered")
    private Long dateDelivered;

    @SerializedName("dateRead")
    private Long dateRead;

    @SerializedName("errored")
    private Boolean errored;

    @SerializedName("isSent")
    private Boolean isSent;

    @SerializedName("isDelivered")
    private Boolean isDelivered;

    @SerializedName("isRead")
    private Boolean isRead;

    @SerializedName("isFinished")
    private Boolean isFinished;

    @SerializedName("isFromMe")
    private Boolean isFromMe;

    @SerializedName("effect")
    private String messageEffect;

    public JSONMessage(String macGuid, JSONChat chat, String handle, List<JSONAttachment> attachments, EncryptedText encryptedText, Long dateSent, Long dateDelivered, Long dateRead,
                       Boolean errored, Boolean isSent, Boolean isDelivered, Boolean isRead, Boolean isFinished, Boolean isFromMe, String messageEffect){
        this.macGuid = macGuid;
        this.chat = chat;
        this.handle = handle;
        this.attachments = attachments;
        this.encryptedText = encryptedText;
        this.attachments = attachments;
        this.dateSent = dateSent;
        this.dateDelivered = dateDelivered;
        this.dateRead = dateRead;
        this.errored = errored;
        this.isSent = isSent;
        this.isDelivered = isDelivered;
        this.isRead = isRead;
        this.isFinished = isFinished;
        this.isFromMe = isFromMe;
        this.messageEffect = messageEffect;
    }

    public String getMacGuid() {
        return macGuid;
    }

    public JSONChat getChat() {
        return chat;
    }

    public String getHandle() {
        return handle;
    }

    public List<JSONAttachment> getAttachments() {
        return attachments;
    }

    public EncryptedText getEncryptedText() {
        return encryptedText;
    }

    public Long getDateSent() {
        return dateSent;
    }

    public Long getDateDelivered() {
        return dateDelivered;
    }

    public Long getDateRead() {
        return dateRead;
    }

    public Boolean getErrored() {
        return errored;
    }

    public Boolean isSent() {
        return isSent;
    }

    public Boolean isDelivered() {
        return isDelivered;
    }

    public Boolean isRead() {
        return isRead;
    }

    public Boolean isFinished() {
        return isFinished;
    }

    public Boolean isFromMe() {
        return isFromMe;
    }

    public String getMessageEffect() {
        return messageEffect;
    }

    public void setMacGuid(String macGuid) {
        this.macGuid = macGuid;
    }

    public void setChat(JSONChat chat) {
        this.chat = chat;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setAttachments(List<JSONAttachment> attachments) {
        this.attachments = attachments;
    }

    public void setEncryptedText(EncryptedText encryptedText) {
        this.encryptedText = encryptedText;
    }

    public void setDateSent(Long dateSent) {
        this.dateSent = dateSent;
    }

    public void setDateDelivered(Long dateDelivered) {
        this.dateDelivered = dateDelivered;
    }

    public void setDateRead(Long dateRead) {
        this.dateRead = dateRead;
    }

    public void setErrored(Boolean errored) {
        this.errored = errored;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
    }

    public void setDelivered(Boolean delivered) {
        isDelivered = delivered;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public void setFinished(Boolean finished) {
        isFinished = finished;
    }

    public void setFromMe(Boolean fromMe) {
        isFromMe = fromMe;
    }

    public void setMessageEffect(String messageEffect) {
        this.messageEffect = messageEffect;
    }
}