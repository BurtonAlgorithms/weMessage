package scott.wemessage.app.utils.reflection;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.lang.reflect.Field;
import java.util.List;

import scott.wemessage.app.AppLogger;

/** TODO: DO NOT LET PRO GUARD CHANGE THIS **/

public class ChatKitHelper {

    private static final String DIALOGS_FIELD_NAME = "items";

    public static String getChatIdFromPosition(DialogsListAdapter adapter, int position){
        try {
            Field field = adapter.getClass().getDeclaredField(DIALOGS_FIELD_NAME);
            field.setAccessible(true);

            List<IDialog> dialogList = (List<IDialog>) field.get(adapter);

            return dialogList.get(position).getId();
        }catch(Exception ex){
            AppLogger.error("An error occurred with the reflection in ChatKit Reflection Lib", ex);
            return null;
        }
    }

    public static Integer getChatPositionFromId(DialogsListAdapter adapter, String id){
        try {
            Field field = adapter.getClass().getDeclaredField(DIALOGS_FIELD_NAME);
            field.setAccessible(true);

            List<IDialog> dialogList = (List<IDialog>) field.get(adapter);

            for (IDialog dialog : dialogList){
                if (dialog.getId().equals(id)){
                    return dialogList.indexOf(dialog);
                }
            }
        }catch (Exception ex){
            AppLogger.error("An error occurred with the reflection in ChatKit Reflection Lib", ex);
        }
        return null;
    }
}
