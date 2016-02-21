package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.shl.checkpin.android.dto.UploadConfDTO;
import com.shl.checkpin.android.factories.Injector;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import com.shl.checkpin.android.requests.*;
import com.shl.checkpin.android.services.OnUploadEvent;
import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FileLocator;
import org.apache.commons.io.IOUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.Arrays;

/**
 * Created by sesshoumaru on 09.12.15.
 */
public class ImageUploadTask extends AsyncTask<ImageDoc, String, Boolean> {
    private static final String TAG = "ImageUploadTask";
    @Inject
    Context context;
    @Inject
    SharedPreferences preferences;

    @Inject
    @Named(Constants.HIGHRES)
    FileLocator fileLocator;

    @Inject
    ImageDocService imageDocService;

    private ImageDoc imageDoc;

    private String phoneNumber;

    private String googleToken;

    private OnUploadEvent successCallback;
    private OnUploadEvent failCallback;
    private File source;

    private RestAdapter authRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST + ":8080")
            .build();
    private RestAdapter uploadRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST)
            .build();

    public ImageUploadTask() {
        Injector.inject(this);
        this.phoneNumber = AndroidUtils.getPhoneNumber(context);
        this.googleToken = preferences.getString(Constants.GCM_TOKEN, "");
    }

    public ImageUploadTask(OnUploadEvent successCallback, OnUploadEvent failCallback) {
        Injector.inject(this);
        this.phoneNumber = AndroidUtils.getPhoneNumber(context);
        this.googleToken = preferences.getString(Constants.GCM_TOKEN, "");
        this.successCallback = successCallback;
        this.failCallback = failCallback;
    }

    @Override
    protected Boolean doInBackground(ImageDoc... params) {
        this.imageDoc = params[0];
        source = fileLocator.locate(null, params[0].getName());
        try {
            return upload(source);
        } catch (IOException e) {
            return false;
        }
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
            String version = "CheckPin Mobile Android v" + AndroidUtils.getVersion(context);

            UploadTokenRequest uploadTokenRequest = authRestAdapter.create(UploadTokenRequest.class);
            phoneNumber = phoneNumber.replace("+", "");
            String uploadToken = uploadTokenRequest.get(version, phoneNumber).getToken();

            GcmRegisterRequest gcmRegisterRequest = authRestAdapter.create(GcmRegisterRequest.class);
            int responseCode = gcmRegisterRequest.register(version, uploadToken, googleToken).getStatus();
            Log.i(TAG, responseCode == 200
                    ? "User with number" + phoneNumber + " was register on upload service"
                    : "Error during registration of " + phoneNumber + " occurred ");

            //get configuration
            UploadConfigurationRequest conf = uploadRestAdapter.create(UploadConfigurationRequest.class);
            UploadConfDTO uploadConf = conf.getConfiguration(version);

            //image upload
            byte[] fileInBytes = IOUtils.toByteArray(new FileInputStream(image));

            int chunkSize = uploadConf.getChunkSize();
            int chunksTotal = (int) Math.ceil((double) fileInBytes.length / chunkSize);

            int chunkId = 1;

            for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
                int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
                byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
                TypedInput typedBytes = new TypedByteArrayWithFilename("multipart/form-data", data, image.getName());
                uploadRestAdapter.create(UploadImageRequest.class).upload(version, uploadToken, chunkId, chunksTotal, typedBytes);
                chunkId++;
            }
            imageDoc.setStatus(ImageDoc.Status.SEND);
            imageDocService.update(imageDoc);
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
        //AndroidUtils.dialog(context, "Upload error!", (message != null && message.length != 0 ? message[0] : ""), null);
        AndroidUtils.toast(context, (message != null && message.length != 0 ? message[0] : ""), Toast.LENGTH_LONG);
    }

    /*@Override
    protected Boolean doInBackground(File... params) {
        try {
            return upload(params[0]);
        } catch (IOException e) {
            return false;
        }
    }*/

    @Override
    protected void onPostExecute(Boolean result) {
        if (result && successCallback != null)
            successCallback.executeFor(source);
        if (!result && failCallback != null)
            failCallback.executeFor(source);

        if (result)
            AndroidUtils.toast(context, "Image was sent.");
    }
}
