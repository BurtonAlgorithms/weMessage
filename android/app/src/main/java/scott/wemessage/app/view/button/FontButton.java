package scott.wemessage.app.view.button;

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
        String fontName = array.getString(R.styleable.FontButton_buttonFont);
        array.recycle();

        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        setTypeface(font);
    }

    public FontButton(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontButton);
        String fontName = array.getString(R.styleable.FontButton_buttonFont);
        array.recycle();

        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        setTypeface(font);
    }

    public void setFont(String fontName){
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
        setTypeface(font);
    }
}