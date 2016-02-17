package com.shl.checkpin.android.factories;

import com.shl.checkpin.android.services.ImageDocFileService;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.utils.Constants;
import com.shl.checkpin.android.utils.FileLocator;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

/**
 * Created by sesshoumaru on 14.02.16.
 */
@Module(library=true, includes = {LocatorModule.class})
public class ImageDocServiceModule {

    @Provides
    ImageDocService getService(@Named(Constants.IMAGE_FILE_DB) FileLocator locator) {
        return new ImageDocFileService(locator);
    }
}
