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

package scott.wemessage.commons.types;

import scott.wemessage.commons.Constants;

public enum ReturnType {

    VERSION_MISMATCH(Constants.VERSION_MISMATCH, "Version Mismatch"),
    UNKNOWN_ERROR(Constants.UNKNOWN_ERROR, "Unknown Error"),
    SENT(Constants.SENT, "Sent"),
    INVALID_NUMBER(Constants.INVALID_NUMBER, "Invalid Number"),
    NUMBER_NOT_IMESSAGE(Constants.NUMBER_NOT_IMESSAGE, "Account not iMessage"),
    GROUP_CHAT_NOT_FOUND(Constants.GROUP_CHAT_NOT_FOUND, "Group Chat Not Found"),
    NOT_SENT(Constants.NOT_SENT, "Not Sent"),
    SERVICE_NOT_AVAILABLE(Constants.SERVICE_NOT_AVAILABLE, "Service Not Available"),
    FILE_NOT_FOUND(Constants.FILE_NOT_FOUND, "File Not Found"),
    NULL_MESSAGE(Constants.NULL_MESSAGE, "Empty Message"),
    ASSISTIVE_ACCESS_DISABLED(Constants.ASSISTIVE_ACCESS_DISABLED, "Assistive Access Is Disabled"),
    UI_ERROR(Constants.UI_ERROR, "Messages Application Error"),
    ACTION_PERFORMED(Constants.ACTION_PERFORMED, "Action Performed");

    int code;
    String returnName;

    ReturnType(int code, String returnName){
        this.code = code;
        this.returnName = returnName;
    }

    public int getCode(){
        return code;
    }

    public String getReturnName(){
        return returnName;
    }

    public static ReturnType fromCode(Integer value){
        switch (value){
            case Constants.VERSION_MISMATCH:
                return ReturnType.VERSION_MISMATCH;
            case Constants.UNKNOWN_ERROR:
                return ReturnType.UNKNOWN_ERROR;
            case Constants.SENT:
                return ReturnType.SENT;
            case Constants.INVALID_NUMBER:
                return ReturnType.INVALID_NUMBER;
            case Constants.NUMBER_NOT_IMESSAGE:
                return ReturnType.NUMBER_NOT_IMESSAGE;
            case Constants.GROUP_CHAT_NOT_FOUND:
                return ReturnType.GROUP_CHAT_NOT_FOUND;
            case Constants.NOT_SENT:
                return ReturnType.NOT_SENT;
            case Constants.SERVICE_NOT_AVAILABLE:
                return ReturnType.SERVICE_NOT_AVAILABLE;
            case Constants.FILE_NOT_FOUND:
                return ReturnType.FILE_NOT_FOUND;
            case Constants.NULL_MESSAGE:
                return ReturnType.NULL_MESSAGE;
            case Constants.ASSISTIVE_ACCESS_DISABLED:
                return ReturnType.ASSISTIVE_ACCESS_DISABLED;
            case Constants.UI_ERROR:
                return ReturnType.UI_ERROR;
            case Constants.ACTION_PERFORMED:
                return ReturnType.ACTION_PERFORMED;
            default:
                return null;
        }
    }
}