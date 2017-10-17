package scott.wemessage.server.commands.scripts;

import java.util.List;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.chat.GroupChat;

public class CommandLeaveGroup extends ScriptCommand {

    public CommandLeaveGroup(CommandManager manager){
        super(manager, "leavegroup", "Leaves a group chat", new String[]{ "leave" });
    }

    public void execute(String[] args) {
        if (args.length == 0) {
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.log("Example Usage: leavegroup \"Group Name\"");
            return;
        }

        try {
            List<GroupChat> chats = getMessagesDatabase().getGroupChatsByName(args[0]);

            if (chats.size() == 0) {
                ServerLogger.log("Group Chat " + args[0] + " does not exist!");
                return;
            }

            if (args.length == 1) {
                if (chats.size() == 1) {
                    Object result = getScriptExecutor().runLeaveGroupScript(chats.get(0));

                    ServerLogger.log("Action Leave Group returned a result of: " + processResult(result));
                    return;
                }

                if (chats.size() > 1) {
                    ServerLogger.log("Multiple group chats have the name: " + args[0] + "!");
                    ServerLogger.log("Please choose which group chat you want to perform this action on!");
                    ServerLogger.log("Example Usage: leavegroup \"Group Name\" 2");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                }
            } else if (args.length == 2) {
                Integer i;

                if (chats.size() == 1){
                    ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                    ServerLogger.log("Example Usage: leavegroup \"Group Name\"");
                    return;
                }

                try {
                    i = Integer.parseInt(args[1]);
                } catch (Exception ex) {
                    ServerLogger.log(args[1] + " is not a number! Make sure you choose a valid option based off the list below.");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                    return;
                }

                GroupChat chat;

                try {
                    chat = chats.get(i - 1);
                } catch (Exception ex) {
                    ServerLogger.log(args[1] + " is not a valid number for your chat list options!");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                    return;
                }

                Object result = getScriptExecutor().runLeaveGroupScript(chat);
                ServerLogger.log("Action Leave Group returned a result of: " + processResult(result));
            } else if (args.length > 3) {
                ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                ServerLogger.log("Example Usage: leavegroup \"Group Name\"");
                ServerLogger.emptyLine();

                ServerLogger.log("If you have multiple group chats with the same name, choose your chat based off of the option list.");
                ServerLogger.log("Example Usage: leavegroup \"Group Name\" 3");

                if (chats.size() > 1) {
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                }
            }
        } catch (Exception ex) {
            ServerLogger.error("An error occurred while trying to run this command!", ex);
        }
    }
}