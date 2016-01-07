package com.shl.checkpin.android.jobs;

import android.app.Activity;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import com.shl.checkpin.android.utils.FileType;

import java.io.File;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageThumbnailCreateTask extends AsyncTask<File, Void, Boolean> {
    private final Activity context;
    private final OnTaskCompletedListener listener;
    private FileLocator fileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private ImageProcessingService processingService = new ImageProcessingService();


    public ImageThumbnailCreateTask(Activity context, OnTaskCompletedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(File... params) {
        if (params == null || params.length == 0)
            return false;
        return makeThumbnail(params[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
            listener.onTaskCompleted();
        }else
            AndroidUtils.toast(context, "Sorry we could not create thumbnail!");
    }

    private boolean makeThumbnail(File file) {
        if (!file.exists())
            return false;
        File thumbnail = fileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, file.getName());
        double angle = processingService.getExifRotationAngle(file);
        Point dimension = getDimension(angle);
        processingService.resize(file, thumbnail, dimension.x, dimension.y);
        processingService.rotate(thumbnail,thumbnail,angle);
        return true;
    }

    private Point getDimension(double angle) {
        Point dimension = AndroidUtils.getScreenDimension(context);
        dimension = angle==90?new Point(dimension.y, dimension.x):dimension;
        return dimension;
    }
}
