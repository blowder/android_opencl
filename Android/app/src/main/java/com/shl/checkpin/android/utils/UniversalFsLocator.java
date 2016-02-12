package com.shl.checkpin.android.utils;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sesshoumaru on 12.02.16.
 */
public class UniversalFsLocator implements FileLocator {
    private File root;

    public UniversalFsLocator(File root) {
        this.root = root;
    }

    @Override
    public File locate(String context, String name) {
        return new File(getRoot().getAbsolutePath()
                + (context != null && !context.isEmpty() ? File.separator + context : "")
                + File.separator + name);
    }

    @Override
    public List<File> locate(String context) {
        final File result = new File(getRoot().getAbsolutePath()
                + (context != null && !context.isEmpty() ? File.separator + context : ""));
        return result.isDirectory() ? Arrays.asList(result.listFiles()) : new ArrayList<File>() {{
            add(result);
        }};
    }


    @Override
    public File getRoot() {
        return root;
    }

}
