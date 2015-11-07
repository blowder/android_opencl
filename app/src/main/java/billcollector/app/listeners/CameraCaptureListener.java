package billcollector.app.listeners;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import billcollector.app.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * Created by sesshoumaru on 19.09.15.
 */
public class CameraCaptureListener implements Camera.PictureCallback {
    private static String TAG = "CameraCaptureListener";

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File imageFolder = new File(Constants.APP_IMAGE_FOLDER);
        if (!imageFolder.exists())
            if (!imageFolder.mkdirs()) {
                Log.i(TAG, imageFolder + " not exists or not a directory");
                return;
            }

        File result = new File(imageFolder, UUID.randomUUID().toString() + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(result);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, "Error occurred during image save: " + e.getMessage());
        }

    }
}
