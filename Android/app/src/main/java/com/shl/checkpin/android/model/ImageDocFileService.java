package com.shl.checkpin.android.model;

import android.util.Log;
import com.shl.checkpin.android.utils.FileLocator;
import org.json.JSONObject;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public class ImageDocFileService implements ImageDocService {
    public static final String NAME = "name";
    private static final String STATUS = "status";
    private static final String CREATION_DATE = "date";
    private static final String TAG = ImageDocFileService.class.getSimpleName();
    private final FileLocator locator;

    public ImageDocFileService(FileLocator locator) {
        this.locator = locator;
    }

    @Override
    public void create(ImageDoc imageDoc) {
        try {
            JSONObject json = new JSONObject();
            json.put(NAME, imageDoc.getName());
            json.put(CREATION_DATE, imageDoc.getCreationDate().getTime());
            json.put(STATUS, imageDoc.getStatus());
            File source = locator.locate(null, imageDoc.getName());
            source.delete();
            FileWriter fileWriter = new FileWriter(source);
            fileWriter.write(json.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not create " + imageDoc);
        }
    }

    @Override
    public ImageDoc findByName(String name) {
        File source = locator.locate(null, name);
        try {
            String content = new Scanner(source).useDelimiter("\\Z").next();
            JSONObject object = new JSONObject(content);
            long time = object.getLong(CREATION_DATE);
            ImageDoc result = new ImageDoc(new Date(time));
            result.setStatus(ImageDoc.Status.valueOf(object.getString(STATUS)));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Could not find " + name);
        }
        return null;
    }

    @Override
    public void update(ImageDoc imageDoc) {
        create(imageDoc);
    }

    @Override
    public void delete(ImageDoc imageDoc) {
        locator.locate(null, imageDoc.getName()).delete();
    }
}
