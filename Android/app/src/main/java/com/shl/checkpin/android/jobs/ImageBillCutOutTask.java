package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import org.opencv.core.Size;

import java.io.File;
import java.util.UUID;


/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageBillCutOutTask extends AsyncTask<Circle, Void, Boolean> {

    private final Context context;
    private final File image;
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private File target = appFileLocator.locate(Environment.DIRECTORY_PICTURES, UUID.randomUUID() + ".png");

    public ImageBillCutOutTask(Context context, File image) {
        this.context = context;
        this.image = image;
    }

    @Override
    protected Boolean doInBackground(Circle... circles) {
        if (circles.length == 4) {
            double width = Math.max(circles[0].distanceTo(circles[1]), circles[2].distanceTo(circles[3]));
            double height = Math.max(circles[0].distanceTo(circles[3]), circles[1].distanceTo(circles[2]));
            new ImageProcessingService().cutOutRectangle(image, target, circles, new Size(width, height));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            CharSequence text = "Image was created " + target;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
