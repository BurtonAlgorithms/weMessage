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

package scott.wemessage.app.ui.view.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import scott.wemessage.R;

public class FontButton extends AppCompatButton {

    public FontButton(Context context){
        super(context);
    }

    public FontButton(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontButton);
        String fontName = array.getString(R.styleable.FontButton_buttonFontName);
        array.recycle();

        setFont(fontName);
    }

    public FontButton(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontButton);
        String fontName = array.getString(R.styleable.FontButton_buttonFontName);
        array.recycle();

        setFont(fontName);
    }

    public void setFont(String fontName){
        FontType fontType = FontType.getTypeFromName(fontName);

        if (fontType != null){
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontType.getFontFile());
            setTypeface(font);
        }
    }
}