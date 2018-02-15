package scott.wemessage.app.ui.view.dialog;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
        TextView view = findViewById(R.id.alertDialogTitle);
        view.setText(title);

        this.title = title;
    }

    public void setMessage(String message){
        TextView view = findViewById(R.id.alertDialogMessage);
        view.setText(message);

        this.message = message;
    }

    public void linkify(){
        TextView view = findViewById(R.id.alertDialogMessage);
        view.setText(Html.fromHtml(message.replaceAll("\n", "<br />")));
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }
}