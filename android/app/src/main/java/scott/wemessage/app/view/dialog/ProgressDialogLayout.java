package scott.wemessage.app.view.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import scott.wemessage.R;

public class ProgressDialogLayout extends LinearLayout {

    private String title;
    private String message;

    public ProgressDialogLayout(Context context){
        super(context);
    }

    public ProgressDialogLayout(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public ProgressDialogLayout(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public void setTitle(String title){
        TextView titleView = (TextView) findViewById(R.id.progressDialogTitle);
        titleView.setText(title);

        this.title = title;
    }

    public void setMessage(String message){
        TextView messageView = (TextView) findViewById(R.id.progressDialogMessage);
        messageView.setText(message);

        this.message = message;
    }

    public void hideButton(){
        Button actionButton = (Button) findViewById(R.id.progressDialogButton);
        LinearLayout buttonContainer = (LinearLayout) findViewById(R.id.progressDialogButtonContainer);

        actionButton.setClickable(false);
        actionButton.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.GONE);
    }

    public void showButton(){
        Button actionButton = (Button) findViewById(R.id.progressDialogButton);
        LinearLayout buttonContainer = (LinearLayout) findViewById(R.id.progressDialogButtonContainer);

        actionButton.setClickable(true);
        actionButton.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.VISIBLE);
    }

    public void setButton(String buttonName, OnClickListener onClickListener){
        Button actionButton = (Button) findViewById(R.id.progressDialogButton);

        actionButton.setText(buttonName);
        actionButton.setOnClickListener(onClickListener);
    }
}