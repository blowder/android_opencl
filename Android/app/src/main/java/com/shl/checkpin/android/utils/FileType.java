package com.shl.checkpin.android.utils;

/**
 * Created by sesshoumaru on 03.01.16.
 */
public enum FileType {

    IMAGE_THUMB("_thumb.png"),
    IMAGE_UPLOAD_META("_meta");


    private final String prefix;

    FileType(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
