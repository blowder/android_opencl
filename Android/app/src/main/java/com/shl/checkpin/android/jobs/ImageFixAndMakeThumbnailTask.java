package com.shl.checkpin.android.jobs;

import android.app.Activity;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import com.shl.checkpin.android.utils.FileType;

import java.io.File;
import java.io.IOException;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageFixAndMakeThumbnailTask extends AsyncTask<File, Void, Boolean> {
    private static final String TAG = "ImageFilterTask.class";
    private final Activity context;
    ImageProcessingService processingService = new ImageProcessingService();
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);

    public ImageFixAndMakeThumbnailTask(Activity context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(File... params) {
        if (params == null || params.length == 0)
            return false;
        return rotateAndMakeThumbnail(params[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            CharSequence text = "Thumbnail was generated";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        if (!result) {
            //TODO: delete file if not check
            CharSequence text = "Sorry we could not create thumbnail!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private boolean rotateAndMakeThumbnail(File file) {
        if (!file.exists())
            return false;
        correctRotation(file);
        File thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, file.getName());
        Point dimension = AndroidUtils.getScreenDimension(context);
        new ImageProcessingService().resize(file, thumbnail, dimension.x, dimension.y);
        return true;
    }

    private void correctRotation(File source) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(source.getAbsolutePath());
        } catch (IOException e) {
            Log.i(TAG, "correctRotation(): could not correct rotation for image " + source);
            return;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                new ImageProcessingService().rotate(source, source, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                new ImageProcessingService().rotate(source, source, 180);
                break;
        }
    }

}
