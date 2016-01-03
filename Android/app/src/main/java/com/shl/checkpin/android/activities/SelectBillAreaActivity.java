package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.opencv.OpenCvUtils;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import org.opencv.core.Size;

/**
 * Created by sesshoumaru on 29.12.15.
 */
public class SelectBillAreaActivity extends Activity implements View.OnTouchListener {
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    String fileName = "bill.jpg";
    String COLOR = "SelectBillAreaActivity.color";
    private DrawView drawView = null;
    private int color = Color.GREEN;
    private int canvasWidth;
    private int canvasHeight;
    Bitmap image;


    private int threshold = 50;
    private int circleRadius = 30;

    Circle circle = new Circle(200, 200, circleRadius);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(COLOR);
        }
        image = BitmapFactory.decodeFile(appFileLocator.locate(Environment.DIRECTORY_PICTURES, fileName).getAbsolutePath());
        drawView = new DrawView(this);
        drawView.setOnTouchListener(this);
        setContentView(drawView);

        Point size = AndroidUtils.getScreenDimension(this);

        canvasWidth = size.x;
        canvasHeight = size.y;
        Size dimension = OpenCvUtils.getScaledDimension(new Size(image.getWidth(), image.getHeight()), new Size(canvasWidth, canvasHeight));
        image = Bitmap.createScaledBitmap(image, (int) dimension.width, (int) dimension.height, true);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(COLOR, color);


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            if (x < circle.getX() + threshold
                    && x > circle.getX() - threshold
                    && y < circle.getY() + threshold
                    && y > circle.getY() - threshold) {
                circle.setX(x);
                circle.setY(y);
                drawView.invalidate();
            }
        }
        return true;
    }

    class DrawView extends View {


        public DrawView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(image, 0, 0, null);
            //image.recycle();
            //image=null;
            circle.draw(canvas);

        }
    }

}