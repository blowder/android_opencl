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
import billcollector.app.jobs.ImagePrepareJob;
import billcollector.app.utils.Constants;
import billcollector.app.utils.FSFileLocator;
import billcollector.app.utils.FileLocator;
import billcollector.app.utils.OpenCvUtils;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;
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
    private JobManager jobManager;

    private View.OnClickListener aboutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();
            CharSequence text = "About button was pressed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

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
        //TODO add manipulation when we already have image file
        System.out.println("picture is " + picture);
        if (picture != null)
            jobManager.addJob(new ImagePrepareJob(picture));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        addListenersForButtons();
        configureJobManager();
    }

    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(1)//up to 3 consumers at a time
                .loadFactor(1)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);
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
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV was loaded");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


}
