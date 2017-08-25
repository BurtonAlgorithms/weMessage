package scott.wemessage.commons.json.connection;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import scott.wemessage.commons.utils.ByteArrayAdapter;

public class ClientMessage extends ConnectionMessage {

    private String incomingJson;

    public ClientMessage(String messageUuid, String incomingJson){
        super(messageUuid);
        this.incomingJson = incomingJson;
    }

    public Object getIncoming(Class<?> objectClass, ByteArrayAdapter byteArrayAdapter) {
        Type type = TypeToken.get(objectClass).getType();
        return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(incomingJson, type);
    }

    public boolean isJsonOfType(Class<?> type, ByteArrayAdapter byteArrayAdapter){
        try {
            return !(new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().toJson(getIncoming(type, byteArrayAdapter), type).equals("{}"));
        }catch(Exception ex){
            return false;
        }
    }

    public void setIncomingJson(String incomingJson) {
        this.incomingJson = incomingJson;
    }
}