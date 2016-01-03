package com.shl.checkpin.android.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by sesshoumaru on 02.01.16.
 */
public class Circle {
    float x;
    float y;
    float radius;
    Paint paint;
    Paint linePaint;
    Circle next;

    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        this.linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
    }

    public Circle(float x, float y, float radius, Paint paint) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = paint;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Circle getNext() {
        return next;
    }

    public void setNext(Circle next) {
        this.next = next;
    }

    public void draw(Canvas canvas) {
        if (next != null)
            canvas.drawLine(x, y, next.getX(), next.getY(), linePaint);
        canvas.drawCircle(x, y, radius, paint);
    }
}
