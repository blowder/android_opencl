package com.shl.checkpin.android.canvas;

import android.content.Context;
import android.graphics.*;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask;
import com.shl.checkpin.android.jobs.OnTaskCompletedListener;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import com.shl.checkpin.android.utils.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class CanvasView extends View {
    private Context context;
    private File originImage;
    private File thumbnail;
    private Bitmap backgroundImage;
    private Bitmap background;
    private List<Circle> circles = new ArrayList<Circle>();
    private Paint linePaint = new Paint();
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    //preview

    private Bitmap preview;
    private boolean previewEnabled = false;
    private float previewX;
    private float previewY;
    private int previewWidth;
    private int previewHeight;


    public CanvasView(Context context) {
        super(context);
        this.context = context;
        initLinePaint();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initLinePaint();
    }

    public void setImageSource(File originImage) {
        this.originImage = originImage;
        this.thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, originImage.getName());
    }

    public void setDimensionsOfPreview(int width, int height) {
        this.previewWidth = width;
        this.previewHeight = height;
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
    }

    private void initLinePaint() {
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }


    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
        invalidate();
        requestLayout();
    }

    public void setPreviewX(float previewX) {
        this.previewX = previewX;
    }

    public void setPreviewY(float previewY) {
        this.previewY = previewY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundImage == null)
            new ImageThumbnailCreateTask(canvas.getWidth(), canvas.getHeight(), thumbnail, context, onThumbnailCreate).execute(originImage);

        if (backgroundImage != null && background == null) {
            background = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas backgroundCanvas = new Canvas(background);
            int x = (backgroundCanvas.getWidth() - backgroundImage.getWidth()) / 2;
            x = x < 0 ? 0 : x;
            int y = (backgroundCanvas.getHeight() - backgroundImage.getHeight()) / 2;
            y = y < 0 ? 0 : y;
            backgroundCanvas.drawBitmap(backgroundImage, x, y, null);
        }

        if (background != null)
            canvas.drawBitmap(background, 0, 0, null);

        for (Circle circle : circles)
            if (circle.getNext() != null)
                canvas.drawLine(circle.getX(), circle.getY(), circle.getNext().getX(), circle.getNext().getY(), linePaint);

        for (Circle circle : circles)
            circle.draw(canvas);

        if (previewEnabled && background != null) {

            int targetX = (int) (previewX - previewWidth / 2);
            targetX = targetX < 0 ? 0 : targetX;
            int targetY = (int) (previewY - previewHeight / 2);
            targetY = targetY < 0 ? 0 : targetY;

            int targetWidth = targetX + previewWidth > background.getWidth() ? background.getWidth() - targetX : previewWidth;
            int targetHeight = targetY + previewHeight > background.getHeight() ? background.getHeight() - targetY : previewHeight;

            preview = Bitmap.createBitmap(background, targetX, targetY, targetWidth, targetHeight);
            int x = canvas.getWidth() / 2 - previewWidth / 2;
            int y = canvas.getHeight() / 2 - previewHeight / 2;
            canvas.drawBitmap(preview, x, y, null);

            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            canvas.drawRect(x, y, x + preview.getWidth(), y + preview.getHeight(), p);
            canvas.drawLine(x + preview.getWidth() / 2, y, x + preview.getWidth() / 2, y + preview.getHeight(), p);
            canvas.drawLine(x, y + preview.getHeight() / 2, x + preview.getWidth(), y + preview.getHeight() / 2, p);
        }
    }

    private OnTaskCompletedListener onThumbnailCreate = new OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted() {
            backgroundImage = BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
            invalidate();
            requestLayout();
        }
    };
}
