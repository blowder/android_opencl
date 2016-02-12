package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.utils.AndroidUtils;

import java.io.File;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageThumbnailCreateTask extends AsyncTask<File, Void, Boolean> {
    private final Context context;
    private final OnTaskCompletedListener listener;
    private final int height;
    private final int width;
    private final File result;
    //private FileLocator fileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private ImageProcessingService processingService = new ImageProcessingService();


    public ImageThumbnailCreateTask(int width, int height, File result, Context context, OnTaskCompletedListener listener) {
        this.width = width;
        this.height = height;
        this.context = context;
        this.listener = listener;
        this.result = result;
    }

    @Override
    protected Boolean doInBackground(File... params) {
        if (params == null || params.length == 0)
            return false;
        return makeThumbnail(params[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (listener != null)
                listener.onTaskCompleted();
        } else
            AndroidUtils.toast(context, "Sorry we could not create thumbnail!");
    }

    private boolean makeThumbnail(File file) {
        if (!file.exists())
            return false;

        double angle = processingService.getExifRotationAngle(file);
        Point dimension = getDimension(angle);
        processingService.resize(file, result, dimension.x, dimension.y);
        processingService.rotate(result, result, angle);
        return true;
    }

    private Point getDimension(double angle) {
        Point dimension = new Point(width, height);
        dimension = angle == 90 ? new Point(dimension.y, dimension.x) : dimension;
        return dimension;
    }
}
