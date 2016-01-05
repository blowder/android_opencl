package com.shl.checkpin.android.canvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class CanvasView extends View {
    private Bitmap backgroundImage;
    private List<Circle> circles = new ArrayList<Circle>();
    Paint linePaint = new Paint();


    public CanvasView(Context context) {
        super(context);
        initLinePaint();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLinePaint();

    }

    private void initLinePaint() {
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;
        invalidate();
        requestLayout();
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundImage != null)
            canvas.drawBitmap(backgroundImage, 0, 0, null);
        //background.recycle();
        //background=null;
        for (Circle circle : circles)
            if (circle.getNext() != null)
                canvas.drawLine(circle.getX(), circle.getY(), circle.getNext().getX(), circle.getNext().getY(), linePaint);
        for (Circle circle : circles)
            circle.draw(canvas);
    }
}
