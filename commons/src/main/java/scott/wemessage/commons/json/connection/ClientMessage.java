package scott.wemessage.commons.json.connection;

import scott.wemessage.commons.utils.ByteArrayAdapter;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ClientMessage {

    private String messageUuid;
    private String incomingJson;

    public ClientMessage(String messageUuid, String incomingJson){
        this.messageUuid = messageUuid;
        this.incomingJson = incomingJson;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public Object getIncoming(Class<?> objectClass, ByteArrayAdapter byteArrayAdapter) {
        Type type = TypeToken.get(objectClass).getType();
        return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(incomingJson, type);
    }

    public boolean isJsonOfType(Class<?> type, ByteArrayAdapter byteArrayAdapter){
        try {
            getIncoming(type, byteArrayAdapter);
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public void setIncomingJson(String incomingJson) {
        this.incomingJson = incomingJson;
    }
}