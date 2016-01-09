package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.shl.checkpin.android.dto.UploadConfDTO;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.requests.*;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.Constants;
import org.apache.commons.io.IOUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by sesshoumaru on 09.12.15.
 */
public class ImageUploadTask extends AsyncTask<File, String, Boolean> {
    private static final String TAG = "ImageUploadTask";
    private final Context context;
    private String phoneNumber;
    private String googleToken;

    private RestAdapter authRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST + ":8080")
            .build();

    private RestAdapter uploadRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST)
            .build();

    public ImageUploadTask(Context context, String phoneNumber, String googleToken) {
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.googleToken = googleToken;
    }

    private boolean upload(File image) throws IOException {

        if (!image.exists())
            return false;
        new ImageProcessingService().resize(image, image, 600, 20000);
        /*
        FileInputStream fis = new FileInputStream(image);
        byte[] fileBytes = IOUtils.toByteArray(fis);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 600, 600, false);
        FileOutputStream fos = new FileOutputStream(image);
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fis.close();
        fos.close();*/
        try {
            //authorization and registration
            UploadTokenRequest uploadTokenRequest = authRestAdapter.create(UploadTokenRequest.class);
            phoneNumber = phoneNumber.replace("+", "");
            String uploadToken = uploadTokenRequest.get(phoneNumber).getToken();

            GcmRegisterRequest gcmRegisterRequest = authRestAdapter.create(GcmRegisterRequest.class);
            int responseCode = gcmRegisterRequest.register(uploadToken, googleToken).getStatus();
            Log.i(TAG, responseCode == 200
                    ? "User with number" + phoneNumber + " was register on upload service"
                    : "Error during registration of " + phoneNumber + " occurred ");

            //get configuration
            UploadConfigurationRequest conf = uploadRestAdapter.create(UploadConfigurationRequest.class);
            UploadConfDTO uploadConf = conf.getConfiguration();

            //image upload
            byte[] fileInBytes = IOUtils.toByteArray(new FileInputStream(image));

            int chunkSize = uploadConf.getChunkSize();
            int chunksTotal = (int) Math.ceil((double) fileInBytes.length / chunkSize);

            int chunkId = 1;

            for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
                int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
                byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
                TypedInput typedBytes = new TypedByteArrayWithFilename("multipart/form-data", data, image.getName());
                uploadRestAdapter.create(UploadImageRequest.class).upload(uploadToken, chunkId, chunksTotal, typedBytes);
                chunkId++;
            }
            Log.i(TAG, "File " + image + " was uploaded");
            return true;
        } catch (Exception e) {
            String message = null;
            if (e instanceof RetrofitError) {
                RetrofitError error = (RetrofitError) e;
                if (error.getResponse() != null
                        && error.getResponse().getBody() != null
                        && error.getResponse().getBody().in() != null) {
                    InputStream is = error.getResponse().getBody().in();
                    message = new String(IOUtils.toByteArray(is));
                    Log.e(TAG, message);
                    is.close();
                }
            } else {
                Log.e(TAG, "Unexpected error occurred ", e);
            }
            CharSequence text = "Internal error during upload!\n " + (message != null ? message : e.getMessage());
            publishProgress(text.toString());
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(String... message) {
        AndroidUtils.toast(context, (message != null && message.length != 0 ? message[0] : ""), Toast.LENGTH_LONG);
    }

    @Override
    protected Boolean doInBackground(File... params) {
        try {
            return upload(params[0]);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result)
            AndroidUtils.toast(context, "Image was sent.");
    }
}
