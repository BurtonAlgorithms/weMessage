package scott.wemessage.commons.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import scott.wemessage.commons.crypto.Base64Wrapper;

public class ByteArrayAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    private Base64Wrapper base64Wrapper;

    public ByteArrayAdapter(Base64Wrapper base64Wrapper){
        this.base64Wrapper = base64Wrapper;
    }

    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return base64Wrapper.decodeString(json.getAsString());
    }

    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(base64Wrapper.encodeToString(src));
    }
}