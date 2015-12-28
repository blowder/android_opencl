package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.shl.checkpin.android.opencv.ImageProcessingService;

import java.io.File;

/**
 * Created by sesshoumaru on 25.12.15.
 */
public class ImagePrepareTask extends AsyncTask<File, Void, Boolean> {
    private final Context context;
    ImageProcessingService processingService = new ImageProcessingService();

    public ImagePrepareTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(File... params) {
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
