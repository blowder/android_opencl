package com.shl.checkpin.android.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by sesshoumaru on 02.01.16.
 */
public class Circle {
    float x;
    float y;
    float radius;
    Paint paint;
    Circle next;

    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
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
        canvas.drawCircle(x, y, radius, paint);
    }

    public double distanceTo(Circle circle) {
        double x = this.x - circle.getX();
        double y = this.y - circle.getY();
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
        return "Circle{" +
                "x=" + x +
                ", y=" + y +
                ", radius=" + radius +
                '}';
    }
}
