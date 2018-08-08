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
        TextView titleView = findViewById(R.id.progressDialogTitle);
        titleView.setText(title);

        this.title = title;
    }

    public void setMessage(String message){
        TextView messageView = findViewById(R.id.progressDialogMessage);
        messageView.setText(message);

        this.message = message;
    }

    public void hideButton(){
        Button actionButton = findViewById(R.id.progressDialogButton);
        LinearLayout buttonContainer = findViewById(R.id.progressDialogButtonContainer);

        actionButton.setClickable(false);
        actionButton.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.GONE);
    }

    public void showButton(){
        Button actionButton = findViewById(R.id.progressDialogButton);
        LinearLayout buttonContainer = findViewById(R.id.progressDialogButtonContainer);

        actionButton.setClickable(true);
        actionButton.setVisibility(View.VISIBLE);
        buttonContainer.setVisibility(View.VISIBLE);
    }

    public void setButton(String buttonName, OnClickListener onClickListener){
        Button actionButton = findViewById(R.id.progressDialogButton);

        actionButton.setText(buttonName);
        actionButton.setOnClickListener(onClickListener);
    }
}