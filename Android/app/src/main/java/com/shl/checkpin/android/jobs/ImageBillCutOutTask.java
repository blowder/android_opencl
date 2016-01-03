package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.shl.checkpin.android.canvas.Circle;

import java.util.Arrays;


/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageBillCutOutTask extends AsyncTask<Circle, Void, Boolean> {

    private final Context context;
    private Circle[] circles = null;

    public ImageBillCutOutTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Circle... params) {
        circles = params;
        return null;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        CharSequence text = Arrays.toString(circles);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
