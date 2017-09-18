package com.ys.cpm.vm

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.view.View
import com.yanzhenjie.permission.*
import com.ys.cpm.CPMApp
import com.ys.cpm.MainActivity
import com.ys.cpm.PlaylistAdapter
import com.ys.cpm.R
import com.ys.cpm.databinding.ActivityMainBinding
import com.ys.cpm.db.SQLiteOperator
import com.ys.cpm.db.manager.VideoManager
import com.ys.cpm.utils.UriUtil
import org.jetbrains.anko.toast

/**
 * Created by Ys on 2017/9/17.
 */
class MainViewModel(
        activity: Activity,
        binding: ActivityMainBinding
) : RecyclerViewModel(activity) {

    val viewBinding = binding
    var dbOperator = SQLiteOperator(CPMApp.instance.dbHelper)
    var mediaAdapter: PlaylistAdapter

    init {
        val files =
                VideoManager.getVideoFiles(this.dbOperator, MainActivity.DEFAULT_PLAYLIST)
        this.mediaAdapter =
                PlaylistAdapter(activity, this.dbOperator, MainActivity.DEFAULT_PLAYLIST)
        this.mediaAdapter.addAll(files)
    }

    fun initView() {
        this.viewBinding.content.recycler.adapter = this.mediaAdapter

        this.viewBinding.fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                AndPermission.with(context)
                        .requestCode(100)
                        .permission(Permission.STORAGE)
                        .rationale(object : RationaleListener {
                            override fun showRequestPermissionRationale(requestCode: Int, rationale: Rationale?) {
                                AndPermission.rationaleDialog(context, rationale).show()
                            }
                        })
                        .callback(object : PermissionListener {
                            override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                                gotoSelectVideo()
                            }

                            override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                            }
                        })
                        .start()
            }
        })
    }

    fun addMediaFile(data: Intent?) {
        val uri = data?.data
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.moveToFirst()
            val path = UriUtil.getPath(context, uri)
            for ((itemPath) in this.mediaAdapter.getList()) {
                if (itemPath == path) {
                    context.toast(R.string.file_is_exist)
                    return
                }
            }
            val title = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
            )
            val time: Long
            // 获取媒体元数据
            var mmr: MediaMetadataRetriever? = null
            try {
                mmr = MediaMetadataRetriever()
                mmr.setDataSource(path)
                val timeString = mmr.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                time = timeString.toLong()
            } finally {
                mmr?.release()
            }
            val itemVideo = VideoManager.addVideoFile(
                    this.dbOperator,
                    path,
                    title,
                    time,
                    MainActivity.DEFAULT_PLAYLIST
            )
            this.mediaAdapter.add(itemVideo)
        } finally {
            cursor?.close()
        }
    }

    private fun gotoSelectVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        context.startActivityForResult(intent, MainActivity.REQUEST_MEDIA_FILE)
    }

}