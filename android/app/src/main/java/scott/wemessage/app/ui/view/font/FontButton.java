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