package billcollector.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import billcollector.app.R;
import billcollector.app.utils.Constants;
import billcollector.app.utils.FSFileLocator;
import billcollector.app.utils.FileLocator;
import billcollector.app.utils.OpenCvUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;

/**
 * Created by sesshoumaru on 19.09.15.
 */
public class MainScreenActivity extends Activity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    public static String TAG = "MainScreenActivity";
    private File picture = null;
    private FileLocator fileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);

    private View.OnClickListener customCameraIntend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), CameraActivity.class);
            startActivity(i);
        }
    };

    private View.OnClickListener nativeCameraIntend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            picture = fileLocator.locate(Environment.DIRECTORY_PICTURES, "test.png");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("picture is " + picture);
    }

    private View.OnClickListener aboutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();
            CharSequence text = "About button was pressed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            //TODO delete this
            onlyForOpenCVTest();
        }
    };

    private void onlyForOpenCVTest() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File imageFolder = new File(Constants.APP_IMAGE_FOLDER);
                File[] files = imageFolder.listFiles() != null ? imageFolder.listFiles() : new File[]{};
                for (File file : files) {
                    OpenCvUtils.adaptiveThreshold(file, file);
                }
                return null;
            }
        }.execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        addListenersForButtons();
    }

    private void addListenersForButtons() {
        Button cameraButton = (Button) findViewById(R.id.main_screen_camera_button);
        Button aboutButton = (Button) findViewById(R.id.main_screen_about_button);
        cameraButton.setOnClickListener(nativeCameraIntend);
        aboutButton.setOnClickListener(aboutButtonListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV was loaded");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


}
