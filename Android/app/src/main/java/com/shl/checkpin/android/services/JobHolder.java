package com.shl.checkpin.android.services;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.path.android.jobqueue.*;
import com.path.android.jobqueue.config.Configuration;
import com.shl.checkpin.android.jobs.ImagePrepareJob;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by sesshoumaru on 24.11.15.
 */
public class JobHolder {

    public enum Status {
        STARTED,
        PREPARED,
        SENT
    }

    private static ConcurrentHashMap<File, Status> jobs = new ConcurrentHashMap<>();
    private static ConcurrentSkipListSet<File> jobsInProcess = new ConcurrentSkipListSet<>();
    private JobManager jobManager;
    private Context context;

    public JobHolder(Context context) {
        this.context = context;
        this.jobManager = buildJobManager(context);
    }

    public void addJob(File file) {
        if (file.exists() && !jobs.containsKey(file)) {
            jobs.put(file, Status.STARTED);
            runJobs();
        }
    }

    public void removeJob(File file) {
        jobsInProcess.remove(file);
    }

    public void clearJobList() {
        jobsInProcess.clear();
    }

    public Status jobStatus(File file) {
        return jobs.get(file);
    }


    private void runJobs() {
        for (Map.Entry<File, Status> entry : jobs.entrySet())
            if (!jobsInProcess.contains(entry.getKey()) && !Status.SENT.equals(entry.getValue()))
                runJob(entry.getKey());
    }

    private void runJob(File file) {
        System.out.println("Job started for " + file.getAbsolutePath());
        jobManager.addJob(new ImagePrepareJob(file));
        //TODO test and fix upload
        //jobManager.addJob(new ImageUploadJob(file, getPhoneNumber()));
        jobsInProcess.add(file);
    }

    private String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    private JobManager buildJobManager(Context context) {
        Configuration configuration = new Configuration.Builder(context)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        return new JobManager(context, configuration);
    }
}
