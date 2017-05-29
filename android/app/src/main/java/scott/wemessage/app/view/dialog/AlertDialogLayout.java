package scott.wemessage.app.view.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import scott.wemessage.R;

public class AlertDialogLayout extends LinearLayout {

    private String title;
    private String message;

    public AlertDialogLayout(Context context){
        super(context);
    }

    public AlertDialogLayout(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public AlertDialogLayout(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public void setTitle(String title){
        TextView view = (TextView) findViewById(R.id.alertDialogTitle);
        view.setText(title);

        this.title = title;
    }

    public void setMessage(String message){
        TextView view = (TextView) findViewById(R.id.alertDialogMessage);
        view.setText(message);

        this.message = message;
    }
}