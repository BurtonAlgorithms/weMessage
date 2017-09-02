package scott.wemessage.app.messages.objects;

import android.content.res.Resources;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.UUID;

import scott.wemessage.app.utils.FileLocationContainer;
import scott.wemessage.commons.utils.StringUtils;

public class Contact {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private Handle handle;
    private FileLocationContainer contactPictureFileLocation;
    private boolean isDoNotDisturb;
    private boolean isBlocked;

    public Contact(){

    }

    public Contact(UUID uuid, String firstName, String lastName, Handle handle, FileLocationContainer contactPictureFileLocation, boolean isDoNotDisturb, boolean isBlocked) {
        this.uuid = uuid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
        this.contactPictureFileLocation = contactPictureFileLocation;
        this.isDoNotDisturb = isDoNotDisturb;
        this.isBlocked = isBlocked;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUIDisplayName(){
        try {
            String fullString = "";

            if (!StringUtils.isEmpty(getFirstName())) {
                fullString = getFirstName();
            }

            if (!StringUtils.isEmpty(getLastName())) {
                fullString += " " + getLastName();
            }

            if (StringUtils.isEmpty(fullString)) {
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

                try {
                    Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(getHandle().getHandleID(), Resources.getSystem().getConfiguration().locale.getCountry());

                    fullString = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                }catch (Exception ex){
                    fullString = getHandle().getHandleID();
                }
            }

            return fullString;
        }catch(Exception ex){
            return "";
        }
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

    public boolean isDoNotDisturb() {
        return isDoNotDisturb;
    }

    public boolean isBlocked(){
        return isBlocked;
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

    public Contact setDoNotDisturb(boolean isDoNotDisturb) {
        this.isDoNotDisturb = isDoNotDisturb;
        return this;
    }

    public Contact setBlocked(boolean isBlocked){
        this.isBlocked = isBlocked;
        return this;
    }
}