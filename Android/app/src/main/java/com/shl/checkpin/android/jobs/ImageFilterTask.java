package com.shl.checkpin.android.jobs;

import android.app.Activity;
import android.content.Context;
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
 * Created by sesshoumaru on 25.12.15.
 */
public class ImageFilterTask extends AsyncTask<File, Void, Boolean> {
    private final Activity context;
    ImageProcessingService processingService = new ImageProcessingService();

    public ImageFilterTask(Activity context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(File... params) {
        if (params == null || params.length == 0)
            return false;
        return prepare(params[0]);
    }

    private boolean prepare(File file) {
        if (!file.exists())
            return false;
        processingService.detectAndCorrectSkew(file, file);
        processingService.prepareForSend(file, file);
        return processingService.isBill(file);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            CharSequence text = "This is check!!!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        if (!result) {
            //TODO: delete file if not check
            CharSequence text = "Sorry, we could not detect bill on this image, try photograph again!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
