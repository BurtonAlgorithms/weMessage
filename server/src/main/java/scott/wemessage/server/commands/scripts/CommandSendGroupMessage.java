package scott.wemessage.server.commands.scripts;

import java.io.File;
import java.util.List;

import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.chat.GroupChat;

public class CommandSendGroupMessage extends ScriptCommand {

    public CommandSendGroupMessage(CommandManager manager){
        super(manager, "sendgroupmessage", "Send a message to a group chat", new String[]{ "sendgroupmsg", "groupmessage", "groupmsg" });
    }

    public void execute(String[] args){
        if (args.length < 2){
            ServerLogger.log("Too little arguments provided to run this command.");
            ServerLogger.emptyLine();
            ServerLogger.log("Message Sending Example: sendgroupmessage \"Group Name\" \"An example Message.\"");
            ServerLogger.log("File Sending Example: sendgroupmessage \"Group Name\" \"/Users/me/Desktop/example.file\"");
            ServerLogger.log("Message and File Sending: sendgroupmessage \"Group Name\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
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
                    Object result;

                    if (new File(args[1]).exists()) {
                        result = getScriptExecutor().runSendGroupMessageScript(chats.get(0), args[1], "");
                    }else {
                        result = getScriptExecutor().runSendGroupMessageScript(chats.get(0), "", args[1]);
                    }

                    ServerLogger.log("Action Send Group Message returned a result of: " + processResult(result));
                    return;
                }

                if (chats.size() > 1){
                    ServerLogger.log("Multiple group chats have the name: " + args[0] + "!");
                    ServerLogger.log("Please choose which group chat you want to perform this action on!");
                    ServerLogger.log("Example Usage: sendgroupmessage \"Group Name\" 2 \"An example Message.\"");

                    ServerLogger.emptyLine();
                    printChatOptions(chats);
                }
            } else if (args.length == 3) {

                if (chats.size() == 1){
                    Object result = getScriptExecutor().runSendGroupMessageScript(chats.get(0), args[2], args[1]);

                    ServerLogger.log("Action Send Group Message returned a result of: " + processResult(result));
                    return;
                }

                if (chats.size() > 1){
                    Integer i;

                    try {
                        i = Integer.parseInt(args[1]);
                    } catch (Exception ex){
                        ServerLogger.log("Multiple group chats have the name: " + args[0] + "!");
                        ServerLogger.log("Please choose which group chat you want to perform this action on!");
                        ServerLogger.log("Example Usage: sendgroupmessage \"Group Name\" 2 \"An example Message.\" \"/Users/me/Desktop/example.file\"");
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

                    Object result;

                    if (new File(args[2]).exists()) {
                        result = getScriptExecutor().runSendGroupMessageScript(chat, args[2], "");
                    }else {
                        result = getScriptExecutor().runSendGroupMessageScript(chat, "", args[2]);
                    }

                    ServerLogger.log("Action Send Group Message returned a result of: " + processResult(result));
                }
            } else if (args.length == 4){

                if (chats.size() == 1){
                    ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                    ServerLogger.log("Example Usage: sendgroupmessage \"Group Name\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
                    return;
                }

                Integer i;

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

                Object result = getScriptExecutor().runSendGroupMessageScript(chat, args[3], args[2]);
                ServerLogger.log("Action Send Group Message returned a result of: " + processResult(result));

            } else if (args.length > 4){
                ServerLogger.log("There were too many arguments provided. Make sure your messages are surrounded in \"quotation marks.\"");
                ServerLogger.log("Example Usage: sendgroupmessage \"Group Name\" \"An example Message.\" \"/Users/me/Desktop/example.file\"");
                ServerLogger.emptyLine();

                ServerLogger.log("If you have multiple group chats with the same name, choose your chat based off of the option list.");
                ServerLogger.log("Example Usage: sendgroupmessage \"Group Name\" 3 \"An example Message.\" \"/Users/me/Desktop/example.file\"");

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
