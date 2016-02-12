package com.shl.checkpin.android.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sesshoumaru on 09.02.16.
 */
public class ImageDoc {
    public static final String NAME_PATTERN = "yyyyMMdd-HHmmss";

    public enum Status {
        NEW,
        OFFLINE,
        SEND
    }

    private String name;
    private Date creationDate;
    private Status status;

    public ImageDoc(Date creationDate) {
        DateFormat nameFormat = new SimpleDateFormat(NAME_PATTERN, Locale.getDefault());
        this.name = nameFormat.format(creationDate) + ".png";
        this.creationDate = creationDate;
        this.status = Status.NEW;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
