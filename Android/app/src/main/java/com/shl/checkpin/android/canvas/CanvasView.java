package com.shl.checkpin.android.canvas;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.shl.checkpin.android.factories.Injector;
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask;
import com.shl.checkpin.android.jobs.OnTaskCompletedListener;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FileLocator;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class CanvasView extends View {
    @Inject
    @Named(Constants.LOWRES)
    FileLocator lowResLocator;
    private Context context;
    private File originImage;
    private File thumbnail;
    private List<Circle> circles = new ArrayList<Circle>();
    private Background background;
    //private FSFileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private Bitmap thumbnailBitmap;

    private Preview preview;
    private boolean previewEnabled = false;
    private Point touchCoords;
    private int previewWidth;
    private int prewiewHeight;

    public CanvasView(Context context) {
        super(context);
        this.context = context;
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setImageSource(File originImage) {
        this.originImage = originImage;
        this.thumbnail = lowResLocator.locate(null, originImage.getName());
    }

    public Point getPointOnBackgroundImage(Point point) {
        return background == null ? point : background.getPointOnBackgroundImage(point);
    }

    private Paint getLinePaint() {
        Paint result = new Paint();
        result.setColor(Color.BLACK);
        result.setStrokeWidth(5);
        return result;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawLines(canvas);

        for (Circle circle : circles)
            circle.draw(canvas);

        drawPreview(canvas);
    }

    private void drawPreview(Canvas canvas) {
        if (preview != null && previewEnabled)
            preview.draw(canvas, touchCoords);

        if (preview == null && thumbnailBitmap != null) {
            Point center = new Point(canvas.getWidth() / 2, canvas.getHeight() / 2);
            preview = new Preview(thumbnailBitmap, center, previewWidth, prewiewHeight);
        }
    }

    private void drawLines(Canvas canvas) {
        for (Circle circle : circles)
            if (circle.getNext() != null)
                canvas.drawLine(circle.getX(), circle.getY(), circle.getNext().getX(), circle.getNext().getY(), getLinePaint());
    }

    private void drawBackground(Canvas canvas) {
        if (background == null && thumbnail != null)
            new ImageThumbnailCreateTask(canvas.getWidth(), canvas.getHeight(), thumbnail, context, onBackgroundCreate).execute(originImage);
        else if (background != null)
            background.draw(canvas);
    }

    private OnTaskCompletedListener onBackgroundCreate = new OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted() {
            thumbnailBitmap = BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
            background = new Background(thumbnailBitmap);
            invalidate();
            requestLayout();
        }
    };

    public void enablePreview() {
        this.previewEnabled = true;
    }

    public void disablePreview() {
        this.previewEnabled = false;
    }

    public void setTouchCoords(Point touchCoords) {
        this.touchCoords = touchCoords;
    }

    public void setDimensionsOfPreview(int width, int height) {
        this.previewWidth = width;
        this.prewiewHeight = height;
    }
}
