package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.canvas.CanvasView;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.jobs.ImageBillCutOutTask;
import com.shl.checkpin.android.opencv.OpenCvUtils;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import com.shl.checkpin.android.utils.FileType;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 29.12.15.
 */
public class SelectBillAreaActivity extends Activity implements View.OnTouchListener {
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    String fileName = "bill.jpg";
    private CanvasView drawView = null;
    private int canvasWidth;
    private int canvasHeight;
    private Button finishButton;
    Bitmap image;
    File thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, fileName);

    private int threshold = 50;
    private int circleRadius = 30;
    List<Circle> circles = new ArrayList<Circle>();

    private View.OnClickListener finishButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new ImageBillCutOutTask(getApplicationContext(), thumbnail)
                    .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, circles.toArray(new Circle[circles.size()]));
            finish();
        }
    };

    private void initCircles() {
        initScreenDimension();
        int aThirdOfWidth = canvasWidth / 3;
        int aThirdOfHeight = canvasHeight / 3;
        Circle tl = new Circle(aThirdOfWidth, aThirdOfHeight, circleRadius);
        Circle tr = new Circle(aThirdOfWidth * 2, aThirdOfHeight, circleRadius);
        Circle bl = new Circle(aThirdOfWidth, aThirdOfHeight * 2, circleRadius);
        Circle br = new Circle(aThirdOfWidth * 2, aThirdOfHeight * 2, circleRadius);

        tl.setNext(tr);
        tr.setNext(br);
        br.setNext(bl);
        bl.setNext(tl);

        circles.add(tl);
        circles.add(tr);
        circles.add(br);
        circles.add(bl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_screen);

        initCircles();
        initBackground();

        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(finishButtonListener);

        drawView = (CanvasView) findViewById(R.id.canvasView);
        drawView.setBackgroundImage(image);
        drawView.setCircles(circles);
        drawView.setOnTouchListener(this);
    }

    private void initBackground() {
        image = BitmapFactory.decodeFile(appFileLocator.locate(Environment.DIRECTORY_PICTURES, fileName).getAbsolutePath());
        Size dimension = OpenCvUtils.getScaledDimension(new Size(image.getWidth(), image.getHeight()), new Size(canvasWidth, canvasHeight));
        image = Bitmap.createScaledBitmap(image, (int) dimension.width, (int) dimension.height, true);
        try {
            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(thumbnail));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initScreenDimension() {
        Point size = AndroidUtils.getScreenDimension(this);
        canvasWidth = size.x;
        canvasHeight = size.y;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            List<Circle> touchedCircles = new ArrayList<Circle>();
            for (Circle circle : circles)
                if (x < circle.getX() + threshold
                        && x > circle.getX() - threshold
                        && y < circle.getY() + threshold
                        && y > circle.getY() - threshold)
                    touchedCircles.add(circle);

            if (touchedCircles.size() != 0) {
                touchedCircles.get(0).setX(x);
                touchedCircles.get(0).setY(y);
            }
            drawView.invalidate();
        }
        return true;
    }

}
