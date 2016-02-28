package com.shl.checkpin.android.services;

import android.util.Log;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.utils.FileLocator;
import org.json.JSONException;
import org.json.JSONObject;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public class ImageDocDbService extends SQLiteOpenHelper implements ImageDocService  {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "imagedb";
    private static final String TABLE_NAME = "image";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String STATUS = "status";
    private static final String CREATION_DATE = "date";
    private static final String AMOUNT = "amount";
    private static final String URL = "url";
    private static final String RETAILER = "retailer";

    private static final String TAG = ImageDocDbService.class.getSimpleName();
    
    public ImageDocDbService(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      String createTable = "CREATE TABLE " + DB_NAME + " ("
        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
        + NAME + " TEXT NOT NULL, " 
        + STATUS + " TEXT NOT NULL, "
        + CREATION_DATE + " TEXT, " 
        + AMOUNT + " DOUBLE, " 
        + URL + " TEXT, " 
        + RETAILER + " TEXT" 
        + ")";
db.execSQL(createTable);
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  if(DB_VERSION < newVersion) {
     db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
     onCreate(db);
 }
}


@Override
public void create(ImageDoc imageDoc) {
    try {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NAME, imageDoc.getName());
        cv.put(CREATION_DATE, imageDoc.getCreationDate().getTime());
        cv.put(STATUS, imageDoc.getStatus());
        cv.put(AMOUNT, imageDoc.getAmount());
        cv.put(URL, imageDoc.getUrl());
        cv.put(RETAILER, imageDoc.getRetailer());
        db.insert(TABLE_NAME, null, cv);
        db.close();
    } catch (Exception e) {
        Log.e(TAG, "Could not create " + imageDoc);
    }
}

@Override
public ImageDoc findByName(String name) {
    ImageDoc result = null;
    try {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { ID, NAME,STATUS,CREATION_DATE,AMOUNT,URL,RETAILER},
            NAME + "=?", new String[] {name}, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            result = new ImageDoc(new Date(cursor.getString(3)));
            result.setStatus(ImageDoc.Status.valueOf(cursor.getString(2)));
            double amount = 0.0;
            try {
                amount = Double.parseDouble(cursor.getString(4));
            } catch (JSONException e) {
                    //skip this
            } catch (NumberFormatException e) {
                    //skip this
            }
            result.setAmount(amount);
            try {
                result.setUrl(cursor.getString(5));
            } catch (JSONException e) {
                    //skip this
            }
            try {
                result.setRetailer(cursor.getString(6));
            } catch (JSONException e) {
                    //skip this
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "Could not find " + name, e);
    }
    return result;
}

@Override
public void update(ImageDoc imageDoc) {
    create(imageDoc);
}

@Override
public void delete(ImageDoc imageDoc) {
  SQLiteDatabase db = this.getWritableDatabase();
  db.delete(TABLE_NAME, NAME + "=?", new String[] { imageDoc.getName()});
}
}
