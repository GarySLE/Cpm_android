package com.ys.cpm.lib

import android.content.Context
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.FrameLayout
import android.provider.Settings
import java.util.*


/**
 * Created by Ys on 2017/8/16.
 */
abstract class AbsPlayController(context: Context) : FrameLayout(context) {

    companion object {

        /**
         * 播放位置触控
         */
        const val TOUCH_CONTROL_PLAY_POSITION = 1

        /**
         * 播放音量触控
         */
        const val TOUCH_CONTROL_PLAY_VOLUME = 2

        /**
         * 屏幕亮度触控
         */
        const val TOUCH_CONTROL_PLAY_BRIGHTNESS = 3

    }

    interface OnControllerHideListener {
        fun onHide()
    }

    private val THRESHOLD = 80

    var playMaster: AbsPlayMaster? = null

    private var progressUpdateTimer: Timer? = null
    private var progressUpdateTask: TimerTask? = null

    private var downX = 0f
    private var downY = 0f
    private var isChangePosition = false
    private var isChangeVolume = false
    private var isChangeBrightness = false
    private var downPosition = 0L
    private var downBrightness = 0
    private var downVolume = 0
    private var newPosition = 0L

    private val touchListener = OnTouchListener { view, event ->
        // 只有全屏的时候才能触控播放位置、亮度、声音
        if (this.playMaster == null || !this.playMaster!!.isFullScreen()) {
            return@OnTouchListener false
        }
        // 只有在播放、暂停、缓冲的时候能够触控改变播放位置、亮度和声音
        if (this.playMaster!!.isIdle() ||
                this.playMaster!!.isError() ||
                this.playMaster!!.isPreparing() ||
                this.playMaster!!.isPrepared() ||
                this.playMaster!!.isCompleted()) {
            hideTouchControlPanel()
            return@OnTouchListener false
        }
        val x = event.x
        val y = event.y
        when (event.action) {
        // todo 双击小窗口进入全屏模式
        // todo 双击不放左右移动切换上一个/下一个内容
        // todo 截图和宽高比例的切换手势
            MotionEvent.ACTION_DOWN -> {
                this.downX = x
                this.downY = y
                this.isChangePosition = false
                this.isChangeVolume = false
                this.isChangeBrightness = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - this.downX
                var deltaY = y - this.downY
                val absDeltaX = Math.abs(deltaX)
                val absDeltaY = Math.abs(deltaY)
                if (!this.isChangePosition &&
                        !this.isChangeVolume &&
                        !this.isChangeBrightness) {
                    if (absDeltaX >= THRESHOLD) {
                        cancelProgressUpdateSchedule()
                        this.isChangePosition = true
                        this.downPosition = this.playMaster!!.getCurrentPosition()
                    } else if (absDeltaY >= THRESHOLD) {
                        if (this.downX < width * 0.5f) {
                            // 左侧改变亮度
                            this.isChangeBrightness = true
                            val screenBrightness =
                                    CommonUtil.scanForActivity(context)?.window?.
                                            attributes?.screenBrightness ?:
                                            -1f
                            if (screenBrightness < 0) {
                                try {
                                    this.downBrightness =
                                            Settings.System.getInt(
                                                    context.contentResolver,
                                                    Settings.System.SCREEN_BRIGHTNESS
                                            )
                                } catch (e: Settings.SettingNotFoundException) {
                                    e.printStackTrace()
                                    this.isChangeBrightness = false
                                    LogUtil.e("Get current system brightness failed", e)
                                }
                            } else {
                                this.downBrightness = (screenBrightness * 255).toInt()
                            }
                            LogUtil.d("current system brightness: ${this.downBrightness}")
                        } else {
                            // 右侧改变声音
                            this.isChangeVolume = true
                            this.downVolume = this.playMaster!!.getVolume()
                        }
                    }
                }
                if (this.isChangePosition) {
                    val duration = this.playMaster!!.getDuration()
                    val toPosition =
                            (this.downPosition + duration * deltaX / width).toLong()
                    this.newPosition = Math.max(0, Math.min(duration, toPosition))
                    val newPositionProgress =
                            (100f * this.newPosition / duration).toInt()
                    showTouchControlPanel(
                            TOUCH_CONTROL_PLAY_POSITION,
                            newPositionProgress
                    )
                }
                if (this.isChangeVolume) {
                    deltaY = -deltaY
                    val maxVolume = this.playMaster!!.getMaxVolume()
                    val deltaVolume =
                            (maxVolume.toFloat() * deltaY * 3f / height).toInt()
                    var newVolume = this.downVolume + deltaVolume
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume))
                    this.playMaster!!.setVolume(newVolume)
                    val newVolumeProgress = (100f * newVolume / maxVolume).toInt()
                    showTouchControlPanel(TOUCH_CONTROL_PLAY_VOLUME, newVolumeProgress)
                }
                if (this.isChangeBrightness) {
                    deltaY = -deltaY
                    val deltaBrightness = 255f * deltaY * 3f / height
                    //这和声音有区别，必须自己过滤一下负值
                    var newBrightness = (this.downBrightness + deltaBrightness) / 255f
                    newBrightness = Math.max(0.01f, Math.min(newBrightness, 1f))
                    val activity = CommonUtil.scanForActivity(context)
                    val params = activity?.window?.attributes
                    params?.screenBrightness = newBrightness
                    activity?.window?.attributes = params
                    val newBrightnessProgress =
                            (100f * newBrightness).toInt()
                    showTouchControlPanel(
                            TOUCH_CONTROL_PLAY_BRIGHTNESS,
                            newBrightnessProgress
                    )
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                hideTouchControlPanel()
                if (this.isChangePosition) {
                    this.playMaster!!.seekTo(this.newPosition)
                    startProgressUpdateSchedule()
                    return@OnTouchListener true
                }
                if (this.isChangeBrightness) {
                    return@OnTouchListener true
                }
                if (this.isChangeVolume) {
                    return@OnTouchListener true
                }
            }
        }
        return@OnTouchListener false
    }

    var onControllerHideListener: OnControllerHideListener? = null

    init {
        this.setOnTouchListener(this.touchListener)
    }

    /**
     * 当播放器的播放状态发生变化
     *
     * @param playState 播放状态：
     *
     *  * [AbsPlayMaster.STATE_IDLE]
     *  * [AbsPlayMaster.STATE_PREPARING]
     *  * [AbsPlayMaster.STATE_PREPARED]
     *  * [AbsPlayMaster.STATE_PLAYING]
     *  * [AbsPlayMaster.STATE_PAUSED]
     *  * [AbsPlayMaster.STATE_BUFFERING_PLAYING]
     *  * [AbsPlayMaster.STATE_BUFFERING_PAUSED]
     *  * [AbsPlayMaster.STATE_ERROR]
     *  * [AbsPlayMaster.STATE_SEEKING]
     *  * [AbsPlayMaster.STATE_COMPLETED]
     *
     */
    abstract fun onPlayStateChanged(playState: Int)

    /**
     * 当播放器的播放模式发生变化
     *
     * @param playMode 播放器的模式：
     *
     *  * [AbsPlayMaster.MODE_COMMON]
     *  * [AbsPlayMaster.MODE_FULL_SCREEN]
     *  * [AbsPlayMaster.MODE_TINY_WINDOW]
     *
     */
    abstract fun onPlayModeChanged(playMode: Int)

    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    abstract fun reset()

    /**
     * 更新进度，包括进度条进度，展示的当前播放位置时长，总时长等。
     */
    protected abstract fun doProgressUpdate()

    /**
     * 开启更新进度的计时器。
     */
    protected fun startProgressUpdateSchedule() {
        cancelProgressUpdateSchedule()
        if (this.progressUpdateTimer == null) {
            this.progressUpdateTimer = Timer()
        }
        if (this.progressUpdateTask == null) {
            this.progressUpdateTask = object : TimerTask() {
                override fun run() {
                    this@AbsPlayController.post { doProgressUpdate() }
                }
            }
        }
        this.progressUpdateTimer!!.schedule(this.progressUpdateTask, 0, 200)
    }

    /**
     * 取消更新进度的计时器。
     */
    protected fun cancelProgressUpdateSchedule() {
        this.progressUpdateTimer?.cancel()
        this.progressUpdateTimer = null

        this.progressUpdateTask?.cancel()
        this.progressUpdateTask = null
    }

    /**
     * 显示触摸控制器
     *
     * @param what 触发的控制器类型
     * @param newProgress 新的进度
     */
    protected abstract fun showTouchControlPanel(what: Int, newProgress: Int)

    /**
     * 隐藏触摸控制器
     */
    protected abstract fun hideTouchControlPanel()

}