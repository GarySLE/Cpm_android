package com.ys.cpm

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.ys.cpm.db.manager.VideoManager
import com.ys.cpm.data.MediaFile
import com.ys.cpm.db.SQLiteOperator
import com.ys.cpm.utils.ImageUtil
import com.ys.cpm.utils.VideoUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ys on 2017/6/13.
 * Playlist Adapter
 */
class PlaylistAdapter(context: Context, operator: SQLiteOperator, dirTitle: String)
    : BaseRvAdapter<MediaFile, PlaylistAdapter.MediaViewHolder>(context) {

    val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
    var mOperator: SQLiteOperator = operator
    var mDirTitle: String = dirTitle

    override fun getItemCount(): Int = mList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.cell_video, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder?, position: Int) {
        val media = mList[position]
        if (!media.path.startsWith("http")) {
            if (!TextUtils.isEmpty(media.imagePath)) {
                Picasso.with(mCtx)
                        .load(File(media.imagePath))
                        .into(holder?.mediaImage)
            } else {
                val path = media.path
                holder?.mediaImage?.tag = path
                doAsync {
                    if (path != holder?.mediaImage?.tag) {
                        return@doAsync
                    }
                    val thumbnail =
                            if (path.startsWith("android.resource"))
                                VideoUtil.getThumbnail(mCtx, path)
                            else VideoUtil.getVideoThumbnail(path, 512, 384,
                                    MediaStore.Images.Thumbnails.MINI_KIND)
                    val name = media.title
                    val imageFile = ImageUtil.saveFile(mCtx, thumbnail, name + "_thumb")
                    val thumbPath = imageFile.path
                    uiThread {
                        if (path == holder.mediaImage.tag) {
                            holder.mediaImage.setImageBitmap(thumbnail)
                        }
                        VideoManager.updateVideoFile(
                                mOperator, media.title, mDirTitle, imagePath = thumbPath)
                        media.imagePath = thumbPath
                    }
                }
            }
            holder?.timeText?.text = sdf.format(Date(media.time))
        } else {
            holder?.timeText?.visibility = View.GONE
        }
        holder?.titleText?.text = media.title
        holder?.timeText?.text = sdf.format(Date(media.time))
        holder?.itemView?.setOnClickListener {
            mCtx.startActivity(Intent(mCtx, PlayActivity::class.java)
                    .putExtra(PlayActivity.KEY_MEDIA_FILE, media)
                    .putExtra(PlayActivity.KEY_DIR_TITLE, mDirTitle))
        }
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediaImage: ImageView = itemView.findViewById(R.id.mediaImage)
        var titleText: TextView = itemView.findViewById(R.id.titleText)
        var timeText: TextView = itemView.findViewById(R.id.timeText)
    }
}