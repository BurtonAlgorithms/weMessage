package scott.wemessage.app.ui.view.chat;

import android.content.Context;
import android.util.AttributeSet;

import com.flipboard.bottomsheet.BottomSheetLayout;

import scott.wemessage.app.ui.CreateChatFragment;

public class CreateChatBottomSheet extends BottomSheetLayout {

    private CreateChatFragment.SelectedNameView selectedNameView;

    public CreateChatBottomSheet(Context context) {
        super(context);
    }

    public CreateChatBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CreateChatBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CreateChatFragment.SelectedNameView getSelectedNameView(){
        return selectedNameView;
    }

    public void setSelectedNameView(CreateChatFragment.SelectedNameView selectedNameView){
        this.selectedNameView = selectedNameView;
    }
}