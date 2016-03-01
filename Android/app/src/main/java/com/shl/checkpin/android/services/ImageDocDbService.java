package com.shl.checkpin.android.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.services.mappers.ImageDocMapper;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sesshoumaru on 14.02.16.
 */
public class ImageDocDbService extends SQLiteOpenHelper implements ImageDocService {
    private static final String TAG = ImageDocDbService.class.getSimpleName();
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "image_db";

    public ImageDocDbService(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + ImageDoc.TABLE_NAME + " ("
                + ImageDoc.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ImageDoc.NAME + " TEXT NOT NULL, "
                + ImageDoc.STATUS + " TEXT NOT NULL, "
                + ImageDoc.CREATION_DATE + " TEXT, "
                + ImageDoc.AMOUNT + " DOUBLE, "
                + ImageDoc.URL + " TEXT, "
                + ImageDoc.RETAILER + " TEXT"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + ImageDoc.TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public void create(ImageDoc imageDoc) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues cv = ImageDocMapper.INSTANCE.map(imageDoc);
            db.insert(ImageDoc.TABLE_NAME, null, cv);
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not create " + imageDoc);
        }finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public ImageDoc findByName(String name) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(ImageDoc.TABLE_NAME, ImageDoc.TABLE_SELECT_COLUMNS,
                    ImageDoc.NAME + "=?", new String[]{name}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                return ImageDocMapper.INSTANCE.map(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not find " + name, e);
        }
        return null;
    }

    @Override
    public void update(ImageDoc imageDoc) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues cv = ImageDocMapper.INSTANCE.map(imageDoc);
            db.update(ImageDoc.TABLE_NAME, cv, ImageDoc.NAME + "=?", new String[]{imageDoc.getName()});
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not create " + imageDoc);
        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void delete(ImageDoc imageDoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ImageDoc.TABLE_NAME, ImageDoc.NAME + "=?", new String[]{imageDoc.getName()});
    }
}
