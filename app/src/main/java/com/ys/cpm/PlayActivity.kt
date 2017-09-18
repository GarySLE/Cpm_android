package com.ys.cpm

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.yanzhenjie.permission.*
import com.ys.cpm.data.MediaFile
import com.ys.cpm.databinding.ActivityPlayBinding
import com.ys.cpm.lib.ChaosPlayController
import com.ys.cpm.lib.ChaosPlayMaster

class PlayActivity : AppCompatActivity() {

    companion object {
        @JvmField
        val KEY_MEDIA_FILE = "video"
        @JvmField
        val KEY_DIR_TITLE = "dir_title"
    }

    private lateinit var binding: ActivityPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_play)

        val media = intent.getParcelableExtra<MediaFile>(KEY_MEDIA_FILE)

        val controller = ChaosPlayController(this)
        controller.setTitle(media.title)
        controller.setPlaceHolder(R.drawable.gakki)
        this.binding.playView.setController(controller)
        this.binding.playView.setup(media.path, null)

        AndPermission.with(this)
                .requestCode(100)
                .permission(Permission.STORAGE)
                .rationale(object : RationaleListener {
                    override fun showRequestPermissionRationale(requestCode: Int, rationale: Rationale?) {
                        AndPermission.rationaleDialog(this@PlayActivity, rationale).show()
                    }
                })
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                    }
                })
                .start()
    }

    override fun onPause() {
        super.onPause()
        if (this.binding.playView.isPlaying() ||
                this.binding.playView.isBufferingPlaying()) {
            this.binding.playView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this.binding.playView.isPaused() ||
                this.binding.playView.isBufferingPaused()) {
            this.binding.playView.replay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.binding.playView.reset()
    }

    override fun onBackPressed() {
        if (this.binding.playView.isFullScreen()) {
            this.binding.playView.returnCommon()
        } else {
            super.onBackPressed()
        }
    }
}
