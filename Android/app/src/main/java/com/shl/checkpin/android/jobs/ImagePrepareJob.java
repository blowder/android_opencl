package com.shl.checkpin.android.jobs;

import com.shl.checkpin.android.services.JobHolder;
import com.shl.checkpin.android.utils.OpenCvUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.util.*;

/**
 * Created by sesshoumaru on 15.11.15.
 */
public class ImagePrepareJob extends Job {
    private JobHolder jobHolder;
    public static final int PRIORITY = 10;
    private File image;

    public ImagePrepareJob(JobHolder jobHolder, File image) {
        super(new Params(PRIORITY));
        this.image = image;
        this.jobHolder = jobHolder;
    }

    @Override
    public void onAdded() {
        System.out.println("Preparing image: " + image);
    }

    @Override
    public void onRun() throws Throwable {
        System.out.println("Image prepare job was started");
        Mat temp = Highgui.imread(image.getAbsolutePath());
        List<Rect> rects = new ArrayList<Rect>(OpenCvUtils.findSquares(temp));
        Collections.sort(rects, new Comparator<Rect>() {
            @Override
            public int compare(Rect lhs, Rect rhs) {
                if (rhs.area() < lhs.area())
                    return -1;
                else if (rhs.area() < lhs.area())
                    return 1;
                else
                    return 0;
            }
        });
        try {
            if (rects.size() == 0)
                throw new Exception("Frame of bill was not detected on photo!!!");

            //crop main image to this rectangle
            Mat ROI = OpenCvUtils.cropByRect(temp, rects.iterator().next());
            //look for text on crop rectangle
            if (!OpenCvUtils.ifContainText(ROI))
                throw new Exception("Frame of bill was not detected on photo!!!");
            //writing crop result

            Highgui.imwrite(image.getAbsolutePath(), ROI);
            OpenCvUtils.adaptiveThreshold(image, image);
        } finally {
            image.delete();
            jobHolder.setStatus(image, JobHolder.Status.PREPARED);
        }
    }

    @Override
    protected void onCancel() {
        image.delete();
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
