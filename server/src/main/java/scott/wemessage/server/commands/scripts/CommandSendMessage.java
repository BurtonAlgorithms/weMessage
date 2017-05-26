package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.utils.LoggingUtils;

import java.io.File;

public class CommandSendMessage extends ScriptCommand {

    public CommandSendMessage(CommandManager manager){
        super(manager, "sendmessage", "Send a direct message to a person", new String[]{ "sendmsg" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            LoggingUtils.log("Too little arguments provided to run this command.");
            LoggingUtils.log("Example Usage: sendmessage \"user@icloud.com\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
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
            LoggingUtils.log("There were too many arguments provided. Make sure your message is surrounded in \"quotation marks.\"");
            LoggingUtils.log("Example Usage: sendmessage \"user@icloud.com\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.SEND_MESSAGE, arguments);

        LoggingUtils.log("Script SendMessage returned a result of: " + processResult(result));
    }
}