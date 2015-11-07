package billcollector.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import billcollector.app.R;
import billcollector.app.listeners.CameraCaptureListener;
import billcollector.app.utils.CameraUtils;

/**
 * Created by sesshoumaru on 19.09.15.
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static String TAG = "CameraActivity";
    private CameraUtils cameraUtils;
    private SurfaceView cameraFrame;

    private View.OnClickListener captureButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraUtils.takeShot(null, null, new CameraCaptureListener());

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        addListenersForButtons();

    }

    private void addListenersForButtons() {
        Button captureButton = (Button) findViewById(R.id.camera_activity_capture_button);
        captureButton.setOnClickListener(captureButtonListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraFrame = (SurfaceView) findViewById(R.id.camera_preview);
        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraUtils != null && cameraUtils.isOn()) {
            cameraUtils.stop();
        }
        SurfaceHolder surfaceHolder = cameraFrame.getHolder();
        surfaceHolder.removeCallback(this);
        Log.d(TAG, "Camera engine destroyed");

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //init singleton with camera controls
        Log.d(TAG, "Surface Created - starting camera");
        if (cameraUtils != null && !cameraUtils.isOn()) {
            cameraUtils.start();
        }

        if (cameraUtils != null && cameraUtils.isOn()) {
            Log.d(TAG, "Camera engine already on");
            return;
        }
        cameraUtils = CameraUtils.New(holder);
        cameraUtils.start();
        Log.d(TAG, "Camera engine started");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
