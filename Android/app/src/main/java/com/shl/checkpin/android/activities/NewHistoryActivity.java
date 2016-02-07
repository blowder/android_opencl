package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.shl.checkpin.android.utils.*;
import android.graphics.Matrix;
import android.content.Intent;
import android.net.Uri;
import java.text.ParseException;


import java.io.File;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sesshoumaru on 16.01.16.
 */
public class NewHistoryActivity extends Activity {
    private static final String pattern = "^[0-9]{8}-[0-9]{6}\\.png$";
    private final DateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final DateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private List<File> files;
    private SharedPreferences sharedPreferences;

    private MyAdapter adapter;

    private RecyclerView listView;
    private RecyclerView.LayoutManager mLayoutManager;
    private static Bitmap sendIcon;
    private static Bitmap deleteIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_history_layuot);
        listView = (RecyclerView) findViewById(R.id.history_list);
        files = getImages();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        listView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(mLayoutManager);
        OnIconClickListener onIconClickListener = new OnIconClickListener(){
            public void onIconClick(View view, int position){
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(files.get(position));
                intent.setDataAndType(uri, "image/*");
                NewHistoryActivity.this.startActivity(intent);
            }  
        };

        adapter = new MyAdapter(files, onIconClickListener);

        listView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    int iconSize = (int)((itemView.getTop()-itemView.getBottom())/2);
                    
                    if (dX < 0) {                      
                        if(sendIcon==null)
                            sendIcon = getBitmapById(R.raw.send_icon, iconSize, iconSize);                            
                        
                        int drawableX = itemView.getRight()-(sendIcon.getWidth() + sendIcon.getWidth()/4);
                        int drawableY = itemView.getTop() + sendIcon.getWidth()/2;                        
                        c.drawBitmap(sendIcon, drawableX, drawableY, null);                       
                    }
                    if (dX > 0) {
                        if(deleteIcon==null)
                            deleteIcon = getBitmapById(R.raw.delete_icon, iconSize, iconSize);                                                          
                        
                        int drawableX = itemView.getLeft() + deleteIcon.getWidth()/4;
                        int drawableY = itemView.getTop() + deleteIcon.getWidth()/2;                        
                        c.drawBitmap(deleteIcon, drawableX, drawableY, null);                       
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
            private Bitmap getBitmapById(int id, int width, int height){
                Bitmap result = BitmapFactory.decodeResource(getApplicationContext().getResources(), id);                        
                result = Bitmap.createScaledBitmap(result, width, height, false);                                                   
                //TODO: remove this spike
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                return Bitmap.createBitmap(result , 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (swipeDir == ItemTouchHelper.LEFT) {
                    File source = ((HistoryViewHolder) viewHolder).file;
                    int position = files.indexOf(source);
                    files.remove(source);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, files.size());
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NewHistoryActivity.this);
                    boolean offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false);
                    if (AndroidUtils.isInetConnected(NewHistoryActivity.this) && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)&& !offlineMode) {
                        String gcmToken = sharedPreferences.getString(Constants.GCM_TOKEN, "");
                        new ImageUploadTask(NewHistoryActivity.this, AndroidUtils.getPhoneNumber(NewHistoryActivity.this), gcmToken)
                        .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, source);
                    } else {
                        AndroidUtils.toast(NewHistoryActivity.this, "Sorry there is no Internet connection or you try to send existed file", Toast.LENGTH_LONG);
                    }
                }
                if (swipeDir == ItemTouchHelper.RIGHT) {
                    File source = ((HistoryViewHolder) viewHolder).file;
                    File thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, source.getName());
                    File lowRes = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_LOWRES, source.getName());
                    int position = files.indexOf(source);
                    files.remove(source);
                    source.delete();
                    thumbnail.delete();
                    lowRes.delete();
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
            File thumbnail = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_THUMB, images.get(position).getName());
            File lowRes = appFileLocator.locate(Environment.DIRECTORY_PICTURES, FileType.IMAGE_LOWRES, images.get(position).getName());
            if (!lowRes.exists() && thumbnail.exists()) {
                int pixels = AndroidUtils.mmInPixels(NewHistoryActivity.this, 20);
                new ImageThumbnailCreateTask(pixels, pixels, lowRes, NewHistoryActivity.this, null).execute(thumbnail);
            }
            holder.image.setImageDrawable(Drawable.createFromPath(lowRes.getAbsolutePath()));
            try{
                Date date = fileNameFormat.parse(images.get(position).getName().replace(".png",""));
                holder.date.setText(dateFormat.format(date));
                holder.time.setText(timeFormat.format(date));    
            }catch(ParseException e){
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

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    private List<File> getImages() {
        List<File> files = new ArrayList<File>();
        for (File file : appFileLocator.locate(Environment.DIRECTORY_PICTURES))
            if (file.getName().matches(pattern))
                files.add(file);
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return new Date(rhs.lastModified()).compareTo(new Date(lhs.lastModified()));
                }
            });
            return files;
        }

    }
