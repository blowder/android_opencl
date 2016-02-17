package com.shl.checkpin.android.activities;

import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.canvas.CanvasView;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.factories.Injector;
import com.shl.checkpin.android.jobs.ImageBillCutOutTask;
import com.shl.checkpin.android.jobs.ImageUploadTask;
import com.shl.checkpin.android.jobs.OnTaskCompletedListener;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.utils.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 29.12.15.
 */
public class SelectBillAreaActivity extends AbstractActivity implements View.OnTouchListener {
    @Inject
    @Named(Constants.HIGHRES)
    FileLocator highResLocator;
    @Inject
    @Named(Constants.LOWRES)
    FileLocator lowResLocator;
    @Inject
    ImageDocService imageDocService;

    private File originImage;
    private File thumbnail;

    private CanvasView drawView = null;
    private List<Circle> circles = new ArrayList<Circle>();
    private ImageDoc imageDoc;



    private void initCircles() {
        int circleRadius = AndroidUtils.mmInPixels(this, 3);
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


    private OnTaskCompletedListener onImageReadyForUpload = new OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted() {
            boolean offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false);
            if (offlineMode) {
                imageDoc.setStatus(ImageDoc.Status.OFFLINE);
                imageDocService.update(imageDoc);
            } else if (originImage != null && AndroidUtils.isInetConnected(SelectBillAreaActivity.this)
                    && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)) {
                //String gcmToken = sharedPreferences.getString(Constants.GCM_TOKEN, "");
                //String userId = AndroidUtils.getPhoneNumber(SelectBillAreaActivity.this);
                new ImageUploadTask().execute(imageDoc);
                //new ImageUploadTask(SelectBillAreaActivity.this, userId, gcmToken).executeFor(originImage);
            } else {
                AndroidUtils.toast(SelectBillAreaActivity.this, "Image was not sent, you can send it from history page manually", Toast.LENGTH_LONG);
            }
        }
    };

    private View.OnClickListener onFinishButtonPress = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.toast(SelectBillAreaActivity.this, "Bill will be uploaded soon!");
            for (Circle circle : circles)
                circle.setCenter(drawView.getPointOnBackgroundImage(circle.getCenter()));
            new ImageBillCutOutTask(getApplicationContext(), originImage, thumbnail, onImageReadyForUpload).execute(circles.toArray(new Circle[circles.size()]));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_screen);
        //   EventBus.getDefault().register(this);
        //imageDocService = new ImageDocFileService(imageDocFileLocator);

        String imageName = getIntent().getStringExtra(BundleParams.IMAGE_SOURCE);
        imageDoc = imageDocService.findByName(imageName);
        originImage = highResLocator.locate(null, imageName);
        thumbnail = lowResLocator.locate(null, imageName);

        initCircles();

        Button finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(onFinishButtonPress);

        drawView = (CanvasView) findViewById(R.id.canvasView);
        Injector.inject(drawView);
        drawView.setImageSource(originImage);
        drawView.setDimensionsOfPreview(AndroidUtils.mmInPixels(this, 20), AndroidUtils.mmInPixels(this, 20));
        drawView.setCircles(circles);
        drawView.setOnTouchListener(this);
    }

    @Override
    public void onStop() {
        //  EventBus.getDefault().unregister(this);
        super.onStop();
    }

/*    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onImageTransferEvent(ImageDoc imageDoc) {
        this.imageDoc = imageDoc;
        originImage = highResLocator.locate(null, imageDoc.getName());
        thumbnail = lowResLocator.locate(null, imageDoc.getName());
        EventBus.getDefault().removeStickyEvent(imageDoc);
    }*/

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            List<Circle> touchedCircles = new ArrayList<Circle>();
            int threshold = AndroidUtils.mmInPixels(this, 5);
            for (Circle circle : circles)
                if (x < circle.getX() + threshold
                        && x > circle.getX() - threshold
                        && y < circle.getY() + threshold
                        && y > circle.getY() - threshold)
                    touchedCircles.add(circle);

            if (touchedCircles.size() != 0) {
                drawView.enablePreview();
                drawView.setTouchCoords(new Point((int) x, (int) y));
                touchedCircles.get(0).setX((int) x);
                touchedCircles.get(0).setY((int) y);
            }
            drawView.invalidate();
        }

        if (MotionEvent.ACTION_UP == event.getAction()) {
            drawView.disablePreview();
            drawView.invalidate();
        }
        return true;
    }
}
