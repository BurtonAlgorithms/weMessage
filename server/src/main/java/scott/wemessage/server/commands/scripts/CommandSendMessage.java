package scott.wemessage.server.commands.scripts;

import java.io.File;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;

public class CommandSendMessage extends ScriptCommand {

    public CommandSendMessage(CommandManager manager){
        super(manager, "sendmessage", "Send a direct message to a person", new String[]{ "sendmsg" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.log("Example Usage: sendmessage \"user@icloud.com\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }
        String[] arguments = {};

        if(args.length == 2){
            if (new File(args[1]).exists()) {
                arguments = new String[]{args[0], args[1], ""};
            }else {
                arguments = new String[]{args[0], "", args[1]};
            }
        }else if(args.length == 3){
            arguments = new String[]{args[0], args[2], args[1]};
        }else if (args.length > 3){
            ServerLogger.log("There were too many arguments provided. Make sure your message is surrounded in \"quotation marks.\"");
            ServerLogger.log("Example Usage: sendmessage \"user@icloud.com\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.SEND_MESSAGE, arguments);

        ServerLogger.log("Script SendMessage returned a result of: " + processResult(result));
    }
}