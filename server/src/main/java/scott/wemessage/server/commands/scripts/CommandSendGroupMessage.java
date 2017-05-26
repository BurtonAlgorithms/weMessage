package scott.wemessage.server.commands.scripts;

import scott.wemessage.commons.types.ActionType;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.utils.LoggingUtils;

import java.io.File;

public class CommandSendGroupMessage extends ScriptCommand {

    public CommandSendGroupMessage(CommandManager manager){
        super(manager, "sendgroupmessage", "Send a message to a group chat", new String[]{ "sendgroupmsg", "groupmessage", "groupmsg" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            LoggingUtils.log("Too little arguments provided to run this command.");
            LoggingUtils.log("Example Usage: sendgroupessage \"Group Name\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            LoggingUtils.emptyLine();
            LoggingUtils.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            LoggingUtils.log("Example Usage: sendgroupmessage \"Group Name\" \"2:22 PM\" \"Last message sent\" \"Example message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }
        String[] arguments = {};

        if(args.length == 2){
            if (new File(args[1]).exists()) {
                arguments = new String[]{args[0], "", "", args[1], ""};
            }else {
                arguments = new String[]{args[0], "", "", "", args[1]};
            }
        }else if(args.length == 3){
            arguments = new String[]{args[0], "", "", args[2], args[1]};
        }else if (args.length == 4) {
            arguments = new String[]{args[0], args[1], "", args[3], args[2]};
        }else if (args.length == 5) {
            arguments = new String[]{args[0], args[1], args[2], args[4], args[3]};
        }else if (args.length > 5){
            LoggingUtils.log("There were too many arguments provided. Make sure your messages are surrounded in \"quotation marks.\"");
            LoggingUtils.log("Example Usage: sendgroupmessage \"Group Name\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
            LoggingUtils.emptyLine();
            LoggingUtils.log("If you have multiple groups with the same name, be sure to specify the time of the last message sent and its content.");
            LoggingUtils.log("Example Usage: sendgroupmessage \"Group Name\" \"2:22 PM\" \"Last message sent\" \"Example message.\" \"/Users/me/Desktop/example.file\"");
            return;
        }

        Object result = getScriptExecutor().runScript(ActionType.SEND_GROUP_MESSAGE, arguments);

        LoggingUtils.log("Script SendGroupMessage returned a result of: " + processResult(result));
    }
}
