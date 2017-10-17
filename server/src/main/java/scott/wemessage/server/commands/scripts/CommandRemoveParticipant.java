package scott.wemessage.server.commands.scripts;

import java.util.List;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.chat.GroupChat;

public class CommandRemoveParticipant extends ScriptCommand {

    public CommandRemoveParticipant(CommandManager manager){
        super(manager, "removeparticipant", "Removes a person from a group chat", new String[]{ "removeperson", "groupremove", "removeaccount" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.log("Example Usage: removeparticipant \"Group Name\" \"account@icloud.com\"");
            return;
        }

        try {
            List<GroupChat> chats = getMessagesDatabase().getGroupChatsByName(args[0]);

            if (chats.size() == 0) {
                ServerLogger.log("Group Chat " + args[0] + " does not exist!");
                return;
            }

            if (args.length == 2) {
                if (chats.size() == 1){
                    Object result = getScriptExecutor().runRemoveParticipantScript(chats.get(0), args[1]);

                    ServerLogger.log("Action Remove Participant returned a result of: " + processResult(result));
                    return;
                }

                if (chats.size() > 1){
                    ServerLogger.log("Multiple group chats have the name: " + args[0] + "!");
                    ServerLogger.log("Please choose which group chat you want to perform this action on!");
                    ServerLogger.log("Example Usage: removeparticipant \"Group Name\" 2 \"account@icloud.com\"");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                }
            } else if (args.length == 3) {
                Integer i;

                if (chats.size() == 1){
                    ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                    ServerLogger.log("Example Usage: removeparticipant \"Group Name\" \"account@icloud.com\"");
                    return;
                }

                try {
                    i = Integer.parseInt(args[1]);
                }catch (Exception ex){
                    ServerLogger.log(args[1] + " is not a number! Make sure you choose a valid option based off the list below.");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                    return;
                }

                GroupChat chat;

                try {
                    chat = chats.get(i - 1);
                }catch (Exception ex){
                    ServerLogger.log(args[1] + " is not a valid number for your chat list options!");
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                    return;
                }

                Object result = getScriptExecutor().runRemoveParticipantScript(chat, args[2]);
                ServerLogger.log("Action Remove Participant returned a result of: " + processResult(result));
            } else if (args.length > 3){
                ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                ServerLogger.log("Example Usage: removeparticipant \"Group Name\" \"account@icloud.com\"");
                ServerLogger.emptyLine();

                ServerLogger.log("If you have multiple group chats with the same name, choose your chat based off of the option list.");
                ServerLogger.log("Example Usage: removeparticipant \"Group Name\" 3 \"account@icloud.com\"");

                if (chats.size() > 1) {
                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                }
            }
        }catch (Exception ex){
            ServerLogger.error("An error occurred while trying to run this command!", ex);
        }
    }
}
