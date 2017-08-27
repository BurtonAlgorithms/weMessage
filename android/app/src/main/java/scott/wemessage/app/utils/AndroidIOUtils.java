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

    public static String getChatIconUri(Chat chat, IconSize iconSize){
        if (chat.getChatType() == Chat.ChatType.PEER){
            PeerChat peerChat = (PeerChat) chat;

            if (peerChat.getContact().getContactPictureFileLocation() == null){
                return getDefaultContactUri(iconSize);
            }else if (StringUtils.isEmpty(peerChat.getContact().getContactPictureFileLocation().getFileLocation())){
                return getDefaultContactUri(iconSize);
            }else {
                return Uri.fromFile(peerChat.getContact().getContactPictureFileLocation().getFile()).toString();
            }
        }else {
            GroupChat groupChat = (GroupChat) chat;

            if (groupChat.getChatPictureFileLocation() == null){
                return getDefaultChatUri(iconSize);
            } else if (StringUtils.isEmpty(groupChat.getChatPictureFileLocation().getFileLocation())){
                return getDefaultChatUri(iconSize);
            } else {
                return Uri.fromFile(groupChat.getChatPictureFileLocation().getFile()).toString();
            }
        }
    }

    public static String getContactIconUri(Contact contact, IconSize iconSize){
        try {
            if (contact.getContactPictureFileLocation() == null) {
                return getDefaultContactUri(iconSize);
            } else if (StringUtils.isEmpty(contact.getContactPictureFileLocation().getFileLocation())) {
                return getDefaultContactUri(iconSize);
            } else {
                return Uri.fromFile(contact.getContactPictureFileLocation().getFile()).toString();
            }
        }catch (Exception ex){
            return getDefaultContactUri(iconSize);
        }
    }

    public static String getDefaultContactUri(IconSize iconSize){
        int resId;

        if (iconSize == IconSize.NORMAL){
            resId = R.drawable.ic_default_contact;
        }else if (iconSize == IconSize.LARGE){
            resId = R.drawable.ic_default_contact_large;
        }else {
            resId = R.drawable.ic_default_contact;
        }

        return AndroidIOUtils.getUriFromResource(weMessage.get(), resId).toString();
    }

    public static String getDefaultChatUri(IconSize iconSize){
        int resId;

        if (iconSize == IconSize.NORMAL){
            resId = R.drawable.ic_default_group_chat;
        }else if (iconSize == IconSize.LARGE){
            resId = R.drawable.ic_default_group_chat_large;
        }else {
            resId = R.drawable.ic_default_group_chat;
        }

        return AndroidIOUtils.getUriFromResource(weMessage.get(), resId).toString();
    }

    public enum IconSize {
        NORMAL,
        LARGE
    }
}