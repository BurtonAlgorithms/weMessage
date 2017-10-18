package scott.wemessage.server.commands.database;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;

public class CommandChatInfo extends DatabaseCommand {

    public CommandChatInfo(CommandManager manager){
        super(manager, "chatinfo", "Gets information about a conversation", new String[]{ "getchat" });
    }

    public void execute(String[] args){
        if(args.length == 0){
            ServerLogger.log("Please enter the phone number, email address, or group chat name you want to look up information for.");
            return;
        }

        try {
            if(args.length == 1) {
                PeerChat peerChat = getMessagesDatabase().getChatByAccount(args[0]);

                if (peerChat != null) {
                    Message lastMessage = getMessagesDatabase().getLastNotNullMessageFromChat(peerChat);
                    ServerLogger.log("Member: " + peerChat.getPeer().getHandleID());

                    if (lastMessage == null){
                        ServerLogger.log("Last Message from Chat: Null Message");
                    }else {
                        ServerLogger.log("Last Message from Chat: " + lastMessage.getText());
                    }
                    return;
                }

                List<GroupChat> groupChats = getMessagesDatabase().getGroupChatsByName(args[0]);

                if (groupChats.size() == 0){
                    ServerLogger.log("Could not find chat: " + args[0]);
                    return;
                }

                if (groupChats.size() == 1){
                    GroupChat groupChat = groupChats.get(0);

                    Message lastMessage = getMessagesDatabase().getLastNotNullMessageFromChat(groupChat);
                    ArrayList<String> participants = new ArrayList<>();

                    for (Handle handle : groupChat.getParticipants()) {
                        participants.add(handle.getHandleID());
                    }

                    ServerLogger.log("Group Chat Name: " + groupChat.getDisplayName());
                    ServerLogger.log("Participants: " + StringUtils.join(participants, ", ", 2));

                    if (lastMessage == null) {
                        ServerLogger.log("Last Message from Chat: Null Message");
                    } else {
                        ServerLogger.log("Last Message from Chat: " + lastMessage.getText());
                    }
                    return;
                }

                if (groupChats.size() > 1) {
                    ServerLogger.log("Multiple group chats have the name: " + args[0] + "!");
                    ServerLogger.log("Please choose which group chat you want to look up information on!");
                    ServerLogger.log("Example Usage: chatinfo \"Group Name\" 2");

                    ServerLogger.emptyLine();
                    printChatOptions(groupChats);
                }
            }else if(args.length == 2){
                List<GroupChat> groupChats = getMessagesDatabase().getGroupChatsByName(args[0]);

                if (groupChats.size() == 0){
                    ServerLogger.log("Could not find group chat: " + args[0]);
                    return;
                }

                if (groupChats.size() == 1){
                    ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                    ServerLogger.log("Example Usage: chatinfo \"Group Name\" 2");
                    return;
                }

                Integer i;

                try {
                    i = Integer.parseInt(args[1]);
                }catch (Exception ex){
                    ServerLogger.log(args[1] + " is not a number! Make sure you choose a valid option based off the list below.");
                    ServerLogger.emptyLine();
                    printChatOptions(groupChats);
                    return;
                }

                GroupChat groupChat;

                try {
                    groupChat = groupChats.get(i - 1);
                }catch (Exception ex){
                    ServerLogger.log(args[1] + " is not a valid number for your chat list options!");
                    ServerLogger.emptyLine();
                    printChatOptions(groupChats);
                    return;
                }

                Message lastMessage = getMessagesDatabase().getLastNotNullMessageFromChat(groupChat);
                ArrayList<String> participants = new ArrayList<>();

                for (Handle handle : groupChat.getParticipants()){
                    participants.add(handle.getHandleID());
                }

                ServerLogger.log("Group Chat Name: " + groupChat.getDisplayName());
                ServerLogger.log("Participants: " + StringUtils.join(participants, ", ", 2));

                if (lastMessage == null){
                    ServerLogger.log("Last Message from Chat: Null Message");
                }else {
                    ServerLogger.log("Last Message from Chat: " + lastMessage.getText());
                }
            }else if (args.length > 2){
                ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
                ServerLogger.log("Example Usage: chatinfo \"(123) 456-7890 \"");
            }
        }catch(Exception ex){
            ServerLogger.error("An error occurred while fetching the Messages database", ex);
        }
    }

    private void printChatOptions(List<GroupChat> groupChats){
        int i = 1;

        try {
            ServerLogger.log("Chat Option List");
            ServerLogger.emptyLine();

            for (GroupChat chat : groupChats) {
                Message lastMessage = getMessagesDatabase().getLastNotNullMessageFromChat(chat);
                String lastMessageText;

                if (lastMessage == null){
                    lastMessageText = "Null Message";
                }else {
                    lastMessageText = lastMessage.getText();
                }

                ServerLogger.log(i + ". " + chat.getDisplayName() + " - Last Message: " + lastMessageText);
                i++;
            }
        }catch (Exception ex){
            ServerLogger.error("An error occurred while fetching chat data from the database", ex);
        }
    }
}