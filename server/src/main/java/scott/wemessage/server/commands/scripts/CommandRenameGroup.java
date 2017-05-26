package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.utils.LoggingUtils;

public class CommandRenameGroup extends ScriptCommand {

    public CommandRenameGroup(CommandManager manager){
        super(manager, "renamegroup", "Changes the name of a group chat", new String[]{ "rename" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            LoggingUtils.log("Too little arguments provided to run this command.");
            LoggingUtils.log("Example Usage: renamegroup \"Group Name\" \"New Group Name\"");
            LoggingUtils.emptyLine();
            LoggingUtils.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            LoggingUtils.log("Example Usage: renamegroup \"Group Name\" \"2:22 PM\" \"Last message sent\" \"New Group Name\"");
            return;
        }

        String[] arguments = {};

        if(args.length == 2){
            arguments = new String[]{args[0], "", "", args[1]};
        }else if(args.length == 3){
            arguments = new String[]{args[0], args[1], "", args[2]};
        }else if(args.length == 4){
            arguments = new String[]{args[0], args[1], args[2], args[3]};
        }else if(args.length > 4){
            LoggingUtils.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
            LoggingUtils.log("Example Usage: renamegroup \"Group Name\" \"New Group Name\"");
            LoggingUtils.emptyLine();
            LoggingUtils.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            LoggingUtils.log("Example Usage: renamegroup \"Group Name\" \"2:22 PM\" \"Last message sent\" \"New Group Name\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.RENAME_GROUP, arguments);

        LoggingUtils.log("Script RenameGroup returned a result of: " + processResult(result));
    }
}
