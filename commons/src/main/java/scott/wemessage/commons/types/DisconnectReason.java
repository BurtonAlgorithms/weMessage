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

public enum DisconnectReason {

    ALREADY_CONNECTED(Constants.DISCONNECT_REASON_ALREADY_CONNECTED),
    INVALID_LOGIN(Constants.DISCONNECT_REASON_INVALID_LOGIN),
    SERVER_CLOSED(Constants.DISCONNECT_REASON_SERVER_CLOSED),
    ERROR(Constants.DISCONNECT_REASON_ERROR),
    FORCED(Constants.DISCONNECT_REASON_FORCED),
    CLIENT_DISCONNECTED(Constants.DISCONNECT_REASON_CLIENT_QUIT),
    INCORRECT_VERSION(Constants.DISCONNECT_REASON_INCORRECT_VERSION);

    Integer code;

    DisconnectReason(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return code;
    }

    public static DisconnectReason fromCode(Integer value){
        switch (value){
            case Constants.DISCONNECT_REASON_ALREADY_CONNECTED:
                return DisconnectReason.ALREADY_CONNECTED;
            case Constants.DISCONNECT_REASON_INVALID_LOGIN:
                return DisconnectReason.INVALID_LOGIN;
            case Constants.DISCONNECT_REASON_SERVER_CLOSED:
                return DisconnectReason.SERVER_CLOSED;
            case Constants.DISCONNECT_REASON_ERROR:
                return DisconnectReason.ERROR;
            case Constants.DISCONNECT_REASON_FORCED:
                return DisconnectReason.FORCED;
            case Constants.DISCONNECT_REASON_CLIENT_QUIT:
                return DisconnectReason.CLIENT_DISCONNECTED;
            case Constants.DISCONNECT_REASON_INCORRECT_VERSION:
                return DisconnectReason.INCORRECT_VERSION;
            default:
                return null;
        }
    }
}