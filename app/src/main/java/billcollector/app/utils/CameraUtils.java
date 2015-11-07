package billcollector.app.utils;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by sesshoumaru on 06.09.15.
 */
public class CameraUtils {
    private static String TAG = "CameraUtils";
    private boolean on;
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    };

    public boolean isOn() {
        return on;
    }

    private CameraUtils(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    static public CameraUtils New(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Creating camera engine");
        return new CameraUtils(surfaceHolder);
    }

    public void requestFocus() {
        if (camera == null)
            return;

        if (isOn()) {
            camera.autoFocus(autoFocusCallback);
        }
    }

    public void start() {
        this.camera = Camera.open();
        if (this.camera == null)
            return;

        try {
            this.camera.setPreviewDisplay(this.surfaceHolder);
            this.camera.setDisplayOrientation(90);//Portrait Camera
            this.camera.startPreview();
            on = true;
        } catch (IOException e) {
            Log.e(TAG, "Error in setPreviewDisplay");
        }
    }

    public void stop() {

        if (camera != null) {
            //this.autoFocusEngine.stop();
            camera.release();
            camera = null;
        }

        on = false;
        Log.d(TAG, "Camera Stopped");
    }

    public void takeShot(final Camera.ShutterCallback shutterCallback,
                         final Camera.PictureCallback rawPictureCallback,
                         final Camera.PictureCallback jpegPictureCallback) {
        if (isOn()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    camera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
                    try {
                        Thread.sleep(2000); // 2 second preview
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    // This returns the preview back to the live camera feed
                    try {
                        if (camera != null) {
                            camera.setPreviewDisplay(surfaceHolder);
                            camera.setDisplayOrientation(90);//Portrait Camera
                            camera.startPreview();
                            on = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }.execute();

        }

    }

}
