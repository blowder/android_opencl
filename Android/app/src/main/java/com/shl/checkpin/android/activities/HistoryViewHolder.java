package com.shl.checkpin.android.activities;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.shl.checkpin.android.R;

import java.io.File;

/**
 * Created by sesshoumaru on 17.01.16.
 */
public class HistoryViewHolder extends RecyclerView.ViewHolder {
    CardView cv;
    ImageView image;
    TextView date;
    TextView time;
    ImageView statusIcon;
    File file;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.card_view);
        image = (ImageView) itemView.findViewById(R.id.imageView2);
        date = (TextView) itemView.findViewById(R.id.dateView);
        time = (TextView) itemView.findViewById(R.id.timeView);
        statusIcon = (ImageView) itemView.findViewById(R.id.status_icon);
    }
}
