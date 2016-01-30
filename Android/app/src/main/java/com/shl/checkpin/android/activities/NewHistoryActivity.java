package com.shl.checkpin.android.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import com.shl.checkpin.android.R;
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask;
import com.shl.checkpin.android.jobs.ImageUploadTask;
import com.shl.checkpin.android.utils.*;

import java.io.File;
import java.util.*;

/**
 * Created by sesshoumaru on 16.01.16.
 */
public class NewHistoryActivity extends Activity {
    private static final String pattern = "^[0-9]{8}-[0-9]{6}\\.png$";
    private FileLocator appFileLocator = new FSFileLocator(FSFileLocator.FSType.EXTERNAL);
    private List<File> files;
    private SharedPreferences sharedPreferences;

    private MyAdapter adapter;

    private RecyclerView listView;
    private RecyclerView.LayoutManager mLayoutManager;


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

        adapter = new MyAdapter(files);

        listView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
                    if (AndroidUtils.isInetConnected(NewHistoryActivity.this) && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)) {
                        String gcmToken = sharedPreferences.getString(Constants.GCM_TOKEN, "");
                        new ImageUploadTask(NewHistoryActivity.this, AndroidUtils.getPhoneNumber(NewHistoryActivity.this), gcmToken)
                                .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, source);
                    } else {
                        AndroidUtils.toast(NewHistoryActivity.this, "Sorry there is no Internet connection or you try to send unexisted file", Toast.LENGTH_LONG);
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

        public MyAdapter(List<File> images) {
            this.images = images;
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
            holder.text.setText(images.get(position).getName());
            holder.file = images.get(position);
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
