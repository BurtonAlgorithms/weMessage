package scott.wemessage.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

import scott.wemessage.R;
import scott.wemessage.app.WeApp;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.commons.utils.StringUtils;

public class AndroidIOUtils {

    /**
     * Creates a Uri which parses the given encoded URI string.
     * @param context The context
     * @param resId The res ID for the required URI
     * @throws NullPointerException if uriString is null
     * @return Uri for the resource ID
     */

    public static final Uri getUriFromResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        Resources res = context.getResources();

        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));

        return resUri;
    }

    public static String getChatIconUri(Chat chat){
        if (chat.getChatType() == Chat.ChatType.PEER){
            PeerChat peerChat = (PeerChat) chat;

            if (peerChat.getContact().getContactPictureFileLocation() == null){
                return getUriFromResource(WeApp.get(), R.drawable.ic_default_contact).toString();
            }else if (StringUtils.isEmpty(peerChat.getContact().getContactPictureFileLocation().getFileLocation())){
                return getUriFromResource(WeApp.get(), R.drawable.ic_default_contact).toString();
            }else {
                return Uri.fromFile(peerChat.getContact().getContactPictureFileLocation().getFile()).toString();
            }
        }else {
            GroupChat groupChat = (GroupChat) chat;

            if (groupChat.getChatPictureFileLocation() == null){
                return AndroidIOUtils.getUriFromResource(WeApp.get(), R.drawable.ic_default_group_chat).toString();
            } else if (StringUtils.isEmpty(groupChat.getChatPictureFileLocation().getFileLocation())){
                return AndroidIOUtils.getUriFromResource(WeApp.get(), R.drawable.ic_default_group_chat).toString();
            } else {
                return Uri.fromFile(groupChat.getChatPictureFileLocation().getFile()).toString();
            }
        }
    }
}