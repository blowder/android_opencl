package billcollector.app.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by sesshoumaru on 08.11.15.
 */
public class FSFileLocator implements FileLocator {
    public enum FSType {
        TEMP,
        APPLICATION,
        EXTERNAL
    }

    private FSType storageType;

    public FSFileLocator(FSType storageType) {
        this.storageType = storageType;
    }

    @Override
    public File locate(String context, String name) {
        return new File(getRoot().getAbsolutePath()
                + File.separator + context
                + File.separator + name);
    }

    @Override
    public File getRoot() {
        switch (storageType) {
            case APPLICATION:
                return Environment.getDataDirectory();
            case EXTERNAL:
                return Environment.getExternalStorageDirectory();
            case TEMP:
                return Environment.getDownloadCacheDirectory();
            default:
                return null;
        }
    }

    @Override
    public boolean isStorageWritable() {
        return !FSType.EXTERNAL.equals(storageType)
                || Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

    }

    @Override
    public boolean isStorageReadable() {
        String state = Environment.getExternalStorageState();
        return !FSType.EXTERNAL.equals(storageType)
                || Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
