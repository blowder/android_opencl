package com.shl.checkpin.android.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by sesshoumaru on 24.01.16.
 */
public class Background {
    private int y;
    private int x;
    private Bitmap image;

    public Background(Bitmap image) {
        this.image = image;
    }

    public Point getPointOnBackgroundImage(Point point) {
        Point result = new Point();
        result.x = point.x - this.x;
        result.x = result.x < 0 ? 0 : result.x;
        result.y = point.y - this.y;
        result.y = result.y < 0 ? 0 : result.y;
        return result;
    }

    public Bitmap getImage() {
        return image;
    }

    public void draw(Canvas canvas) {
        initCoords(canvas);
        canvas.drawBitmap(image, x, y, null);
    }

    private void initCoords(Canvas canvas) {
        int x = (canvas.getWidth() - image.getWidth()) / 2;
        this.x = x < 0 ? 0 : x;
        int y = (canvas.getHeight() - image.getHeight()) / 2;
        this.y = y < 0 ? 0 : y;
    }
}
