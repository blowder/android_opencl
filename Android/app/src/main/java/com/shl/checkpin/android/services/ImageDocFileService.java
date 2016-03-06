package com.shl.checkpin.android.services;

import android.util.Log;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.services.mappers.ImageDocMapper;
import com.shl.checkpin.android.utils.FileLocator;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public class ImageDocFileService implements ImageDocService {
    private static final String TAG = ImageDocFileService.class.getSimpleName();
    private final FileLocator locator;

    public ImageDocFileService(FileLocator locator) {
        this.locator = locator;
    }

    @Override
    public void create(ImageDoc imageDoc) {
        FileWriter fileWriter = null;
        try {
            JSONObject json = ImageDocMapper.INSTANCE.map2Json(imageDoc);
            fileWriter = new FileWriter(locator.locate(null, imageDoc.getName()));
            fileWriter.write(json.toString());
        } catch (Exception e) {
            Log.e(TAG, "Could not create " + imageDoc);
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    @Override
    public ImageDoc findByName(String name) {
        try {
            String content = new Scanner(locator.locate(null, name))
                    .useDelimiter("\\Z")
                    .next();
            JSONObject object = new JSONObject(content);
            return ImageDocMapper.INSTANCE.map(object);
        } catch (Exception e) {
            Log.e(TAG, "Could not find " + name, e);
            return null;
        }
    }

    @Override
    public void update(ImageDoc imageDoc) {
        create(imageDoc);
    }

    @Override
    public void delete(ImageDoc imageDoc) {
        locator.locate(null, imageDoc.getName()).delete();
    }

    @Override
    public List<ImageDoc> findAll() {
        List<File> files = locator.locate(null);
        List<ImageDoc> result = new ArrayList<ImageDoc>();
        for (File source : files)
            try {
                String content = new Scanner(source).useDelimiter("\\Z").next();
                JSONObject json = new JSONObject(content);
                result.add(ImageDocMapper.INSTANCE.map(json));
            } catch (Exception e) {
                Log.e(TAG, "Could not parse to json " + source, e);
            }
        return result;
    }
}
