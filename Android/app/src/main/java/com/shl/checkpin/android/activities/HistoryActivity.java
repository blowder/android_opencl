package com.shl.checkpin.android.activities;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.graphics.Canvas;
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask;
import com.shl.checkpin.android.jobs.ImageUploadTask;
import com.shl.checkpin.android.model.ImageDoc;
import com.shl.checkpin.android.model.ImageDocService;
import com.shl.checkpin.android.services.UploadService;
import com.shl.checkpin.android.utils.*;
import android.graphics.Matrix;
import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;


import java.io.File;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sesshoumaru on 16.01.16.
 */
public class HistoryActivity extends AbstractActivity {
    @Inject
    @Named(Constants.HIGHRES)
    FileLocator highResLocator;
    @Inject
    @Named(Constants.LOWRES)
    FileLocator lowResLocator;
    @Inject
    @Named(Constants.ICONS)
    FileLocator iconsLocator;

    @Inject
    ImageDocService imageDocService;

    private static final String pattern = "^[0-9]{8}-[0-9]{6}\\.png$";
    private final DateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final DateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private List<File> files;
    private MyAdapter adapter;

    private RecyclerView listView;
    private RecyclerView.LayoutManager mLayoutManager;
    private static Bitmap sendIcon;
    private static Bitmap deleteIcon;
    private UploadService uploadService = new UploadService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_history_layuot);
        listView = (RecyclerView) findViewById(R.id.history_list);
        files = getImages();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(mLayoutManager);
        OnIconClickListener onIconClickListener = new OnIconClickListener() {
            public void onIconClick(View view, int position) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(files.get(position));
                intent.setDataAndType(uri, "image/*");
                HistoryActivity.this.startActivity(intent);
            }
        };

        adapter = new MyAdapter(files, onIconClickListener);

        listView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    int iconSize = (itemView.getTop() - itemView.getBottom()) / 2;
                    if (isCurrentlyActive) {
                        if (dX < 0) {
                            if (deleteIcon == null)
                                deleteIcon = getBitmapById(R.raw.delete_icon, iconSize, iconSize);

                            int drawableX = itemView.getRight() - (deleteIcon.getWidth() + deleteIcon.getWidth() / 4);
                            int drawableY = itemView.getTop() + deleteIcon.getWidth() / 2;
                            c.drawBitmap(deleteIcon, drawableX, drawableY, null);
                        }
                        if (dX > 0) {
                            if (sendIcon == null)
                                sendIcon = getBitmapById(R.raw.send_icon, iconSize, iconSize);

                            int drawableX = itemView.getLeft() + sendIcon.getWidth() / 4;
                            int drawableY = itemView.getTop() + sendIcon.getWidth() / 2;
                            c.drawBitmap(sendIcon, drawableX, drawableY, null);
                        }
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            private Bitmap getBitmapById(int id, int width, int height) {
                Bitmap result = BitmapFactory.decodeResource(getApplicationContext().getResources(), id);
                result = Bitmap.createScaledBitmap(result, width, height, false);
                //TODO: remove this spike
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                return Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (swipeDir == ItemTouchHelper.RIGHT) {
                    File source = ((HistoryViewHolder) viewHolder).file;
                    int position = files.indexOf(source);
                    adapter.notifyItemChanged(position);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HistoryActivity.this);
                    boolean offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false);
                    if (AndroidUtils.isInetConnected(HistoryActivity.this)
                            && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)
                            && !offlineMode) {
                        ImageDoc imageDoc = imageDocService.findByName(source.getName());
                        uploadService.addForUpload(imageDoc);
                        uploadService.uploadAll();
                        //new ImageUploadTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, imageDoc);
                    } else {
                        AndroidUtils.toast(HistoryActivity.this, "Sorry there is no Internet connection or you try to send existed file", Toast.LENGTH_LONG);
                    }
                }
                if (swipeDir == ItemTouchHelper.LEFT) {
                    File source = ((HistoryViewHolder) viewHolder).file;
                    File lowRes = lowResLocator.locate(null, source.getName());
                    File icon = iconsLocator.locate(null, source.getName());
                    int position = files.indexOf(source);
                    files.remove(source);
                    source.delete();
                    lowRes.delete();
                    icon.delete();
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, files.size());
                }
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(listView);


    }

    class MyAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
        private final List<File> images;
        private OnIconClickListener listener;


        public MyAdapter(List<File> images) {
            this.images = images;
        }

        public MyAdapter(List<File> images, OnIconClickListener listener) {
            this.images = images;
            this.listener = listener;
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
            return new HistoryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            File lowRes = lowResLocator.locate(null, images.get(position).getName());
            File icon = iconsLocator.locate(null, images.get(position).getName());
            if (!icon.exists() && lowRes.exists()) {
                int pixels = AndroidUtils.mmInPixels(HistoryActivity.this, 20);
                new ImageThumbnailCreateTask(pixels, pixels, icon, HistoryActivity.this, null).execute(lowRes);
            } else {
                holder.image.setImageDrawable(Drawable.createFromPath(icon.getAbsolutePath()));
            }
            try {
                String name = images.get(position).getName();
                Date date = fileNameFormat.parse(name.replace(".png", ""));
                holder.date.setText(dateFormat.format(date));
                holder.time.setText(timeFormat.format(date));
                ImageDoc imageDoc = imageDocService.findByName(name);
                if (imageDoc != null)
                    holder.statusIcon.setImageDrawable(getDrawableByType(imageDoc.getStatus(), true));

            } catch (ParseException e) {
                //do not show text
            }
            holder.file = images.get(position);
            final int index = position;
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onIconClick(v, index);
                }
            });
        }

        private Drawable getDrawableByType(ImageDoc.Status status, boolean white) {
            int iconId = white ? R.drawable.check_status_not_sent_w : R.drawable.check_status_not_sent_b;
            switch (status) {
                case NEW:
                    iconId = white ? R.drawable.check_status_not_sent_w : R.drawable.check_status_not_sent_b;
                    break;
                case OFFLINE:
                    iconId = white ? R.drawable.check_status_offline_w : R.drawable.check_status_offline_b;
                    break;
                case SEND:
                    iconId = white ? R.drawable.check_status_sent_w : R.drawable.check_status_sent_b;
                    break;
            }
            return getResources().getDrawable(iconId);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    private List<File> getImages() {
        List<File> files = new ArrayList<File>();
        for (File file : highResLocator.locate(null))
            if (file.getName().matches(pattern)) {
                files.add(file);
                saveToDbIfNotSaved(file);
            }
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return new Date(rhs.lastModified()).compareTo(new Date(lhs.lastModified()));
            }
        });
        return files;
    }

    private void saveToDbIfNotSaved(File file) {
        try {
            if (imageDocService.findByName(file.getName()) == null) {
                String date = file.getName().replace(".png", "");
                Date creationDate = fileNameFormat.parse(date);
                imageDocService.create(new ImageDoc(creationDate));
            }
        } catch (ParseException e) {
            //skip this
        }
    }
}
