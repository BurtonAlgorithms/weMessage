/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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