package com.shl.checkpin.android.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.shl.checkpin.android.factories.Injector;
import com.shl.checkpin.android.jobs.ImageUploadTask;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.utils.AndroidUtils;

import javax.inject.Inject;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sesshoumaru on 17.02.16.
 */
public class UploadService {
    public static final String TAG = UploadService.class.getSimpleName();

    @Inject
    ImageDocService imageDocService;

    @Inject
    Context context;

    enum UploadStatus {
        NEW, UPLOADING
    }

    ConcurrentHashMap<ImageDoc, UploadStatus> filesMap = new ConcurrentHashMap<ImageDoc, UploadStatus>();

    OnUploadEvent successCallback = new OnUploadEvent() {
        @Override
        public void executeFor(File image) {
            ImageDoc imageDoc = imageDocService.findByName(image.getName());
            filesMap.remove(imageDoc);
            //AndroidUtils.toast(context, TAG + ": file " + image + " successfully uploaded");
        }
    };

    OnUploadEvent failCallback = new OnUploadEvent() {
        @Override
        public void executeFor(File image) {
            ImageDoc imageDoc = imageDocService.findByName(image.getName());
            filesMap.put(imageDoc, UploadStatus.NEW);
            //AndroidUtils.toast(context, TAG + ": file " + image + " failed during upload");
        }
    };

    public UploadService() {
        Injector.inject(this);
    }

    public void addForUpload(ImageDoc source) {
        if(!UploadStatus.UPLOADING.equals(filesMap.get(source)))
            filesMap.put(source, UploadStatus.NEW);
        //AndroidUtils.toast(context, TAG + ": file " + source.getName() + " added to upload queue");
    }

    public synchronized void uploadAll() {
        //AndroidUtils.toast(context, TAG + ": uploading process started, " + filesMap.size() + " files in upload queue");
        Iterator<Map.Entry<ImageDoc, UploadStatus>> iterator = filesMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ImageDoc, UploadStatus> entry = iterator.next();
            if (UploadStatus.NEW.equals(entry.getValue())) {
                new ImageUploadTask(successCallback, failCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, entry.getKey());
                Log.e(TAG, "call imageUploadTask " + entry.getKey());
                filesMap.put(entry.getKey(), UploadStatus.UPLOADING);
            }
        }
    }

    public void clearUploadList() {
        filesMap.clear();
    }
}


