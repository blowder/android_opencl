package com.shl.checkpin.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.shl.checkpin.android.R
import com.shl.checkpin.android.jobs.ImageThumbnailCreateTask
import com.shl.checkpin.android.model.ImageDoc
import com.shl.checkpin.android.model.ImageDocService
import com.shl.checkpin.android.services.UploadService
import com.shl.checkpin.android.utils.AndroidUtils
import com.shl.checkpin.android.utils.Constants
import com.shl.checkpin.android.utils.FileLocator
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by sesshoumaru on 06.03.16.
 */
class HistoryActivity2 : AbstractActivity() {

    @field:[Inject Named(Constants.HIGHRES)]
    lateinit var highResLocator: FileLocator

    @field:[Inject Named(Constants.LOWRES)]
    lateinit var lowResLocator: FileLocator

    @field:[Inject Named(Constants.ICONS)]
    lateinit var iconsLocator: FileLocator

    @Inject
    lateinit var imageDocService: ImageDocService

    private lateinit var listView: RecyclerView

    private val onIconClickListener = OnIconClickListener { name ->
        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        val uri = Uri.fromFile(highResLocator.locate(null, name))
        intent.setDataAndType(uri, "image/*")
        this@HistoryActivity2.startActivity(intent)
    }

    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val uploadService = UploadService()
        private var sendIcon: Bitmap? = null
        private var deleteIcon: Bitmap? = null
        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val iconSize = (viewHolder.itemView.top - viewHolder.itemView.bottom) / 2
            if (sendIcon == null) sendIcon = getBitmapById(R.raw.send_icon, iconSize, iconSize)
            if (deleteIcon == null) deleteIcon = getBitmapById(R.raw.delete_icon, iconSize, iconSize)

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView
                if (isCurrentlyActive ) {
                    if (dX < 0) {
                        val drawableX = itemView.right - (deleteIcon!!.width + deleteIcon!!.width / 4)
                        val drawableY = itemView.top + deleteIcon!!.width / 2
                        c.drawBitmap(deleteIcon, drawableX.toFloat(), drawableY.toFloat(), null)
                    }
                    if (dX > 0) {
                        val drawableX = itemView.left + sendIcon!!.width / 4
                        val drawableY = itemView.top + sendIcon!!.width / 2
                        c.drawBitmap(sendIcon, drawableX.toFloat(), drawableY.toFloat(), null)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        private fun getBitmapById(id: Int, width: Int, height: Int): Bitmap {
            var result = BitmapFactory.decodeResource(applicationContext.resources, id)
            result = Bitmap.createScaledBitmap(result, width, height, false)
            //TODO: remove this spike
            val matrix = Matrix()
            matrix.postRotate(180f)
            return Bitmap.createBitmap(result, 0, 0, result.width, result.height, matrix, true)
        }


        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            var adapter: HistoryViewAdapter = listView.adapter as HistoryViewAdapter
            if (swipeDir == ItemTouchHelper.RIGHT) {
                val imageDoc = (viewHolder as HistoryViewHolder).imageDoc
                val position = adapter.getItemsList().indexOf(imageDoc)
                adapter.notifyItemChanged(position)
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@HistoryActivity2)
                val offlineMode = sharedPreferences.getBoolean(Constants.OFFLINE_MODE, false)
                if (AndroidUtils.isInetConnected(this@HistoryActivity2)
                        && sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false)
                        && !offlineMode) {
                    uploadService.addForUpload(imageDoc)
                    uploadService.uploadAll()
                    //new ImageUploadTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, imageDoc);
                } else {
                    AndroidUtils.toast(this@HistoryActivity2, "Sorry there is no Internet connection or you try to send existed file", Toast.LENGTH_LONG)
                }
            }
            if (swipeDir == ItemTouchHelper.LEFT) {
                val imageDoc = (viewHolder as HistoryViewHolder).imageDoc
                val highRes = highResLocator.locate(null, imageDoc?.name)
                val lowRes = lowResLocator.locate(null, imageDoc?.name)
                val icon = iconsLocator.locate(null, imageDoc?.name)
                val position = adapter.getItemsList().indexOf(imageDoc)
                imageDocService.delete(imageDoc)
                (adapter.getItemsList() as MutableList<ImageDoc>).remove(imageDoc)
                highRes.delete()
                lowRes.delete()
                icon.delete()
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, adapter.getItemsList().size)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_page_layuot)
        //setupToolbar()
        listView = findViewById(R.id.history_list) as RecyclerView
        listView.setHasFixedSize(true)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = HistoryViewAdapter(getImages())

        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(listView)
    }

/*    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null)
            (supportActionBar as ActionBar).setDisplayShowTitleEnabled(false)
    }*/


    internal inner class HistoryViewAdapter : RecyclerView.Adapter<HistoryViewHolder> {
        private val iconSizeInMm = 20
        private val dateFormat = SimpleDateFormat("d MMMM yyyy")
        private val timeFormat = SimpleDateFormat("HH:mm:ss")
        private val images: List<ImageDoc>

        constructor(images: List<ImageDoc>) {
            this.images = images
        }

        fun getItemsList(): List<ImageDoc> {
            return images
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
            return HistoryViewHolder(v)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val imageDoc = images[position]
            holder.date.text = dateFormat.format(images[position].creationDate)
            holder.time.text = timeFormat.format(images[position].creationDate)
            holder.statusIcon.setImageDrawable(getDrawableByType(imageDoc.status, true))
            setIconAsync(imageDoc.name, holder)
            //todo: check if necessary to set this
            holder.imageDoc = imageDoc
            holder.image.setOnClickListener { v -> onIconClickListener.onIconClick(imageDoc.name) }
        }

        private fun setIconAsync(name: String, holder: HistoryViewHolder) {
            val icon = iconsLocator.locate(null, name)
            val lowRes = lowResLocator.locate(null, name)
            if (icon.exists())
                holder.image.setImageDrawable(Drawable.createFromPath(icon.absolutePath))
            else if (lowRes.exists()) {
                val pixels = AndroidUtils.mmInPixels(this@HistoryActivity2, iconSizeInMm)
                ImageThumbnailCreateTask(pixels, pixels, icon, this@HistoryActivity2, { this.notifyDataSetChanged() }).execute(lowRes)
            }
        }

        private fun getDrawableByType(status: ImageDoc.Status, white: Boolean): Drawable {
            var iconId = if (white) R.drawable.check_status_not_sent_w else R.drawable.check_status_not_sent_b
            when (status) {
                ImageDoc.Status.EMPTY -> iconId = if (white) R.drawable.check_status_not_sent_w else R.drawable.check_status_not_sent_b
                ImageDoc.Status.NEW -> iconId = if (white) R.drawable.check_status_not_sent_w else R.drawable.check_status_not_sent_b
                ImageDoc.Status.OFFLINE -> iconId = if (white) R.drawable.check_status_offline_w else R.drawable.check_status_offline_b
                ImageDoc.Status.SEND -> iconId = if (white) R.drawable.check_status_sent_w else R.drawable.check_status_sent_b
                ImageDoc.Status.RECOGNIZED -> iconId = if (white) R.drawable.check_status_ident_w else R.drawable.check_status_ident_b
                ImageDoc.Status.UNRECOGNIZED -> iconId = if (white) R.drawable.check_status_unkn_w else R.drawable.check_status_unkn_b
            }
            return resources.getDrawable(iconId)
        }

        override fun getItemCount(): Int {
            return images.size
        }
    }

    private fun getImages(): List<ImageDoc> {
        val result = imageDocService.findAll()
        val iterator = result.iterator()
        while (iterator.hasNext())
            if (ImageDoc.Status.EMPTY == iterator.next().status)
                iterator.remove()
        Collections.sort(result) { lhs, rhs -> rhs.creationDate.compareTo(lhs.creationDate) }
        return result
    }

}