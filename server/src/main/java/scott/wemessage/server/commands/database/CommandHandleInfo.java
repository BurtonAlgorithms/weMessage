package scott.wemessage.server.commands.database;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.utils.LoggingUtils;

public class CommandHandleInfo extends DatabaseCommand {

    public CommandHandleInfo(CommandManager manager){
        super(manager, "handleinfo", "Gets information about an iMessage Account Sender", new String[]{ "senderinfo", "gethandle" });
    }

    public void execute(String[] args){
        if (args.length == 0){
            LoggingUtils.log("Please enter an iMessage Account to look up data for.");
            return;
        }
        try {
            Handle handle = getMessagesDatabase().getHandleByAccount(args[0]);

            if (handle == null){
                LoggingUtils.log("Could not find the handle for account \"" + args[0] + "\"");
                return;
            }

            LoggingUtils.log("Handle Account: " + handle.getHandleID());
            LoggingUtils.log("Country: " + handle.getCountry());
            LoggingUtils.log("Database Row ID: " + handle.getRowID());

        }catch(Exception ex){
            LoggingUtils.error("An error occurred while fetching the handles database.", ex);
        }
    }
}