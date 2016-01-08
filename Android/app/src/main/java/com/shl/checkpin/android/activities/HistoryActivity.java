package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.jobs.ImageUploadTask;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FSFileLocator;
import com.shl.checkpin.android.utils.FileLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 06.01.16.
 */
public class HistoryActivity extends Activity implements View.OnClickListener {
    private static final String pattern = "^[0-9]{8}-[0-9]{6}\\.png$";
    private TableLayout historyTable;
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_layout);
        this.historyTable = (TableLayout) findViewById(R.id.history_table);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        inflate();
    }

    private void inflate() {
        List<File> files = new ArrayList<File>();
        for (File file : appFileLocator.locate(Environment.DIRECTORY_PICTURES))
            if (file.getName().matches(pattern))
                files.add(file);

        for (File image : files) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView imageName = new TextView(this);
            imageName.setText(image.getName());
            Button resendButton = new Button(this);
            resendButton.setWidth(200);
            resendButton.setText("Resend");
            resendButton.setTag(image);
            resendButton.setOnClickListener(this);
            row.addView(imageName);
            row.addView(resendButton);
            historyTable.addView(row);
            //historyTable.setColumnShrinkable(index, true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof File) {
            File image = (File) v.getTag();
            if (image != null && AndroidUtils.isInetConnected(this)
                    && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)) {
                String gcmToken = sharedPreferences.getString(Constants.GCM_TOKEN, "");
                new ImageUploadTask(this, AndroidUtils.getPhoneNumber(this), gcmToken)
                        .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, image);
            } else {
                AndroidUtils.toast(this, "Sorry there is no Internet connection or you try to send unexisted file", Toast.LENGTH_LONG);
            }
        }
    }
}
