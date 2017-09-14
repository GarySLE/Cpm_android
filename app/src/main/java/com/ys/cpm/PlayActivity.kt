package com.ys.cpm

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.yanzhenjie.permission.*
import com.ys.cpm.databinding.ActivityPlayBinding
import com.ys.cpm.lib.ChaosPlayController

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_play)

        val controller = ChaosPlayController(this)
        controller.setTitle("新垣结衣真可爱")
        controller.setPlaceHolder(R.drawable.gakki)
        this.binding.playView.setController(controller)
        this.binding.playView.setup(
                "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4",
                null
        )
//        this.binding.playView.play()

        AndPermission.with(this)
                .permission(Permission.STORAGE)
                .requestCode(100)
                .rationale(object : RationaleListener {
                    override fun showRequestPermissionRationale(requestCode: Int, rationale: Rationale?) {
                        AndPermission.rationaleDialog(this@PlayActivity, rationale).show()
                    }
                })
                .callback(object : PermissionListener{
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
