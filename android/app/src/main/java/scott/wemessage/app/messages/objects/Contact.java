package scott.wemessage.app.messages.objects;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;

public class Contact {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private Handle handle;
    private FileLocationContainer contactPictureFileLocation;

    public Contact(){

    }

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

    public Contact setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Contact setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Contact setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Contact setHandle(Handle handle) {
        this.handle = handle;
        return this;
    }

    public Contact setContactPictureFileLocation(FileLocationContainer contactPictureFileLocation) {
        this.contactPictureFileLocation = contactPictureFileLocation;
        return this;
    }
}