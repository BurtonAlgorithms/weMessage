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

public enum ActionType {

    SEND_MESSAGE(Constants.ACTION_SEND_MESSAGE, "SendMessage"),
    SEND_GROUP_MESSAGE(Constants.ACTION_SEND_GROUP_MESSAGE, "SendGroupMessage"),
    RENAME_GROUP(Constants.ACTION_RENAME_GROUP, "RenameGroup"),
    ADD_PARTICIPANT(Constants.ACTION_ADD_PARTICIPANT, "AddParticipant"),
    REMOVE_PARTICIPANT(Constants.ACTION_REMOVE_PARTICIPANT, "RemoveParticipant"),
    CREATE_GROUP(Constants.ACTION_CREATE_GROUP, "CreateGroup"),
    LEAVE_GROUP(Constants.ACTION_LEAVE_GROUP, "LeaveGroup");

    int code;
    String scriptName;

    ActionType(int value, String scriptName){
        this.code = value;
        this.scriptName = scriptName;
    }

    public String getScriptName(){
        return scriptName;
    }

    public int getCode(){
        return code;
    }

    public static ActionType fromCode(Integer value){
        switch (value){
            case Constants.ACTION_SEND_MESSAGE:
                return ActionType.SEND_MESSAGE;
            case Constants.ACTION_SEND_GROUP_MESSAGE:
                return ActionType.SEND_GROUP_MESSAGE;
            case Constants.ACTION_RENAME_GROUP:
                return ActionType.RENAME_GROUP;
            case Constants.ACTION_ADD_PARTICIPANT:
                return ActionType.ADD_PARTICIPANT;
            case Constants.ACTION_REMOVE_PARTICIPANT:
                return ActionType.REMOVE_PARTICIPANT;
            case Constants.ACTION_CREATE_GROUP:
                return ActionType.CREATE_GROUP;
            case Constants.ACTION_LEAVE_GROUP:
                return ActionType.LEAVE_GROUP;
            default:
                return null;
        }
    }
}