package scott.wemessage.server.commands.database;

import java.util.ArrayList;

import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.Attachment;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.ChatBase;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;

public class CommandLastMessage extends DatabaseCommand {

    public CommandLastMessage(CommandManager manager){
        super(manager, "lastmessage", "Gets information about the last message sent", new String[]{ "lastmsg", "getlastmessage", "lastmessagesent" });
    }

    public void execute(String[] args){
        try {
            Message message = getMessagesDatabase().getLastMessageSent();

            if (message == null){
                ServerLogger.log("The last message sent was a null message (or an action).");
                return;
            }

            if (message.getText() == null){
                ServerLogger.log("Text: Empty Message");
            }else {
                ServerLogger.log("Text: " + message.getText());
            }

            ServerLogger.log("GUID: " + message.getGuid());
            ServerLogger.log("Database Row ID: " + message.getRowID());
            ServerLogger.log("Date Sent: " + DateUtils.getSimpleStringFromDate(message.getModernDateSent()));
            ServerLogger.log("Date Delivered: " + DateUtils.getSimpleStringFromDate(message.getModernDateDelivered()));
            ServerLogger.log("Date Read: " + DateUtils.getSimpleStringFromDate(message.getModernDateRead()));
            ServerLogger.log("Has Errored: " + StringUtils.uppercaseFirst(Boolean.toString(message.hasErrored())));
            ServerLogger.log("Is Sent: " + StringUtils.uppercaseFirst(Boolean.toString(message.isSent())));
            ServerLogger.log("Is Delivered: " + StringUtils.uppercaseFirst(Boolean.toString(message.isDelivered())));
            ServerLogger.log("Is Read: " + StringUtils.uppercaseFirst(Boolean.toString(message.isRead())));
            ServerLogger.log("Is Finished: " + StringUtils.uppercaseFirst(Boolean.toString(message.isFinished())));
            ServerLogger.log("Is From Me: " + StringUtils.uppercaseFirst(Boolean.toString(message.isFromMe())));
            ServerLogger.log("Has Attachments: " + StringUtils.uppercaseFirst(Boolean.toString(message.hasAttachments())));

            if (message.getHandle() != null) {
                ServerLogger.emptyLine();
                ServerLogger.log("Handle Info: ");
                ServerLogger.log("Handle Account: " + message.getHandle().getHandleID());
                ServerLogger.log("Country: " + message.getHandle().getCountry());
                ServerLogger.log("Database Row ID: " + message.getHandle().getRowID());
                ServerLogger.log("Handle: " + message.getHandle().getHandleID());
            }
            if (!message.getAttachments().isEmpty()) {
                ServerLogger.emptyLine();
                ServerLogger.log("Attachments:");
                for (Attachment a : message.getAttachments()) {
                    ServerLogger.emptyLine();
                    ServerLogger.log("Transfer Name: " + a.getTransferName());
                    ServerLogger.log("File Location: " + a.getFileLocation());
                    ServerLogger.log("File Type: " + a.getFileType());
                    ServerLogger.log("Total Bytes: " + a.getTotalBytes());
                }
            }

            ChatBase chatBase = message.getChat();

            ServerLogger.emptyLine();
            if (chatBase instanceof PeerChat){
                ServerLogger.log("Peer: " + ((PeerChat)chatBase).getPeer().getHandleID());
            }
            if (chatBase instanceof GroupChat){
                GroupChat groupChat = (GroupChat) chatBase;
                ArrayList<String>participants = new ArrayList<>();

                for (Handle handle : groupChat.getParticipants()){
                    participants.add(handle.getHandleID());
                }

                ServerLogger.log("Group Chat Name: " + groupChat.getDisplayName());
                ServerLogger.log("Participants: " + StringUtils.join(participants, ", ", 2));
            }
        }catch(Exception ex){
            ServerLogger.error("An error occurred while fetching the messages database", ex);
        }
    }
}