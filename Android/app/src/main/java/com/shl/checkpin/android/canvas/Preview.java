package com.shl.checkpin.android.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by sesshoumaru on 24.01.16.
 */
public class Preview {
    private Bitmap sourceImage;
    private Bitmap backgroundImage;
    private Point center = new Point();
    private int width;
    private int height;
    private int bb;

    public Preview(Bitmap sourceImage, Point center, int width, int height) {
        this.sourceImage = sourceImage;
        this.center = center;
        this.width = width;
        this.height = height;
        this.bb = Math.max(width, height);
    }


    public void draw(Canvas canvas, Point touchCoords) {
        initBackground(canvas);
        drawPreview(canvas, touchCoords);
        drawCross(canvas);
    }

    private Paint getLinePaint() {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        return p;
    }

    private void drawPreview(Canvas canvas, Point touchCoords) {
        Point realTouchCoords = new Point(touchCoords.x + bb, touchCoords.y + bb);
        Point ltRealCorner = getLTPoint(realTouchCoords);
        Bitmap preview = Bitmap.createBitmap(backgroundImage, ltRealCorner.x, ltRealCorner.y, width, height);

        Point ltCanvas = getLTPoint(center);
        canvas.drawBitmap(preview, ltCanvas.x, ltCanvas.y, null);
    }

    private void drawCross(Canvas canvas) {
        Point lt = getLTPoint(center);
        Paint p = getLinePaint();
        canvas.drawRect(lt.x, lt.y, lt.x + width, lt.y + height, p);
        canvas.drawLine(center.x, lt.y, center.x, lt.y + height, p);
        canvas.drawLine(lt.x, center.y, lt.x + width, center.y, p);
    }

    private Point getLTPoint(Point point) {
        int targetX = point.x - width / 2;
        targetX = targetX < 0 ? 0 : targetX;
        int targetY = point.y - height / 2;
        targetY = targetY < 0 ? 0 : targetY;
        return new Point(targetX, targetY);
    }

    private void initBackground(Canvas canvas) {
        if (backgroundImage == null) {
            backgroundImage = Bitmap.createBitmap(canvas.getWidth() + 2 * bb, canvas.getHeight() + 2 * bb, Bitmap.Config.ARGB_8888);
            Canvas backgroundImageCanvas = new Canvas(backgroundImage);
            int x = (backgroundImageCanvas.getWidth() - sourceImage.getWidth()) / 2;
            x = x < 0 ? 0 : x;
            int y = (backgroundImageCanvas.getHeight() - sourceImage.getHeight()) / 2;
            y = y < 0 ? 0 : y;
            backgroundImageCanvas.drawBitmap(sourceImage, x, y, null);
        }
    }
}
