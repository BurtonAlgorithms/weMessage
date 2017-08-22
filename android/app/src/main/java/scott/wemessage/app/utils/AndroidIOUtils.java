package scott.wemessage.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

import java.io.File;

import scott.wemessage.R;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.utils.StringUtils;

public class AndroidIOUtils {

    public static Uri getUriFromResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        Resources res = context.getResources();
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(resId) + '/' + res.getResourceTypeName(resId) + '/' + res.getResourceEntryName(resId));

        return resUri;
    }

    public static Uri getUriFromFile(File file){
        return Uri.fromFile(file);
    }

    public static String getChatIconUri(Chat chat){
        if (chat.getChatType() == Chat.ChatType.PEER){
            PeerChat peerChat = (PeerChat) chat;

            if (peerChat.getContact().getContactPictureFileLocation() == null){
                return getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            }else if (StringUtils.isEmpty(peerChat.getContact().getContactPictureFileLocation().getFileLocation())){
                return getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            }else {
                return Uri.fromFile(peerChat.getContact().getContactPictureFileLocation().getFile()).toString();
            }
        }else {
            GroupChat groupChat = (GroupChat) chat;

            if (groupChat.getChatPictureFileLocation() == null){
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_group_chat).toString();
            } else if (StringUtils.isEmpty(groupChat.getChatPictureFileLocation().getFileLocation())){
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_group_chat).toString();
            } else {
                return Uri.fromFile(groupChat.getChatPictureFileLocation().getFile()).toString();
            }
        }
    }

    public static String getContactIconUri(Contact contact){
        try {
            if (contact.getContactPictureFileLocation() == null) {
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            } else if (StringUtils.isEmpty(contact.getContactPictureFileLocation().getFileLocation())) {
                return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
            } else {
                return Uri.fromFile(contact.getContactPictureFileLocation().getFile()).toString();
            }
        }catch (Exception ex){
            return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
        }
    }

    public static String getDefaultContactUri(){
        return AndroidIOUtils.getUriFromResource(weMessage.get(), R.drawable.ic_default_contact).toString();
    }
}