package scott.wemessage.server.database;

import java.util.List;

import scott.wemessage.server.messages.Message;

public class DatabaseSnapshot {

    private List<Message> messages;

    public DatabaseSnapshot(List<Message> messages){
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }
    
    public Message getMessage(String guid){
        for (Message message : getMessages()){
            if (message.getGuid().equals(guid)){
                return message;
            }
        }
        return null;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}