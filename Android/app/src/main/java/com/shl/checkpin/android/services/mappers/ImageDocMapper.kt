package com.shl.checkpin.android.services.mappers

import android.content.ContentValues
import android.database.Cursor
import com.shl.checkpin.android.model.ImageDoc
import java.util.*

/**
 * Created by sesshoumaru on 01.03.16.
 */
object ImageDocMapper {
    val imageDocSchemeMap: MutableMap<String, Int> = HashMap()
    init {
        for (i in 0..ImageDoc.TABLE_SELECT_COLUMNS.size-1)
            imageDocSchemeMap.put(ImageDoc.TABLE_SELECT_COLUMNS[i], i)
    }

    fun map(image: ImageDoc): ContentValues {
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
        val datePosition = imageDocSchemeMap.get(ImageDoc.CREATION_DATE)
        val statusPosition = imageDocSchemeMap.get(ImageDoc.STATUS)
        val retailerPosition = imageDocSchemeMap.get(ImageDoc.RETAILER)
        val amountPosition = imageDocSchemeMap.get(ImageDoc.AMOUNT)
        val urlPosition = imageDocSchemeMap.get(ImageDoc.URL)

        if (image == null || datePosition == null) return null
        val date = Date(image.getLong(datePosition))
        var result = ImageDoc(date)
        if (statusPosition != null)
            result.status = ImageDoc.Status.valueOf(image.getString(statusPosition))
        if (retailerPosition != null)
            result.retailer = image.getString(retailerPosition)
        if (amountPosition != null)
            result.amount = image.getDouble(amountPosition)
        if (urlPosition != null)
            result.url = image.getString(urlPosition)
        return result
    }
}
