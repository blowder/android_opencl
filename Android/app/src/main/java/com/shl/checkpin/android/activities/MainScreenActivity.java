package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle; 
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.gcm.MyInstanceIDListenerService;
import com.shl.checkpin.android.gcm.RegistrationIntentService;
import com.shl.checkpin.android.utils.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.Menu;
import android.preference.PreferenceManager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*  
 * Created by sesshoumaru on 19.09.15. 
 */
public class MainScreenActivity extends Activity {
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final int CANVAS_IMAGE_ACTIVITY_REQUEST_CODE = 300;
    public static String TAG = "MainScreenActivity";
    private File picture = null;
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    
    private View.OnClickListener aboutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.toast(getApplicationContext(), "About button was pressed!");
        }
    };

    private View.OnClickListener historyButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainScreenActivity.this, NewHistoryActivity.class);
            MainScreenActivity.this.startActivity(intent);
        }
    };

    private View.OnClickListener nativeCameraIntend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            picture = appFileLocator.locate(Environment.DIRECTORY_PICTURES, dateFormat.format(new Date()) + ".png");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && picture != null && resultCode == Activity.RESULT_OK) {
            Intent selectBillIntent = new Intent(MainScreenActivity.this, SelectBillAreaActivity.class);
            selectBillIntent.putExtra(BundleParams.IMAGE_SOURCE, picture.getName());
            startActivityForResult(selectBillIntent, CANVAS_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String message = intent.getStringExtra(Constants.GCM_MESSAGE);
            if (message != null) 
                AndroidUtils.dialog(MainScreenActivity.this, "GCM message", message, null);
        }
        setContentView(R.layout.main_screen);
        startService(new Intent(this, MyInstanceIDListenerService.class));
        startService(new Intent(this, RegistrationIntentService.class));
        addListenersForButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false);
        MenuItem item = menu.findItem(R.id.offline_mode);
        item.setChecked(offlineMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.offline_mode:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false);
                System.out.println("Checkbox 1: "+offlineMode);
                offlineMode = !offlineMode;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.OFFLINE_MODE, offlineMode);
                editor.commit();  
                System.out.println("Checkbox 2: "+offlineMode);
                item.setChecked(offlineMode);
                return true;
            case R.id.menu_settings:
            
            default:
            return super.onOptionsItemSelected(item);
        }
    }


    private void addListenersForButtons() {
        Button cameraButton = (Button) findViewById(R.id.main_screen_camera_button);
        Button historyButton = (Button) findViewById(R.id.historyButton);
        Button aboutButton = (Button) findViewById(R.id.main_screen_about_button);
        cameraButton.setOnClickListener(nativeCameraIntend);
        historyButton.setOnClickListener(historyButtonListener);
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
