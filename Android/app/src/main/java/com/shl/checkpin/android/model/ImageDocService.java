package com.shl.checkpin.android.model;

import java.util.List;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public interface ImageDocService {
    void create(ImageDoc imageDoc);

    ImageDoc findByName(String name);

    void update(ImageDoc imageDoc);

    void delete(ImageDoc imageDoc);

    List<ImageDoc> findAll();
}
