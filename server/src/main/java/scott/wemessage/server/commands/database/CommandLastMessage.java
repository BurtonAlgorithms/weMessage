package scott.wemessage.server.commands.database;

import scott.wemessage.server.commands.CommandManager;
import scott.wemessage.server.messages.Attachment;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.Message;
import scott.wemessage.server.messages.chat.ChatBase;
import scott.wemessage.server.messages.chat.GroupChat;
import scott.wemessage.server.messages.chat.PeerChat;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.server.utils.LoggingUtils;
import scott.wemessage.commons.utils.StringUtils;

import java.util.ArrayList;

public class CommandLastMessage extends DatabaseCommand {

    public CommandLastMessage(CommandManager manager){
        super(manager, "lastmessage", "Gets information about the last message sent", new String[]{ "lastmsg", "getlastmessage", "lastmessagesent" });
    }

    public void execute(String[] args){
        try {
            Message message = getMessagesDatabase().getLastMessageSent();

            if (message == null){
                LoggingUtils.log("The last message sent was a null message (or an action).");
                return;
            }

            if (message.getText() == null){
                LoggingUtils.log("Text: Empty Message");
            }else {
                LoggingUtils.log("Text: " + message.getText());
            }

            LoggingUtils.log("GUID: " + message.getGuid());
            LoggingUtils.log("Database Row ID: " + message.getRowID());
            LoggingUtils.log("Date Sent: " + DateUtils.getSimpleStringFromDate(message.getModernDateSent()));
            LoggingUtils.log("Date Delivered: " + DateUtils.getSimpleStringFromDate(message.getModernDateDelivered()));
            LoggingUtils.log("Date Read: " + DateUtils.getSimpleStringFromDate(message.getModernDateRead()));
            LoggingUtils.log("Has Errored: " + StringUtils.uppercaseFirst(Boolean.toString(message.hasErrored())));
            LoggingUtils.log("Is Sent: " + StringUtils.uppercaseFirst(Boolean.toString(message.isSent())));
            LoggingUtils.log("Is Delivered: " + StringUtils.uppercaseFirst(Boolean.toString(message.isDelivered())));
            LoggingUtils.log("Is Read: " + StringUtils.uppercaseFirst(Boolean.toString(message.isRead())));
            LoggingUtils.log("Is Finished: " + StringUtils.uppercaseFirst(Boolean.toString(message.isFinished())));
            LoggingUtils.log("Is From Me: " + StringUtils.uppercaseFirst(Boolean.toString(message.isFromMe())));
            LoggingUtils.log("Has Attachments: " + StringUtils.uppercaseFirst(Boolean.toString(message.hasAttachments())));

            if (message.getHandle() != null) {
                LoggingUtils.emptyLine();
                LoggingUtils.log("Handle Info: ");
                LoggingUtils.log("Handle Account: " + message.getHandle().getHandleID());
                LoggingUtils.log("Country: " + message.getHandle().getCountry());
                LoggingUtils.log("Database Row ID: " + message.getHandle().getRowID());
                LoggingUtils.log("Handle: " + message.getHandle().getHandleID());
            }
            if (!message.getAttachments().isEmpty()) {
                LoggingUtils.emptyLine();
                LoggingUtils.log("Attachments:");
                for (Attachment a : message.getAttachments()) {
                    LoggingUtils.emptyLine();
                    LoggingUtils.log("Transfer Name: " + a.getTransferName());
                    LoggingUtils.log("File Location: " + a.getFileLocation());
                    LoggingUtils.log("File Type: " + a.getFileType());
                    LoggingUtils.log("Total Bytes: " + a.getTotalBytes());
                }
            }

            ChatBase chatBase = message.getChat();

            LoggingUtils.emptyLine();
            if (chatBase instanceof PeerChat){
                LoggingUtils.log("Peer: " + ((PeerChat)chatBase).getPeer().getHandleID());
            }
            if (chatBase instanceof GroupChat){
                GroupChat groupChat = (GroupChat) chatBase;
                ArrayList<String>participants = new ArrayList<>();

                for (Handle handle : groupChat.getParticipants()){
                    participants.add(handle.getHandleID());
                }

                LoggingUtils.log("Group Chat Name: " + groupChat.getDisplayName());
                LoggingUtils.log("Participants: " + StringUtils.join(participants, ", ", 2));
            }
        }catch(Exception ex){
            LoggingUtils.error("An error occurred while fetching the messages database", ex);
        }
    }
}