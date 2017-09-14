package com.ys.cpm.lib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.BatteryManager
import android.os.CountDownTimer
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.ys.cpm.lib.databinding.ChaosPlayControllerBinding
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Ys on 2017/8/16.
 */
open class ChaosPlayController(context: Context) : AbsPlayController(context) {

    private val binding: ChaosPlayControllerBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.chaos_play_controller,
            null,
            false
    )

    private var isShow: Boolean = false
    private var dismissCountDownTimer: CountDownTimer? = null
    // 是否注册了电池广播接收器
    private var isAlreadyRegisterBatteryReceiver: Boolean = false

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到[onPlayStateChanged]和[onPlayModeChanged]中处理.
     */
    private val clickListener = object : OnClickListener {
        override fun onClick(v: View?) {
            val playMaster = this@ChaosPlayController.playMaster ?: return

            val binding = this@ChaosPlayController.binding
            when (v) {
                binding.sizeChangeButton -> {
                    playMaster.setDisplayMode(playMaster.currentDisplayMode + 1)
                }
                binding.screenshotButton -> {
                    val bitmap = playMaster.getCurrentFrameBitmap()
                    CommonUtil.saveBitmapInExternalStorage(
                            context,
                            bitmap,
                            "cpm_${playMaster.getCurrentPosition()}_"
                                    + System.currentTimeMillis()
                    )
                }
                binding.backButton -> {
                    if (playMaster.isFullScreen() ||
                            playMaster.isTinyWindow()) {
                        playMaster.returnCommon()
                    }
                }
                binding.playPauseButton -> {
                    if (playMaster.isIdle()) {
                        playMaster.play()
                    } else if (playMaster.isPlaying() ||
                            playMaster.isBufferingPlaying()) {
                        playMaster.pause()
                    } else if (playMaster.isPaused() ||
                            playMaster.isBufferingPaused()) {
                        playMaster.replay()
                    }
                    if (!isShow) {
                        binding.playPauseButton.visibility = View.GONE
                    }
                }
                binding.fullScreenButton -> {
                    if (playMaster.isCommon() ||
                            playMaster.isTinyWindow()) {
                        playMaster.enterFullScreen()
                    } else if (playMaster.isFullScreen()) {
                        playMaster.returnCommon()
                    }
                }
                binding.retry -> {
                    playMaster.replay()
                }
                binding.replay -> {
                    binding.retry.performClick()
                }
                this@ChaosPlayController -> {
                    if (playMaster.isPlaying()
                            || playMaster.isPaused()
                            || playMaster.isBufferingPlaying()
                            || playMaster.isBufferingPaused()) {
                        setControllerVisibility(if (isShow) View.GONE else View.VISIBLE)
                    }
                }
            }
        }
    }

    /**
     * seekBar监听
     */
    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            val playMaster = this@ChaosPlayController.playMaster ?: return
            if (playMaster.isBufferingPaused() || playMaster.isPaused()) {
                playMaster.replay()
            }
            val position = (playMaster.getDuration() * seekBar.progress / 100f).toLong()
            playMaster.seekTo(position)
            startAutoDismissTask()
        }
    }

    /**
     * 全屏时显示电池状态即注册电量变化广播接收器
     */
    private val batterReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN)
            val battery = this@ChaosPlayController.binding.battery
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> // 充电中
                    battery.setImageResource(R.drawable.battery_charging)
                BatteryManager.BATTERY_STATUS_FULL -> // 充电完成
                    battery.setImageResource(R.drawable.battery_full)
                else -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                    val percentage = (level.toFloat() / scale * 100).toInt()
                    when {
                        percentage <= 10 ->
                            battery.setImageResource(R.drawable.battery_10)
                        percentage <= 20 ->
                            battery.setImageResource(R.drawable.battery_20)
                        percentage <= 50 ->
                            battery.setImageResource(R.drawable.battery_50)
                        percentage <= 80 ->
                            battery.setImageResource(R.drawable.battery_80)
                        percentage <= 100 ->
                            battery.setImageResource(R.drawable.battery_100)
                    }
                }
            }
        }
    }
    private val timeTextFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        this.addView(this.binding.root)

        this.binding.backButton.setOnClickListener(this.clickListener)
        this.binding.playPauseButton.setOnClickListener(this.clickListener)
        this.binding.fullScreenButton.setOnClickListener(this.clickListener)
        this.binding.retry.setOnClickListener(this.clickListener)
        this.binding.replay.setOnClickListener(this.clickListener)
        this.binding.screenshotButton.setOnClickListener(this.clickListener)
        this.binding.sizeChangeButton.setOnClickListener(this.clickListener)
        this.binding.seekBar.setOnSeekBarChangeListener(this.seekBarChangeListener)
        this.setOnClickListener(this.clickListener)
    }

    fun setTitle(title: String) {
        this.binding.titleText.text = title
    }

    fun getBackgroundImageView(): ImageView = this.binding.previewImage

    fun setPlaceHolder(@DrawableRes resId: Int) {
        this.binding.previewImage.setImageResource(resId)
    }

    override fun onPlayStateChanged(playState: Int) {
        when (playState) {
            AbsPlayMaster.STATE_IDLE -> {
            }
            AbsPlayMaster.STATE_PREPARING -> {
                this.binding.previewImage.visibility = View.GONE
                this.binding.loadingProgressBar.visibility = View.VISIBLE
                this.binding.error.visibility = View.GONE
                this.binding.completed.visibility = View.GONE
                this.binding.playPauseButton.visibility = View.GONE
                setControllerVisibility(View.GONE)
            }
            AbsPlayMaster.STATE_PREPARED -> startProgressUpdateSchedule()
            AbsPlayMaster.STATE_PLAYING -> {
                this.binding.loadingProgressBar.visibility = View.GONE
                this.binding.playPauseIcon.
                        setImageResource(R.drawable.pause)
                startAutoDismissTask()
            }
            AbsPlayMaster.STATE_PAUSED -> {
                this.binding.loadingProgressBar.visibility = View.GONE
                this.binding.playPauseIcon.
                        setImageResource(R.drawable.play)
                cancelAutoDismissTask()
            }
            AbsPlayMaster.STATE_BUFFERING_PLAYING -> {
                this.binding.loadingProgressBar.visibility = View.VISIBLE
                this.binding.playPauseIcon.
                        setImageResource(R.drawable.pause)
                startAutoDismissTask()
            }
            AbsPlayMaster.STATE_BUFFERING_PAUSED -> {
                this.binding.loadingProgressBar.visibility = View.VISIBLE
                this.binding.playPauseIcon.
                        setImageResource(R.drawable.play)
                cancelAutoDismissTask()
            }
            AbsPlayMaster.STATE_ERROR -> {
                cancelProgressUpdateSchedule()
                setControllerVisibility(View.GONE)
                this.binding.backButton.visibility = View.VISIBLE
                this.binding.titleText.visibility = View.VISIBLE
                this.binding.error.visibility = View.VISIBLE
            }
            AbsPlayMaster.STATE_COMPLETED -> {
                cancelProgressUpdateSchedule()
                setControllerVisibility(View.GONE)
                this.binding.previewImage.visibility = View.VISIBLE
                this.binding.completed.visibility = View.VISIBLE
            }
        }
    }

    override fun onPlayModeChanged(playMode: Int) {
        when (playMode) {
            AbsPlayMaster.MODE_COMMON -> {
                if (isShow) {
                    this.binding.backButton.visibility = View.GONE
                    this.binding.batteryAndTime.visibility = View.GONE
                }
                this.binding.fullScreenIcon.
                        setImageResource(R.drawable.full_screen_enter)
                if (this.isAlreadyRegisterBatteryReceiver) {
                    context.unregisterReceiver(this.batterReceiver)
                    this.isAlreadyRegisterBatteryReceiver = false
                }
            }
            AbsPlayMaster.MODE_FULL_SCREEN -> {
                if (isShow) {
                    this.binding.backButton.visibility = View.VISIBLE
                    this.binding.batteryAndTime.visibility = View.VISIBLE
                }
                this.binding.fullScreenIcon.
                        setImageResource(R.drawable.full_screen_exit)
                if (!this.isAlreadyRegisterBatteryReceiver) {
                    context.registerReceiver(this.batterReceiver,
                            IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    this.isAlreadyRegisterBatteryReceiver = true
                }
            }
            AbsPlayMaster.MODE_TINY_WINDOW -> {
                if (isShow) {
                    this.binding.backButton.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun reset() {
        this.isShow = false
        cancelProgressUpdateSchedule()
        cancelAutoDismissTask()
        this.binding.seekBar.progress = 0
        this.binding.seekBar.secondaryProgress = 0

        setControllerVisibility(View.GONE)
        this.binding.playPauseButton.visibility = View.VISIBLE
        this.binding.previewImage.visibility = View.VISIBLE

        this.binding.fullScreenIcon.
                setImageResource(R.drawable.full_screen_enter)

        this.binding.loadingProgressBar.visibility = View.GONE
        this.binding.error.visibility = View.GONE
        this.binding.completed.visibility = View.GONE
    }

    /**
     * Controller界面的显示和隐藏
     *
     * @param visibility [View]的显示标签
     */
    private fun setControllerVisibility(visibility: Int) {
        this.binding.titleWrapper.visibility = visibility
        this.binding.seekBar.visibility = visibility
        this.binding.playPauseButton.visibility = visibility
        this.binding.fullScreenButton.visibility = visibility
        this.binding.screenshotButton.visibility = visibility
        this.binding.sizeChangeButton.visibility = visibility
        this.binding.lengthWrapper.visibility = visibility
        val isFullScreen = this.playMaster != null && this.playMaster!!.isFullScreen()
        this.binding.backButton.visibility =
                if (isFullScreen)
                    visibility
                else
                    View.GONE
        this.binding.batteryAndTime.visibility =
                if (isFullScreen)
                    visibility
                else
                    View.GONE
        this.isShow = visibility == View.VISIBLE
        if (this.isShow) {
            if (this.playMaster != null &&
                    !this.playMaster!!.isPaused() &&
                    !this.playMaster!!.isBufferingPaused()) {
                startAutoDismissTask()
            }
        } else {
            cancelAutoDismissTask()
            this.onControllerHideListener?.onHide()
        }
    }

    /**
     * 开启Controller自动消失的定时任务
     */
    private fun startAutoDismissTask() {
        cancelAutoDismissTask()
        if (this.dismissCountDownTimer == null) {
            this.dismissCountDownTimer = object : CountDownTimer(5000, 5000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    setControllerVisibility(View.GONE)
                }
            }
        }
        this.dismissCountDownTimer!!.start()
    }

    /**
     * 取消Controller自动消失的定时任务
     */
    private fun cancelAutoDismissTask() {
        this.dismissCountDownTimer?.cancel()
    }

    override fun doProgressUpdate() {
        if (this.playMaster == null) {
            return
        }
        val position = this.playMaster!!.getCurrentPosition()
        val duration = this.playMaster!!.getDuration()
        val bufferPercent = this.playMaster!!.bufferPercent
        this.binding.seekBar.secondaryProgress = bufferPercent
        val progress = (100f * position / duration).toInt()
        this.binding.seekBar.progress = progress
        // 显示剩余播放时长
        doRemainingTimeUpdate(duration - position)
        // 更新时间
        this.binding.time.text = this.timeTextFormat.format(Date())
    }

    private fun doRemainingTimeUpdate(position: Long) {
        this.binding.length.text = CommonUtil.formatTime(position)
    }

    override fun showTouchControlPanel(what: Int, newProgress: Int) {
        when (what) {
            TOUCH_CONTROL_PLAY_POSITION -> {
                val duration = this.playMaster!!.getDuration()
                this.binding.changePosition.visibility = View.VISIBLE
                val newPosition = (duration * newProgress / 100f).toLong()
                this.binding.changePositionCurrent.text =
                        CommonUtil.formatTime(newPosition)
                this.binding.changePositionProgress.progress = newProgress
                this.binding.seekBar.progress = newProgress
                // 显示剩余播放时长
                doRemainingTimeUpdate(duration - newPosition)
            }
            TOUCH_CONTROL_PLAY_VOLUME -> {
                this.binding.changeVolume.visibility = View.VISIBLE
                this.binding.changeVolumeProgress.progress = newProgress
            }
            TOUCH_CONTROL_PLAY_BRIGHTNESS -> {
                this.binding.changeBrightness.visibility = View.VISIBLE
                this.binding.changeBrightnessProgress.progress = newProgress
            }
        }
    }

    override fun hideTouchControlPanel() {
        this.binding.changePosition.visibility = View.GONE
        this.binding.changeVolume.visibility = View.GONE
        this.binding.changeBrightness.visibility = View.GONE
    }

}