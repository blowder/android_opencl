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
    private List<Circle> circles = new ArrayList<Circle>();
    private Paint linePaint = new Paint();
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);


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
    public void addImageSource(File originImage){
        this.originImage = originImage;
        this.thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, originImage.getName());
    }

    private void initLinePaint() {
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(5);
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;
        invalidate();
        requestLayout();
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundImage != null) {
            int x = (canvas.getWidth() - backgroundImage.getWidth()) / 2;
            x = x < 0 ? 0 : x;
            int y = (canvas.getHeight() - backgroundImage.getHeight()) / 2;
            y = y < 0 ? 0 : y;
            canvas.drawBitmap(backgroundImage, x, y, null);
        } else {
            if(originImage!=null)
                new ImageThumbnailCreateTask(canvas.getWidth(), canvas.getHeight(), context, onThumbnailCreate).execute(originImage);
        }
        //background.recycle();
        //background=null;
        for (Circle circle : circles)
            if (circle.getNext() != null)
                canvas.drawLine(circle.getX(), circle.getY(), circle.getNext().getX(), circle.getNext().getY(), linePaint);
        for (Circle circle : circles)
            circle.draw(canvas);
    }

    private OnTaskCompletedListener onThumbnailCreate = new OnTaskCompletedListener() {
        @Override
        public void onTaskCompleted() {
            setBackgroundImage(BitmapFactory.decodeFile(thumbnail.getAbsolutePath()));
            invalidate();
        }
    };
}
