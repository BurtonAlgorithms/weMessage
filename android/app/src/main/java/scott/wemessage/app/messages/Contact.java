package scott.wemessage.app.messages;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Contact {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private Handle handle;
    private FileLocationContainer contactPictureFileLocation;

    public Contact(UUID uuid, String firstName, String lastName, Handle handle, FileLocationContainer contactPictureFileLocation) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
        this.contactPictureFileLocation = contactPictureFileLocation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Handle getHandle() {
        return handle;
    }

    public FileLocationContainer getContactPictureFileLocation() {
        return contactPictureFileLocation;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public void setContactPictureFileLocation(FileLocationContainer contactPictureFileLocation) {
        this.contactPictureFileLocation = contactPictureFileLocation;
    }
}