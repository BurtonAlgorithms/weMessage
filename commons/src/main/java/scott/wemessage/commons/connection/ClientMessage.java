/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.commons.connection;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import scott.wemessage.commons.utils.ByteArrayAdapter;

public class ClientMessage extends ConnectionMessage {

    private static ByteArrayAdapter byteArrayAdapter;

    @SerializedName("incomingJson")
    private String incomingJson;

    public ClientMessage(String messageUuid, String incomingJson){
        super(messageUuid);
        this.incomingJson = incomingJson;
    }

    public static void setByteArrayAdapter(ByteArrayAdapter adapter){
        byteArrayAdapter = adapter;
    }

    public Object getIncoming(Class<?> objectClass) {
        Type type = TypeToken.get(objectClass).getType();
        return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().fromJson(incomingJson, type);
    }

    public boolean isJsonOfType(Class<?> type){
        try {
            return !(new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, byteArrayAdapter).create().toJson(getIncoming(type), type).equals("{}"));
        }catch(Exception ex){
            return false;
        }
    }

    public void setIncomingJson(String incomingJson) {
        this.incomingJson = incomingJson;
    }
}