package scott.wemessage.server.commands.database;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;
import scott.wemessage.server.utils.LoggingUtils;
import scott.wemessage.commons.utils.StringUtils;

import java.util.ArrayList;

public class CommandChatInfo extends DatabaseCommand {

    public CommandChatInfo(CommandManager manager){
        super(manager, "chatinfo", "Gets information about a conversation", new String[]{ "getchat" });
    }

    public void execute(String[] args){
        if(args.length == 0){
            LoggingUtils.log("Please enter the phone number, email address, or group chat name you want to look up information for.");
            LoggingUtils.emptyLine();
            LoggingUtils.log("If you are trying to find a group chat and you have multiple groups with the same name");
            LoggingUtils.log("Provide the last message sent to the group chat as well to eliminate conflicts.");
            return;
        }
        try {
            if(args.length == 1) {
                PeerChat peerChat = getMessagesDatabase().getChatByAccount(args[0]);

                if (peerChat != null) {
                    Message lastMessage = getMessagesDatabase().getLastMessageFromChat(peerChat);
                    LoggingUtils.log("Member: " + peerChat.getPeer().getHandleID());

                    if (lastMessage == null){
                        LoggingUtils.log("Last Message from Chat: Null Message");
                    }else {
                        LoggingUtils.log("Last Message from Chat: " + lastMessage.getText());
                    }

                    LoggingUtils.log("GUID: " + peerChat.getGuid());
                    LoggingUtils.log("Chat ID: " + peerChat.getGroupID());
                    LoggingUtils.log("Database Row ID: " + peerChat.getRowID());
                    return;
                }

                GroupChat groupChat = getMessagesDatabase().getGroupChatByName(args[0], null);

                if (groupChat != null){
                    Message lastMessage = getMessagesDatabase().getLastMessageFromChat(groupChat);
                    ArrayList<String> participants = new ArrayList<>();

                    for (Handle handle : groupChat.getParticipants()){
                        participants.add(handle.getHandleID());
                    }

                    LoggingUtils.log("Group Chat Name: " + groupChat.getDisplayName());
                    LoggingUtils.log("Participants: " + StringUtils.join(participants, ", ", 2));

                    if (lastMessage == null){
                        LoggingUtils.log("Last Message from Chat: Null Message");
                    }else {
                        LoggingUtils.log("Last Message from Chat: " + lastMessage.getText());
                    }

                    LoggingUtils.log("GUID: " + groupChat.getGuid());
                    LoggingUtils.log("Chat ID: " + groupChat.getGroupID());
                    LoggingUtils.log("Database Row ID: " + groupChat.getRowID());
                }else {
                    LoggingUtils.log("Could not find chat: " + args[0]);
                }
            }else if(args.length == 2){
                GroupChat groupChat = getMessagesDatabase().getGroupChatByName(args[0], args[1]);
                Message lastMessage = getMessagesDatabase().getLastMessageFromChat(groupChat);
                ArrayList<String> participants = new ArrayList<>();

                if (groupChat == null){
                    LoggingUtils.log("Could not find group chat: " + args[0]);
                    return;
                }

                for (Handle handle : groupChat.getParticipants()){
                    participants.add(handle.getHandleID());
                }

                LoggingUtils.log("Group Chat Name: " + groupChat.getDisplayName());
                LoggingUtils.log("Participants: " + StringUtils.join(participants, ", ", 2));

                if (lastMessage == null){
                    LoggingUtils.log("Last Message from Chat: Null Message");
                }else {
                    LoggingUtils.log("Last Message from Chat: " + lastMessage.getText());
                }

                LoggingUtils.log("GUID: " + groupChat.getGuid());
                LoggingUtils.log("Chat ID: " + groupChat.getGroupID());
                LoggingUtils.log("Database Row ID: " + groupChat.getRowID());
            }
        }catch(Exception ex){
            LoggingUtils.error("An error occurred while fetching the Messages database", ex);
        }
    }
}