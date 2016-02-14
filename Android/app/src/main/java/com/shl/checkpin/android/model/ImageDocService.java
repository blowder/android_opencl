package com.shl.checkpin.android.model;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public interface ImageDocService {
    void create(ImageDoc imageDoc);

    ImageDoc findByName(String name);

    void update(ImageDoc imageDoc);

    void delete(ImageDoc imageDoc);
}