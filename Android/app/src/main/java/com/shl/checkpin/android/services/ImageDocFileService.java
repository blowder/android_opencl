package com.shl.checkpin.android.services;

import android.util.Log;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.utils.FileLocator;
import org.json.JSONException;
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
    private static final String AMOUNT = "amount";
    private static final String URL = "url";
    private static final String RETAILER = "retailer";

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
            json.put(AMOUNT, imageDoc.getAmount());
            json.put(URL, imageDoc.getUrl());
            json.put(RETAILER, imageDoc.getRetailer());

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
        ImageDoc result = null;
        File source = locator.locate(null, name);
        try {
            String content = new Scanner(source).useDelimiter("\\Z").next();
            JSONObject object = new JSONObject(content);
            long time = object.getLong(CREATION_DATE);
            result = new ImageDoc(new Date(time));
            result.setStatus(ImageDoc.Status.valueOf(object.getString(STATUS)));
            double amount = 0.0;
            try {
                amount = Double.parseDouble(object.getString(AMOUNT));
            } catch (JSONException e) {
                //skip this
            } catch (NumberFormatException e) {
                //skip this
            }
            result.setAmount(amount);
            try {
                result.setUrl(object.getString(URL));
            } catch (JSONException e) {
                //skip this
            }
            try {
                result.setRetailer(object.getString(RETAILER));
            } catch (JSONException e) {
                //skip this
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not find " + name, e);
        }
        return result;
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