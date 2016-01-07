package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.graphics.*;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.canvas.CanvasView;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask;
import com.shl.checkpin.android.jobs.OnTaskCompletedListener;
import com.shl.checkpin.android.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 29.12.15.
 */
public class SelectBillAreaActivity extends Activity implements View.OnTouchListener {
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private File originImage;
    private File thumbnail;

    private CanvasView drawView = null;
    private List<Circle> circles = new ArrayList<Circle>();

    private void initCircles() {
        int circleRadius = 30;
        Point dimension = AndroidUtils.getScreenDimension(this);
        int aThirdOfWidth = dimension.x / 3;
        int aThirdOfHeight = dimension.y / 3;

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

    private OnTaskCompletedListener onThumbnailCreate = new OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted() {
            drawView.setBackgroundImage(BitmapFactory.decodeFile(thumbnail.getAbsolutePath()));
            drawView.invalidate();
        }
    };

    private View.OnClickListener onFinishButtonPress = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           /* new ImageBillCutOutTask(getApplicationContext(), thumbnail)
                    .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, circles.toArray(new Circle[circles.size()]));*/
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_screen);

        originImage = appFileLocator.locate(Environment.DIRECTORY_PICTURES,getIntent().getStringExtra(BundleParams.IMAGE_SOURCE));
        thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, originImage.getName());

        new ImageThumbnailCreateTask(this,onThumbnailCreate).execute(originImage);

        initCircles();

        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(onFinishButtonPress);

        drawView = (CanvasView) findViewById(R.id.canvasView);
        drawView.setCircles(circles);
        drawView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            List<Circle> touchedCircles = new ArrayList<Circle>();
            int threshold = 50;
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
