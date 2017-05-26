package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.ServerLogger;

public class CommandAddParticipant extends ScriptCommand {

    public CommandAddParticipant(CommandManager manager){
        super(manager, "addparticipant", "Adds a person to a group chat", new String[]{ "addperson", "groupadd", "addaccount" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.log("Example Usage: addparticipant \"Group Name\" \"account@icloud.com\"");
            ServerLogger.emptyLine();
            ServerLogger.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            ServerLogger.log("Example Usage: addparticipant \"Group Name\" \"2:22 PM\" \"Last message sent\" \"account@icloud.com\"");
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
            ServerLogger.log("Example Usage: addparticipant \"Group Name\" \"account@icloud.com\"");
            ServerLogger.emptyLine();
            ServerLogger.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            ServerLogger.log("Example Usage: addparticipant \"Group Name\" \"2:22 PM\" \"Last message sent\" \"account@icloud.com\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.ADD_PARTICIPANT, arguments);

        ServerLogger.log("Script AddParticipant returned a result of: " + processResult(result));
    }
}