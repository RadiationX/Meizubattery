package ru.radiationx.meizubattery;

import android.content.Context;
import android.graphics.Canvas;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by radiationx on 10.10.16.
 */

public class BibleChapterView extends View {

    private StaticLayout staticLayout;

    public BibleChapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BibleChapterView(Context context) {
        super(context);
    }


    public void setStaticLayout(StaticLayout layout) {

        this.staticLayout = layout;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.save();

        if (staticLayout != null) {
            staticLayout.draw(canvas);
        }

        //canvas.restore();
        Log.d("kek", "set height "+canvas.getHeight()+ " : "+staticLayout.getHeight());
        getLayoutParams().height = staticLayout.getHeight();

    }

}
