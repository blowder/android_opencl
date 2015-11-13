package billcollector.app.utils;

import java.io.File;

import static android.os.Environment.DIRECTORY_PICTURES;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * Created by sesshoumaru on 19.09.15.
 */
public class Constants {
    public static String APP_NAME = "BillReaper";
    public static String APP_IMAGE_FOLDER = "";
    //public static String APP_IMAGE_FOLDER = new File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), APP_NAME).getAbsolutePath();
    public static String SERVER_HOST = "http://checkpin.easymart.com.ua";
}
