package scott.wemessage.app.ui.view.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import scott.wemessage.R;

public class FontTextView extends AppCompatTextView {

    public FontTextView(Context context){
        super(context);
    }

    public FontTextView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontTextView);
        String fontName = array.getString(R.styleable.FontTextView_fontName);


        setFont(fontName);
        array.recycle();
    }

    public FontTextView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontTextView);
        String fontName = array.getString(R.styleable.FontTextView_fontName);


        setFont(fontName);
        array.recycle();
    }

    public void setFont(String fontName){
        FontType fontType = FontType.getTypeFromName(fontName);

        if (fontType != null){
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontType.getFontFile());
            setTypeface(font);
        }
    }
}