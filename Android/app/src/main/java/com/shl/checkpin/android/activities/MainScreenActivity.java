package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.gcm.MyInstanceIDListenerService;
import com.shl.checkpin.android.gcm.RegistrationIntentService;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.utils.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.Menu;
import android.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Date;

/*
 * Created by sesshoumaru on 19.09.15.
 */
public class MainScreenActivity extends AbstractActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final int CANVAS_IMAGE_ACTIVITY_REQUEST_CODE = 300;
    private ImageDoc imageDoc = null;

    @Inject
    @Named(Constants.HIGHRES)
    FileLocator highResLocator;

    private View.OnClickListener aboutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = "CheckPin v" + AndroidUtils.getVersion(MainScreenActivity.this);
            AndroidUtils.toast(getApplicationContext(), message);
        }
    };

    private View.OnClickListener historyButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainScreenActivity.this, HistoryActivity.class);
            MainScreenActivity.this.startActivity(intent);
        }
    };

    private View.OnClickListener nativeCameraIntend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageDoc = new ImageDoc(new Date());
            File pictureFile = highResLocator.locate(null, imageDoc.getName());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
                && imageDoc != null
                && resultCode == Activity.RESULT_OK) {
            Intent selectBillIntent = new Intent(MainScreenActivity.this, SelectBillAreaActivity.class);
            selectBillIntent.putExtra(BundleParams.IMAGE_SOURCE, imageDoc.getName());
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
                offlineMode = !offlineMode;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.OFFLINE_MODE, offlineMode);
                editor.commit();
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
