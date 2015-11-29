package com.shl.checkpin.android.jobs;

import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.shl.checkpin.android.dto.UploadConfDTO;
import com.shl.checkpin.android.requests.*;
import com.shl.checkpin.android.services.JobHolder;
import com.shl.checkpin.android.utils.Constants;
import com.google.common.io.Files;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Created by sesshoumaru on 16.11.15.
 */
public class ImageUploadJob extends Job {
    private static final String TAG= "ImageUploadJob";
    public static final int PRIORITY = 5;
    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
    private File image;
    private String phoneNumber;
    private String googleToken;
    private JobHolder jobHolder;

    RestAdapter authRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST+":8080")
            .build();

    RestAdapter uploadRestAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST)
            .build();

    public ImageUploadJob(JobHolder jobHolder, File image, String phoneNumber, String googleToken) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.image = image;
        this.phoneNumber = phoneNumber;
        this.jobHolder = jobHolder;
        this.googleToken = googleToken;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        //authorization and registration
        UploadTokenRequest uploadTokenRequest = authRestAdapter.create(UploadTokenRequest.class);
        String uploadToken = uploadTokenRequest.get(phoneNumber).getToken();

        GcmRegisterRequest gcmRegisterRequest = authRestAdapter.create(GcmRegisterRequest.class);
        int responseCode = gcmRegisterRequest.register(uploadToken, googleToken).getStatus();
        Log.i(TAG, responseCode==200
                ? "User with number"+phoneNumber+" was register on upload service"
                : "Error during registration of " + phoneNumber + " occurred ");

        //get configuration
        UploadConfigurationRequest conf = uploadRestAdapter.create(UploadConfigurationRequest.class);
        UploadConfDTO uploadConf = conf.getConfiguration();

        //image upload
        byte[] fileInBytes = ByteStreams.toByteArray(new FileInputStream(image));
        String filename = dateFormat.format(new Date());
        int chunkSize = uploadConf.getChunkSize();
        int chunksTotal = (int) Math.ceil((double) fileInBytes.length / chunkSize);

        int chunkId = 1;

        try {
            for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
                int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
                byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
                TypedInput typedBytes = new TypedByteArrayWithFilename("multipart/form-data",  data, filename);
                uploadRestAdapter.create(UploadImageRequest.class).upload(uploadToken, chunkId, chunksTotal, typedBytes);
                chunkId++;
            }
            Log.i(TAG,"File "+ image + " was uploaded");
        }catch (RetrofitError e ){
            if(e.getResponse()!=null
                    &&e.getResponse().getBody()!=null
                    &&e.getResponse().getBody().in()!=null){
                InputStream is = e.getResponse().getBody().in();
                Log.d(TAG, new String(ByteStreams.toByteArray(is)));
                Closeables.closeQuietly(is);
            }else{
                Log.e(TAG,"Unexpected error occurred ", e);
            }
        }
        //job status set
        jobHolder.setStatus(image, JobHolder.Status.SENT);
        jobHolder.removeJob(image);
    }


    @Override
    protected void onCancel() {
        image.delete();
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
