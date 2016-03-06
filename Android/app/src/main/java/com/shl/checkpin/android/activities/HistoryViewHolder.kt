package com.shl.checkpin.android.activities

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shl.checkpin.android.R
import com.shl.checkpin.android.model.ImageDoc

/**
 * Created by sesshoumaru on 05.03.16.
 */
class HistoryViewHolder : RecyclerView.ViewHolder {
    var cv: CardView
    var image: ImageView
    var date: TextView
    var time: TextView
    var statusIcon: ImageView
    var imageDoc: ImageDoc? = null

    constructor(view: View) : super(view) {
        cv = itemView.findViewById(R.id.card_view) as CardView
        image = itemView.findViewById(R.id.imageView2) as ImageView
        date = itemView.findViewById(R.id.dateView) as TextView
        time = itemView.findViewById(R.id.timeView) as TextView
        statusIcon = itemView.findViewById(R.id.status_icon) as ImageView
    }
}