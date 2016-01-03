package com.shl.checkpin.android.utils;

import java.io.File;


/**
 * Created by sesshoumaru on 08.11.15.
 */
public interface FileLocator {
    File locate(String context, String name);

    File locate(String context, FileType type, String name);

    File getRoot();

    boolean isStorageWritable();

    boolean isStorageReadable();
}
