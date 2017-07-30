package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;

public class CommandCreateGroup extends ScriptCommand {

    public CommandCreateGroup(CommandManager manager){
        super(manager, "creategroup", "Creates a new group chat", new String[]{ "newgroup" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.emptyLine();
            ServerLogger.log("Example Usage: creategroup \"New Group\" \"bob@icloud.com joe@gmail.com mike@aol.com\"");
            ServerLogger.log("Make sure each of the participants names are separated by spaces, or you will get errors.");
            return;
        }

        String[] arguments = {};
        String participants = StringUtils.join(StringUtils.getStringListFromString(args[1]), ",", 1);

        if(args.length == 2){
            arguments = new String[]{args[0], participants, "Group Created by weMessage"};
        }else if(args.length == 3){
            arguments = new String[]{args[0], participants, args[2]};
        }else if(args.length > 3){
            ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
            ServerLogger.emptyLine();
            ServerLogger.log("Example Usage: creategroup \"New Group\" \"bob@icloud.com joe@gmail.com mike@aol.com\" \"Initial message\"");
            ServerLogger.log("Make sure each of the participants names are separated by spaces, or you will get errors.");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.CREATE_GROUP, arguments);

        ServerLogger.log("Script CreateGroup returned a result of: " + processResult(result));
    }
}