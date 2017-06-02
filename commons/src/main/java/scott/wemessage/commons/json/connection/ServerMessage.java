package scott.wemessage.commons.json.connection;

import scott.wemessage.commons.utils.ByteArrayAdapter;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ServerMessage {

    private String messageUuid;
    private String outgoingJson;

    public ServerMessage(String messageUuid, String outgoingJson){
        this.messageUuid = messageUuid;
        this.outgoingJson = outgoingJson;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public Object getOutgoing(Class<?> objectClass, ByteArrayAdapter byteArrayAdapter){
        Type type = TypeToken.get(objectClass).getType();
        return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(outgoingJson, type);
    }

    public boolean isJsonOfType(Class<?> type, ByteArrayAdapter byteArrayAdapter){
        try {
            getOutgoing(type, byteArrayAdapter);
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public void setOutgoingJson(String outgoingJson) {
        this.outgoingJson = outgoingJson;
    }
}