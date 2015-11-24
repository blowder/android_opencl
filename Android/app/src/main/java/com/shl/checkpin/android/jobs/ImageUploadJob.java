package com.shl.checkpin.android.jobs;

import com.shl.checkpin.android.dto.UploadConf;
import com.shl.checkpin.android.requests.AuthTokenRequest;
import com.shl.checkpin.android.requests.UploadConfigurationRequest;
import com.shl.checkpin.android.requests.UploadImageRequest;
import com.shl.checkpin.android.services.JobHolder;
import com.shl.checkpin.android.utils.Constants;
import com.google.common.io.Files;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import retrofit.RestAdapter;

import java.io.File;
import java.util.Arrays;

/**
 * Created by sesshoumaru on 16.11.15.
 */
public class ImageUploadJob extends Job {
    public static final int PRIORITY = 5;
    private File image;
    private String phoneNumber;
    private JobHolder jobHolder;
    RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(Constants.SERVER_HOST)
            .build();

    public ImageUploadJob(JobHolder jobHolder, File image, String imei) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.image = image;
        this.phoneNumber = imei;
        this.jobHolder = jobHolder;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        String token = restAdapter.create(AuthTokenRequest.class).get(phoneNumber);

        UploadConf uploadConf = restAdapter.create(UploadConfigurationRequest.class).getConfiguration();
        if (image == null || !image.exists()) {
            throw new Exception("Image don`t exists!!");
        }
        if (image.length() > uploadConf.getMaxSize())
            throw new Exception("Image size is more than acceptable limit " + uploadConf.getMaxSize());

        byte[] fileInBytes = Files.toByteArray(image);
        int chunkSize = uploadConf.getChunkSize();
        int chunksTotal = (int) Math.ceil(fileInBytes.length / chunkSize);

        int chunkId = 0;
        for (int i = 0; i < fileInBytes.length; i = i + chunkSize) {
            int leftLimit = i + chunkSize > fileInBytes.length ? fileInBytes.length : i + chunkSize;
            byte[] data = Arrays.copyOfRange(fileInBytes, i, leftLimit);
            restAdapter.create(UploadImageRequest.class).upload(token, chunkId, chunksTotal, data);
            chunkId++;
        }
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
