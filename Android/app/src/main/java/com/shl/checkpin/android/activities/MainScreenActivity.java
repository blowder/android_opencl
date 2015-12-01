package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.gcm.MyInstanceIDListenerService;
import com.shl.checkpin.android.services.JobHolder;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.UUID;

/**
 * Created by sesshoumaru on 19.09.15.
 */
public class MainScreenActivity extends Activity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    public static String TAG = "MainScreenActivity";
    private File picture = null;
    //private FileLocator externalFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);

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
            picture = appFileLocator.locate(Environment.DIRECTORY_PICTURES, UUID.randomUUID().toString() + ".jpg");
            System.out.println(picture);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO add manipulation when we already have image file
        System.out.println("picture is " + picture);
        if (picture != null) {
            new JobHolder(this).processImage(picture);
        }
    }

    private String getPhoneIMEI() {
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getDeviceId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        startService(new Intent(this, MyInstanceIDListenerService.class));
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
