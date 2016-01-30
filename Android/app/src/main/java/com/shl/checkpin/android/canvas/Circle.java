package com.shl.checkpin.android.canvas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by sesshoumaru on 02.01.16.
 */
public class Circle {
    Point center = new Point();
    float radius;
    Paint paint;
    Circle next;

    public Circle(int x, int y, float radius) {
        this.center.x = x;
        this.center.y = y;
        this.radius = radius;
        this.paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public float getX() {
        return center.x;
    }

    public void setX(int x) {
        this.center.x = x;
    }

    public float getY() {
        return center.y;
    }

    public void setY(int y) {
        this.center.y = y;
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
        canvas.drawCircle(center.x, center.y, radius, paint);
    }

    public double distanceTo(Circle circle) {
        double x = center.x - circle.getX();
        double y = center.y - circle.getY();
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
        return "Circle{" +
                "x=" + center.x +
                ", y=" + center.y +
                ", radius=" + radius +
                '}';
    }
}
