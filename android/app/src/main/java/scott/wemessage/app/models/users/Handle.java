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

package scott.wemessage.app.models.users;

import android.content.res.Resources;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.UUID;

import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class Handle extends ContactInfo {

    private UUID uuid;
    private String handleID;
    private HandleType handleType;
    private boolean isDoNotDisturb;
    private boolean isBlocked;

    public Handle(){

    }

    public Handle(UUID uuid, String handleID, HandleType type, boolean isDoNotDisturb, boolean isBlocked){
        this.uuid = uuid;
        this.handleID = parseHandleId(handleID);
        this.handleType = type;
        this.isDoNotDisturb = isDoNotDisturb;
        this.isBlocked = isBlocked;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getHandleID() {
        return handleID;
    }

    public HandleType getHandleType() {
        return handleType;
    }

    public boolean isDoNotDisturb() {
        return isDoNotDisturb;
    }

    public boolean isBlocked(){
        return isBlocked;
    }

    public String getDisplayName(){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Contact c = weMessage.get().getMessageDatabase().getContactByHandle(this);

        if (c != null){
            String fullString = "";

            if (!StringUtils.isEmpty(c.getFirstName())) {
                fullString = c.getFirstName();
            }

            if (!StringUtils.isEmpty(c.getLastName())) {
                fullString += " " + c.getLastName();
            }

            if (StringUtils.isEmpty(fullString)) {
                try {
                    Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(getHandleID(), Resources.getSystem().getConfiguration().locale.getCountry());

                    return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                }catch (Exception ex){
                    return getHandleID();
                }
            }else {
                return fullString;
            }
        }else {
            try {
                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(getHandleID(), Resources.getSystem().getConfiguration().locale.getCountry());

                return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            }catch (Exception ex){
                return getHandleID();
            }
        }
    }

    @Override
    public ContactInfo findRoot() {
        Contact c = weMessage.get().getMessageManager().getContactHandleMap().get(getUuid().toString());

        if (c != null) return c;
        return this;
    }

    @Override
    public Handle pullHandle(boolean iMessage) {
        return this;
    }

    public Handle setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Handle setHandleID(String handleID) {
        this.handleID = parseHandleId(handleID);
        return this;
    }

    public Handle setHandleType(HandleType handleType) {
        if (this.handleType != HandleType.ME) this.handleType = handleType;
        return this;
    }

    public Handle setDoNotDisturb(boolean isDoNotDisturb) {
        if (handleType != HandleType.ME) this.isDoNotDisturb = isDoNotDisturb;
        return this;
    }

    public Handle setBlocked(boolean isBlocked){
        if (handleType != HandleType.ME) this.isBlocked = isBlocked;
        return this;
    }

    public static String parseHandleId(String handleID){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String locale = Resources.getSystem().getConfiguration().locale.getCountry();
        String returnHandle;

        if (phoneNumberUtil.isPossibleNumber(handleID, locale)){
            try {
                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(handleID, locale);
                returnHandle = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }catch (Exception ex){
                returnHandle = handleID;
            }
        } else {
            returnHandle = handleID.trim().toLowerCase();
        }

        return returnHandle;
    }

    public enum HandleType {
        IMESSAGE("iMessage"),
        SMS("SMS"),
        ME("Me"),
        UNKNOWN("Unknown");

        String typeName;

        HandleType(String typeName){
            this.typeName = typeName;
        }

        public String getTypeName(){
            return typeName;
        }

        public static HandleType stringToHandleType(String s){
            if (s == null) return null;

            switch (s.toLowerCase()){
                case "imessage":
                    return HandleType.IMESSAGE;
                case "sms":
                    return HandleType.SMS;
                case "me":
                    return HandleType.ME;
                case "unknown":
                    return HandleType.UNKNOWN;
                default:
                    return null;
            }
        }
    }
}