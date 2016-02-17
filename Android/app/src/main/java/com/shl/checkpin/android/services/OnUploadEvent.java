package com.shl.checkpin.android.services;

import java.io.File;

/**
 * Created by sesshoumaru on 17.02.16.
 */
public interface OnUploadEvent {
    void executeFor(File image);
}
