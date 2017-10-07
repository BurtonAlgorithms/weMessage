package scott.wemessage.commons.connection;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import scott.wemessage.commons.utils.ByteArrayAdapter;

public class ServerMessage extends ConnectionMessage {

    private static ByteArrayAdapter byteArrayAdapter;

    private String outgoingJson;

    public ServerMessage(String messageUuid, String outgoingJson){
        super(messageUuid);
        this.outgoingJson = outgoingJson;
    }

    public static void setByteArrayAdapter(ByteArrayAdapter adapter){
        byteArrayAdapter = adapter;
    }

    public Object getOutgoing(Class<?> objectClass){
        Type type = TypeToken.get(objectClass).getType();
        return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(outgoingJson, type);
    }

    public boolean isJsonOfType(Class<?> type){
        try {
            return !(new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().toJson(getOutgoing(type), type).equals("{}"));
        }catch(Exception ex){
            return false;
        }
    }

    public void setOutgoingJson(String outgoingJson) {
        this.outgoingJson = outgoingJson;
    }
}