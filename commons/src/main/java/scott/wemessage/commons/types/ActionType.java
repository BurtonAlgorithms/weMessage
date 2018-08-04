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