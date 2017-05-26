package scott.wemessage.server.database;

import scott.wemessage.server.messages.Message;

import java.util.List;

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