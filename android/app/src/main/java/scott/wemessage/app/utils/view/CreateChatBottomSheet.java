package scott.wemessage.app.utils.view;

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

    public CreateChatBottomSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CreateChatFragment.SelectedNameView getSelectedNameView(){
        return selectedNameView;
    }

    public void setSelectedNameView(CreateChatFragment.SelectedNameView selectedNameView){
        this.selectedNameView = selectedNameView;
    }
}