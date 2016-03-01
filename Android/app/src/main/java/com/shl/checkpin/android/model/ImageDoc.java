package com.shl.checkpin.android.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sesshoumaru on 09.02.16.
 */
public class ImageDoc {
    public static final String TABLE_NAME = "images";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String CREATION_DATE = "date";
    public static final String AMOUNT = "amount";
    public static final String URL = "url";
    public static final String RETAILER = "retailer";

    public static final String[] TABLE_SELECT_COLUMNS = new String[]{ID, NAME, STATUS, CREATION_DATE, AMOUNT, URL, RETAILER};

    public static final String NAME_PATTERN = "yyyyMMdd-HHmmss";

    public enum Status {
        EMPTY,
        NEW,
        OFFLINE,
        SEND,
        RECOGNIZED,
        UNRECOGNIZED
    }

    private String name;
    private Date creationDate;
    private Status status;
    private double amount;
    private String retailer;
    private String url;

    public ImageDoc(Date creationDate) {
        DateFormat nameFormat = new SimpleDateFormat(NAME_PATTERN, Locale.getDefault());
        this.name = nameFormat.format(creationDate) + ".png";
        this.creationDate = creationDate;
        this.status = Status.EMPTY;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageDoc imageDoc = (ImageDoc) o;

        if (name != null ? !name.equals(imageDoc.name) : imageDoc.name != null) return false;
        return creationDate != null ? creationDate.equals(imageDoc.creationDate) : imageDoc.creationDate == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
    }
}
