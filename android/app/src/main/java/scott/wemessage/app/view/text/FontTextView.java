package scott.wemessage.app.view.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import scott.wemessage.R;

public class FontTextView extends AppCompatTextView {

    public FontTextView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.FontTextView);
        String fontName = array.getString(R.styleable.FontTextView_font);
        array.recycle();

        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        setTypeface(font);
    }
}