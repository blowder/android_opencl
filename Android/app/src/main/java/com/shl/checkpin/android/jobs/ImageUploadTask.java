package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.shl.checkpin.android.dto.UploadConfDTO;
import com.shl.checkpin.android.requests.*;
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
public class ImageUploadTask extends AsyncTask<File, Void, Boolean> {
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

    private void upload(File image) throws IOException {
        FileInputStream fis = new FileInputStream(image);
        byte[] fileBytes = IOUtils.toByteArray(fis);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 600, 600, false);
        FileOutputStream fos = new FileOutputStream(image);
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fis.close();
        fos.close();

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

        try {
            for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
                int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
                byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
                TypedInput typedBytes = new TypedByteArrayWithFilename("multipart/form-data", data, image.getName());
                uploadRestAdapter.create(UploadImageRequest.class).upload(uploadToken, chunkId, chunksTotal, typedBytes);
                chunkId++;
            }
            Log.i(TAG, "File " + image + " was uploaded");
        } catch (RetrofitError e) {
            if (e.getResponse() != null
                    && e.getResponse().getBody() != null
                    && e.getResponse().getBody().in() != null) {
                InputStream is = e.getResponse().getBody().in();
                Log.e(TAG, new String(IOUtils.toByteArray(is)));
                is.close();
            } else {
                Log.e(TAG, "Unexpected error occurred ", e);
            }
        }
    }

    @Override
    protected Boolean doInBackground(File... params) {
        try {
            upload(params[0]);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        CharSequence text = "Image was sent!!!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
