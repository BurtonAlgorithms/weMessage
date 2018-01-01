package scott.wemessage.commons.connection;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import scott.wemessage.commons.connection.json.message.JSONContact;

public class ContactBatch {

    @SerializedName("contacts")
    private List<JSONContact> contacts;

    public ContactBatch(List<JSONContact> contacts){
        this.contacts = contacts;
    }

    public List<JSONContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<JSONContact> contacts) {
        this.contacts = contacts;
    }
}
