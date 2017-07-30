package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;

public class CommandRenameGroup extends ScriptCommand {

    public CommandRenameGroup(CommandManager manager){
        super(manager, "renamegroup", "Changes the name of a group chat", new String[]{ "rename" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.log("Example Usage: renamegroup \"Group Name\" \"New Group Name\"");
            ServerLogger.emptyLine();
            ServerLogger.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            ServerLogger.log("Example Usage: renamegroup \"Group Name\" \"2:22 PM\" \"Last message sent\" \"New Group Name\"");
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
            ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
            ServerLogger.log("Example Usage: renamegroup \"Group Name\" \"New Group Name\"");
            ServerLogger.emptyLine();
            ServerLogger.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            ServerLogger.log("Example Usage: renamegroup \"Group Name\" \"2:22 PM\" \"Last message sent\" \"New Group Name\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.RENAME_GROUP, arguments);

        ServerLogger.log("Script RenameGroup returned a result of: " + processResult(result));
    }
}
