package com.shl.checkpin.android.services.mappers

import android.content.ContentValues
import android.database.Cursor
import com.shl.checkpin.android.model.ImageDoc
import org.json.JSONObject
import java.util.*

/**
 * Created by sesshoumaru on 01.03.16.
 */
object ImageDocMapper {
    val imageDocSchemeMap: MutableMap<String, Int> = HashMap()

    init {
        for (i in 0..ImageDoc.TABLE_SELECT_COLUMNS.size - 1)
            imageDocSchemeMap.put(ImageDoc.TABLE_SELECT_COLUMNS[i], i)
    }

    fun map2Db(image: ImageDoc): ContentValues {
        var cv = ContentValues()
        cv.put(ImageDoc.NAME, image.name)
        cv.put(ImageDoc.CREATION_DATE, image.creationDate.time)
        cv.put(ImageDoc.STATUS, image.status.toString())
        cv.put(ImageDoc.AMOUNT, image.amount)
        cv.put(ImageDoc.URL, image.url)
        cv.put(ImageDoc.RETAILER, image.retailer)
        return cv
    }

    fun map(image: Cursor): ImageDoc? {
        val dateInMs = getCursorValue(image, ImageDoc.CREATION_DATE) ?: return null
        val date = Date(dateInMs.toLong())
        var result = ImageDoc(date)
        val status = getCursorValue(image, ImageDoc.STATUS)
        if (status != null) result.status = ImageDoc.Status.valueOf(status)
        val amount = getCursorValue(image, ImageDoc.AMOUNT)
        if (amount != null) result.amount = amount.toDouble()
        result.retailer = getCursorValue(image, ImageDoc.RETAILER)
        result.url = getCursorValue(image, ImageDoc.URL)
        return result
    }

    private fun getCursorValue(cursor: Cursor, key: String): String? {
        val index = imageDocSchemeMap[key]
        return if (index == null) null else cursor.getString(index)
    }

    fun map(image: JSONObject): ImageDoc? {
        if (image.has(ImageDoc.CREATION_DATE)) {
            val dateInMs = image.getLong(ImageDoc.CREATION_DATE);
            var result = ImageDoc(Date(dateInMs))
            if (image.has(ImageDoc.STATUS)) result.status = ImageDoc.Status.valueOf(image.getString(ImageDoc.STATUS))
            if (image.has(ImageDoc.RETAILER)) result.retailer = image.getString(ImageDoc.RETAILER)
            if (image.has(ImageDoc.AMOUNT)) result.amount = image.getDouble(ImageDoc.AMOUNT)
            if (image.has(ImageDoc.URL)) result.url = image.getString(ImageDoc.URL)
            return result
        }
        return null;
    }

    fun map2Json(image: ImageDoc): JSONObject {
        var result = JSONObject()
        result.put(ImageDoc.NAME, image.name)
        result.put(ImageDoc.CREATION_DATE, image.creationDate.time)
        result.put(ImageDoc.STATUS, image.status)
        result.put(ImageDoc.AMOUNT, image.amount)
        result.put(ImageDoc.URL, image.url)
        result.put(ImageDoc.RETAILER, image.retailer)
        return result;
    }
}
