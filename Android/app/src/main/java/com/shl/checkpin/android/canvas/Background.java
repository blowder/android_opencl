package com.shl.checkpin.android.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Created by sesshoumaru on 24.01.16.
 */
public class Background {
    private Point lt;
    private Bitmap image;
    private Bitmap background;

    public Background(Bitmap image) {
        this.image = image;
    }

    public Point getPointOnBackgroundImage(Point point) {
        if (lt == null) {
            return point;
        } else {
            Point result = new Point();
            result.x = point.x - lt.x;
            result.x = result.x < 0 ? 0 : result.x;
            result.y = point.y - lt.y;
            result.y = result.y < 0 ? 0 : result.y;
            return result;
        }
    }

    public Bitmap getImage() {
        return image;
    }

    public Bitmap getBackground() {
        return background;
    }

    public void draw(Canvas canvas) {
        initBackground(canvas, image);
        canvas.drawBitmap(background, 0, 0, null);
    }

    private void initBackground(Canvas canvas, Bitmap image) {
        if (background == null) {
            lt = getCenteredImageCoords(canvas, image);
            background = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas fakeCanvas = new Canvas(background);
            fakeCanvas.drawBitmap(image, lt.x, lt.y, null);
        }
    }

    private Point getCenteredImageCoords(Canvas canvas, Bitmap image) {
        int x = (canvas.getWidth() - image.getWidth()) / 2;
        x = x < 0 ? 0 : x;
        int y = (canvas.getHeight() - image.getHeight()) / 2;
        y = y < 0 ? 0 : y;
        return new Point(x, y);
    }
}
