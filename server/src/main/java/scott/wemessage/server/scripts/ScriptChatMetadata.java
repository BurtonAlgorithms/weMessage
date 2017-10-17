package scott.wemessage.server.scripts;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.commons.utils.StringUtils;
import scott.wemessage.server.ServerLogger;
import scott.wemessage.server.database.MessagesDatabase;
import scott.wemessage.server.messages.Handle;
import scott.wemessage.server.messages.chat.GroupChat;

public class ScriptChatMetadata {

    private GroupChat chat;

    public ScriptChatMetadata(GroupChat chat){
        this.chat = chat;
    }

    public int getAlgorithmicRow(MessagesDatabase messagesDatabase){
        try {
            return messagesDatabase.getChatRowPositionByRowId(chat.getRowID());
        }catch (Exception ex){
            ServerLogger.error("An error occurred while fetching chat data from the database", ex);
            return -1;
        }
    }

    public String getGuid(){
        return chat.getGuid();
    }

    public String getNameCheck(){
        if (StringUtils.isEmpty(chat.getDisplayName())){
            List<String> participants = new ArrayList<>();

            for (Handle h : chat.getParticipants()){
                participants.add(h.getHandleID());
            }
            return StringUtils.join(participants, ",", 1);
        }else {
            return chat.getDisplayName();
        }
    }

    public boolean getNoNameFlag(){
        return StringUtils.isEmpty(chat.getDisplayName());
    }
}