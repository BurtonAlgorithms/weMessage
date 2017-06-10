package scott.wemessage.commons.json.connection;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import scott.wemessage.commons.utils.ByteArrayAdapter;

public class ServerMessage extends ConnectionMessage {

    private String outgoingJson;

    public ServerMessage(String messageUuid, String outgoingJson){
        super(messageUuid);
        this.outgoingJson = outgoingJson;
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

    public void setOutgoingJson(String outgoingJson) {
        this.outgoingJson = outgoingJson;
    }
}