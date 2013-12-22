package com.timetracker.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by artem on 12/22/13.
 */
public class MainButtonView extends ImageView {
    private int state = 0;
    private boolean debugEnabled = true;
    private Paint textPaint;
    private Paint mainColorPaint;

    {
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);

        mainColorPaint = new Paint();
        mainColorPaint.setColor(Color.YELLOW);
    }


    public MainButtonView(Context context) {
        super(context);
    }

    public MainButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        state++;
        if (state >= 360)
            state = 0;
        paint(canvas);

        if (debugEnabled) {
            if (state < 90) {
                canvas.drawText(String.valueOf(state + 270), 5, 10, textPaint);
            } else {
                canvas.drawText(String.valueOf(state - 90), 5, 10, textPaint);
            }
            canvas.drawText(String.valueOf(getFinalAngle()), 5, 20, textPaint);
        }
        super.onDraw(canvas);
    }

    private void paint(Canvas canvas) {

        Rect clipBounds = canvas.getClipBounds();
        final float finalEnd = getFinalAngle();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        if (finalEnd < 90) {
            int centerX = (clipBounds.right / 2);
            int centerY = (int) ((clipBounds.bottom / 2) * (1 - finalEnd / 90));
            path.moveTo(centerX, centerY);//todo center here!
            path.lineTo(centerX, clipBounds.top);
            path.arcTo(new RectF(clipBounds), 270, finalEnd);
            path.lineTo(centerX, centerY);
        } else if (finalEnd < 270) {
            canvas.drawArc(new RectF(clipBounds), 270, finalEnd, false, mainColorPaint);
        } else {
            int centerX = (clipBounds.right / 2);
            int centerY = (int) ((clipBounds.bottom / 2) * (finalEnd / 90 - 3));
            path.moveTo(centerX, centerY);//todo center here!
            path.lineTo(centerX, clipBounds.top);
            path.arcTo(new RectF(clipBounds), 270, finalEnd);
            path.lineTo(centerX, centerY);
        }
        path.close();

        canvas.drawPath(path, paint);
    }

    private float getFinalAngle() {
        final float finalEnd;
        if (state > 90) {
            finalEnd = state - 90;
        } else {
            finalEnd = 270 + state;
        }
        return finalEnd;
    }
}
