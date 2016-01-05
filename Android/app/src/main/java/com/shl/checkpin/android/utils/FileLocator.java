package com.shl.checkpin.android.utils;

import java.io.File;
import java.util.List;


/**
 * Created by sesshoumaru on 08.11.15.
 */
public interface FileLocator {
    File locate(String context, String name);

    List<File> locate(String context);

    File locate(String context, FileType type, String name);

    File getRoot();

    boolean isStorageWritable();

    boolean isStorageReadable();
}
