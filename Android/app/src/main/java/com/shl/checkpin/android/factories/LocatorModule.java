package com.shl.checkpin.android.factories;

import android.app.Application;
import android.os.Environment;

import static com.shl.checkpin.android.utils.Constants.*;

import com.shl.checkpin.android.utils.AndroidUtils;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FileLocator;
import com.shl.checkpin.android.utils.UniversalFsLocator;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import java.io.File;

/**
 * Created by sesshoumaru on 12.02.16.
 */
@Module(library = true)
public class LocatorModule {
    private File storageRoot;

    public LocatorModule(Application application) {
        storageRoot = AndroidUtils.getStorage(application.getApplicationContext());
    }


    @Provides
    @Named(HIGHRES)
    public FileLocator getImageLocator() {
        return createDirsAndLocator(Environment.DIRECTORY_PICTURES, Constants.APP_NAME);
    }

    @Provides
    @Named(LOWRES)
    public FileLocator getLowresLocator() {
        return createDirsAndLocator(APP_FOLDER_NAME, LOWRES);
    }

    @Provides
    @Named(ICONS)
    public FileLocator getIconsLocator() {
        return createDirsAndLocator(APP_FOLDER_NAME, ICONS);
    }

    @Provides
    @Named(IMAGE_FILE_DB)
    public FileLocator getImageInfo() {
        return createDirsAndLocator(APP_FOLDER_NAME, IMAGE_FILE_DB);
    }

    private FileLocator createDirsAndLocator(String contextFolder, String imageFolder) {
        File root = contextFolder != null && !contextFolder.isEmpty()
                ? new File(storageRoot, contextFolder)
                : storageRoot;

        root = new File(root, imageFolder);
        if (!root.exists())
            root.mkdirs();
        return new UniversalFsLocator(root);
    }


}
