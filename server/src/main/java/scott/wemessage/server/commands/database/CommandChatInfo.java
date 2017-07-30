package scott.wemessage.server.commands.database;

import java.util.ArrayList;

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
            ServerLogger.emptyLine();
            ServerLogger.log("If you are trying to find a group chat and you have multiple groups with the same name");
            ServerLogger.log("Provide the last message sent to the group chat as well to eliminate conflicts.");
            return;
        }
        try {
            if(args.length == 1) {
                PeerChat peerChat = getMessagesDatabase().getChatByAccount(args[0]);

                if (peerChat != null) {
                    Message lastMessage = getMessagesDatabase().getLastMessageFromChat(peerChat);
                    ServerLogger.log("Member: " + peerChat.getPeer().getHandleID());

                    if (lastMessage == null){
                        ServerLogger.log("Last Message from Chat: Null Message");
                    }else {
                        ServerLogger.log("Last Message from Chat: " + lastMessage.getText());
                    }

                    ServerLogger.log("GUID: " + peerChat.getGuid());
                    ServerLogger.log("Chat ID: " + peerChat.getGroupID());
                    ServerLogger.log("Database Row ID: " + peerChat.getRowID());
                    return;
                }

                GroupChat groupChat = getMessagesDatabase().getGroupChatByName(args[0], null);

                if (groupChat != null){
                    Message lastMessage = getMessagesDatabase().getLastMessageFromChat(groupChat);
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

                    ServerLogger.log("GUID: " + groupChat.getGuid());
                    ServerLogger.log("Chat ID: " + groupChat.getGroupID());
                    ServerLogger.log("Database Row ID: " + groupChat.getRowID());
                }else {
                    ServerLogger.log("Could not find chat: " + args[0]);
                }
            }else if(args.length == 2){
                GroupChat groupChat = getMessagesDatabase().getGroupChatByName(args[0], args[1]);
                Message lastMessage = getMessagesDatabase().getLastMessageFromChat(groupChat);
                ArrayList<String> participants = new ArrayList<>();

                if (groupChat == null){
                    ServerLogger.log("Could not find group chat: " + args[0]);
                    return;
                }

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

                ServerLogger.log("GUID: " + groupChat.getGuid());
                ServerLogger.log("Chat ID: " + groupChat.getGroupID());
                ServerLogger.log("Database Row ID: " + groupChat.getRowID());
            }else if (args.length > 2){
                ServerLogger.log("There were too many arguments provided. Make sure your arguments are surrounded in \"quotation marks.\"");
            }
        }catch(Exception ex){
            ServerLogger.error("An error occurred while fetching the Messages database", ex);
        }
    }
}