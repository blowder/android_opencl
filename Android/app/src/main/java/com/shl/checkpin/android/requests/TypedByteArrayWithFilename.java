package com.shl.checkpin.android.requests;

import retrofit.mime.TypedByteArray;

/**
 * Created by vfedin on 29.11.2015.
 */
public class TypedByteArrayWithFilename extends TypedByteArray {
    private String fileName;

    public TypedByteArrayWithFilename(String mimeType, byte[] bytes, String fileName) {
        super(mimeType, bytes);
        this.fileName = fileName;
    }

    @Override public String fileName() {
        return fileName;
    }
}
