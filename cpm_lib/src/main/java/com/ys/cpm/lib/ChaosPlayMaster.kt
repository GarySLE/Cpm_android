package com.ys.cpm.lib

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import tv.danmaku.ijk.media.player.AndroidMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException


/**
 * Created by Ys on 2017/8/16.
 */
open class ChaosPlayMaster : AbsPlayMaster, TextureView.SurfaceTextureListener,
        AbsPlayController.OnControllerHideListener {

    companion object {
        /**
         * IjkPlayer
         */
        const val TYPE_IJK = 111
        /**
         * MediaPlayer
         */
        const val TYPE_NATIVE = 222
    }

    /**
     * 播放组件初始化状态
     */
    var isComponentInit = false

    override var currentDisplayMode: Int = PlayTexture.DISPLAY_MODE_SMART_PARENT

    /**
     * 设置播放器类型 IjkMediaPlayer or MediaPlayer.
     */
    open var playerType = TYPE_IJK
    /**
     * 目标状态, 以便[play], [seekTo]等延迟耗时操作完成后切换状态
     */
    open var targetState = STATE_IDLE

    open lateinit var playContainer: FrameLayout
    open var playTexture: PlayTexture? = null
    open var playController: AbsPlayController? = null
    open var surfaceTexture: SurfaceTexture? = null
    open var surface: Surface? = null
    open var audioManager: AudioManager? = null
    open var mediaPlayer: IMediaPlayer? = null
    open var positionOfSeek: Long = 0

    open var playCallback: PlayCallbackAdapter? = null

    open var viewGroupOfModeCommon: ViewGroup? = null
    open var viewOfModeChange: View? = null

    private val preparedListener =
            IMediaPlayer.OnPreparedListener { mp ->
                this.currentState = STATE_PREPARED
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("prepared")
                mp.start()
                // 跳到指定位置播放
                if (this.positionOfSeek != 0L) {
                    mp.seekTo(this.positionOfSeek)
                } else if (this.isContinueFromLastPlay) {
                    // 从上次的保存位置播放
                    val savedPlayPosition =
                            CommonUtil.getSavedPlayPosition(context, this.url)
                    mp.seekTo(savedPlayPosition)
                }
                this.playCallback?.onPrepared(mp)
            }

    private val videoSizeChangedListener =
            IMediaPlayer.OnVideoSizeChangedListener { mp, width, height, sar_num, sar_den ->
                this.playTexture?.fitSize(width, height)
                LogUtil.d("videoSizeChanged —> width：$width， height：$height")
                this.playCallback?.onVideoSizeChanged(mp, width, height, sar_num, sar_den)
            }

    private val completionListener =
            IMediaPlayer.OnCompletionListener { mp ->
                this.currentState = STATE_COMPLETED
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("completion")
                // 清除屏幕常亮
                this.playContainer.keepScreenOn = false
                this.playCallback?.onCompletion(mp)
            }

    private val errorListener =
            IMediaPlayer.OnErrorListener { mp, what, extra ->
                this.currentState = STATE_ERROR
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("error —> what：" + what)
                this.playCallback?.onError(mp, what, extra)
                false
            }

    private val infoListener =
            IMediaPlayer.OnInfoListener { mp, what, extra ->
                when (what) {
                    IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        // 播放器开始渲染
                        this.currentState = STATE_PLAYING
                        this.playController?.onPlayStateChanged(this.currentState)
                        LogUtil.d("state -> STATE_PLAYING")
                        LogUtil.i("info —> MEDIA_INFO_VIDEO_RENDERING_START")
                    }
                    IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        // MediaPlayer暂时不播放，缓冲数据
                        if (this.currentState == STATE_PAUSED ||
                                this.currentState == STATE_BUFFERING_PAUSED) {
                            this.currentState = STATE_BUFFERING_PAUSED
                            LogUtil.d("state -> STATE_BUFFERING_PAUSED")
                        } else {
                            this.currentState = STATE_BUFFERING_PLAYING
                            LogUtil.d("state -> STATE_BUFFERING_PLAYING")
                        }
                        this.playController?.onPlayStateChanged(this.currentState)
                        LogUtil.i("info —> MEDIA_INFO_BUFFERING_START")
                    }
                    IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        // 缓冲结束，MediaPlayer恢复播放/暂停
                        if (this.currentState == STATE_BUFFERING_PLAYING) {
                            this.currentState = STATE_PLAYING
                            LogUtil.d("state -> STATE_PLAYING")
                        } else if (this.currentState == STATE_BUFFERING_PAUSED) {
                            this.currentState = STATE_PAUSED
                            LogUtil.d("state -> STATE_PAUSED")
                        }
                        this.playController?.onPlayStateChanged(this.currentState)
                        LogUtil.i("info —> MEDIA_INFO_BUFFERING_END")
                    }
                    IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                        // 旋转extra度
                        this.playTexture?.rotation = extra.toFloat()
                        LogUtil.d("旋转角度 -> $extra")
                    }
                    else -> {
                        LogUtil.d("info —> what：" + what)
                    }
                }
                this.playCallback?.onInfo(mp, what, extra)
                true
            }

    private val bufferingUpdateListener =
            IMediaPlayer.OnBufferingUpdateListener { mp, percent ->
                this.bufferPercent = percent
                LogUtil.d("bufferingUpdate -> $percent")
                this.playCallback?.onBufferingUpdate(mp, percent)
            }

    private val seekCompleteListener =
            IMediaPlayer.OnSeekCompleteListener { mp: IMediaPlayer ->
                this.currentState = this.targetState
                this.playCallback?.onSeekComplete(mp)
            }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initContainer(context)
    }

    private fun initContainer(context: Context) {
        this.playContainer = FrameLayout(context)
        this.playContainer.setBackgroundColor(Color.BLACK)
        val params =
                FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        this.addView(this.playContainer, params)
    }

    private fun initComponent() {
        initAudioManager()
        initMediaPlayer()
        initTextureView()
        addTextureView()
    }

    private fun initAudioManager() {
        if (this.audioManager == null) {
            this.audioManager =
                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            this.audioManager!!.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun initMediaPlayer() {
        if (this.mediaPlayer == null) {
            this.mediaPlayer =
                    when (this.playerType) {
                        TYPE_IJK -> IjkMediaPlayer()
                        else -> AndroidMediaPlayer()
                    }
        }
    }

    private fun initTextureView() {
        if (this.playTexture == null) {
            this.playTexture = PlayTexture(context)
            this.playTexture!!.displayMode = currentDisplayMode
            this.playTexture!!.surfaceTextureListener = this
        }
    }

    private fun addTextureView() {
        this.playContainer.removeView(this.playTexture)
        val params =
                FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                )
        this.playContainer.addView(this.playTexture, 0, params)
    }

    fun setController(controller: AbsPlayController?) {
        this.playContainer.removeView(this.playController)
        this.playController = controller
        if (this.playController != null) {
            this.playController!!.playMaster = this
            this.playController!!.onControllerHideListener = this
            this.playController!!.reset()
            val params =
                    FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            this.playContainer.addView(this.playController, params)
        }
    }

    /**
     * 设置播放速度，目前只有IjkMediaPlayer有效果，原生MediaPlayer暂不支持
     *
     * @param speed 播放速度
     */
    fun setSpeed(speed: Float) {
        if (this.mediaPlayer is IjkMediaPlayer) {
            (this.mediaPlayer as IjkMediaPlayer).setSpeed(speed)
        } else {
            LogUtil.d("IjkMediaPlayer才能设置播放速度")
        }
    }

    override fun play() {
        this.targetState = STATE_PLAYING
        if (this.currentState == STATE_IDLE && !this.isComponentInit) {
//            PlayManager.setCurrentPlayMaster(this)
            initComponent()
        } else if (this.isComponentInit && !TextUtils.isEmpty(this.url)) {
            openMediaPlayer()
        } else {
            LogUtil.d("重复调用play()无效.")
        }
    }

    override fun play(position: Long) {
        this.positionOfSeek = position
        play()
    }

    override fun replay() {
        when (this.currentState) {
            STATE_IDLE -> {
                play()
            }
            STATE_PAUSED -> {
                this.mediaPlayer?.start()
                this.currentState = STATE_PLAYING
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("state -> STATE_PLAYING")
            }
            STATE_BUFFERING_PAUSED -> {
                this.mediaPlayer?.start()
                this.currentState = STATE_BUFFERING_PLAYING
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("state -> STATE_BUFFERING_PLAYING")
            }
            STATE_COMPLETED, STATE_ERROR -> {
                openMediaPlayer()
            }
            else -> {
                LogUtil.d("currentState == ${this.currentState}时调用replay()无效.")
            }
        }
    }

    override fun pause() {
        when (this.currentState) {
            STATE_PLAYING -> {
                this.mediaPlayer?.pause()
                this.currentState = STATE_PAUSED
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("state -> STATE_PAUSED")
            }
            STATE_BUFFERING_PLAYING -> {
                this.mediaPlayer?.pause()
                this.currentState = STATE_BUFFERING_PAUSED
                this.playController?.onPlayStateChanged(this.currentState)
                LogUtil.d("state -> STATE_BUFFERING_PAUSED")
            }
            else -> {
                LogUtil.d("currentState == ${this.currentState}时调用pause()无效.")
            }
        }
    }

    override fun seekTo(position: Long) {
        if (this.mediaPlayer == null) {
            LogUtil.d("mediaPlayer is null")
            return
        }
        this.targetState = this.currentState
        this.currentState = STATE_SEEKING
        this.mediaPlayer!!.seekTo(position)
    }

    override fun onDisplayModeChange(mode: Int): Boolean {
        val playTexture = this.playTexture ?: return false
        val firstMode = PlayTexture.DISPLAY_MODE_SMART_PARENT
        val lastMode = PlayTexture.DISPLAY_MODE_4_3 + 1
        val isExistMode = mode in firstMode..lastMode
        return if (isExistMode) {
            val finalMode = if (mode == lastMode) firstMode else mode
            this.currentDisplayMode = finalMode
            playTexture.displayMode = finalMode
            true
        } else {
            false
        }
    }

    override fun setVolume(volume: Int) {
        if (this.audioManager == null) {
            LogUtil.d("audioManager is null")
        }
        this.audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    override fun getMaxVolume(): Int =
            this.audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0

    override fun getVolume(): Int =
            this.audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0

    override fun getCurrentFrameBitmap(): Bitmap? = this.playTexture?.bitmap

    override fun getDuration(): Long = this.mediaPlayer?.duration ?: 0

    override fun getCurrentPosition(): Long = this.mediaPlayer?.currentPosition ?: 0

    /**
     * 获取播放速度
     *
     * @param speed 播放速度
     * @return 播放速度
     */
    fun getSpeed(speed: Float): Float =
            if (this.mediaPlayer is IjkMediaPlayer)
                (this.mediaPlayer as IjkMediaPlayer).getSpeed(speed)
            else 0F

    /**
     * 获取网络加载速度
     *
     * @return 网络加载速度
     */
    fun getTcpSpeed(): Long =
            if (this.mediaPlayer is IjkMediaPlayer)
                (this.mediaPlayer as IjkMediaPlayer).tcpSpeed
            else 0

    private fun openMediaPlayer() {
        if (this.mediaPlayer == null) {
            LogUtil.d("mediaPlayer还没初始化, 无法进行播放准备")
            return
        }
        // 屏幕常量
        this.playContainer.keepScreenOn = true

        this.mediaPlayer!!.reset()
        this.mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        // 设置监听
        this.mediaPlayer!!.setOnPreparedListener(this.preparedListener)
        this.mediaPlayer!!.setOnVideoSizeChangedListener(this.videoSizeChangedListener)
        this.mediaPlayer!!.setOnCompletionListener(this.completionListener)
        this.mediaPlayer!!.setOnErrorListener(this.errorListener)
        this.mediaPlayer!!.setOnInfoListener(this.infoListener)
        this.mediaPlayer!!.setOnBufferingUpdateListener(this.bufferingUpdateListener)
        this.mediaPlayer!!.setOnSeekCompleteListener(this.seekCompleteListener)
        // 设置dataSource
        try {
            this.mediaPlayer!!.setDataSource(context, Uri.parse(this.url), this.headers)
            if (this.surface == null) {
                this.surface = Surface(this.surfaceTexture)
            }
            this.mediaPlayer!!.setSurface(this.surface)
            this.mediaPlayer!!.prepareAsync()
            this.currentState = STATE_PREPARING
            this.playController?.onPlayStateChanged(this.currentState)
            LogUtil.d("state -> STATE_PREPARING")
        } catch (e: IOException) {
            e.printStackTrace()
            LogUtil.e("打开播放器发生错误", e)
        }
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture,
                                           width: Int,
                                           height: Int
    ) {
        if (this.surfaceTexture == null) {
            this.surfaceTexture = surfaceTexture
            if (!TextUtils.isEmpty(this.url) &&
                    this.targetState == STATE_PLAYING) {
                openMediaPlayer()
            }
        } else {
            this.playTexture?.surfaceTexture = this.surfaceTexture
        }
        this.isComponentInit = true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture,
                                             width: Int,
                                             height: Int
    ) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean =
            this.surfaceTexture == null

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onHide() {
        if (isFullScreen()) {
            CommonUtil.scanForActivity(context)?.window?.decorView?.
                    systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    /**
     * 将Container(内部包含TextureView和Controller)从当前容器中移除, 添加到顶层Content中.
     * 切换横屏时需要在manifest的activity标签下添加
     * android:configChanges="orientation|keyboardHidden|screenSize"配置
     * 避免Activity重新走生命周期, 否则切换全屏模式会导致reset()被调用
     */
    override fun enterFullScreen() {
        if (this.currentMode == MODE_FULL_SCREEN) return

        // 隐藏ActionBar、状态栏，并横屏
        CommonUtil.hideActionBar(context)
        val activity = CommonUtil.scanForActivity(context)
        activity?.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val contentView = activity?.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val container = this.viewOfModeChange ?: this.playContainer
        val parent = this.viewGroupOfModeCommon ?: this
        if (this.currentMode == MODE_TINY_WINDOW) {
            contentView?.removeView(container)
        } else {
            parent.removeView(container)
        }
        val params =
                FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        contentView?.addView(container, params)

        this.currentMode = MODE_FULL_SCREEN
        this.playController?.onPlayModeChanged(this.currentMode)
        LogUtil.d("mode -> MODE_FULL_SCREEN")
    }

    /**
     * 进入小窗口播放，小窗口播放的实现原理与全屏播放类似。
     */
    override fun enterTinyWindow() {
        if (this.currentMode == MODE_TINY_WINDOW) return

        val contentView = CommonUtil.scanForActivity(context)?.
                findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val container = this.viewOfModeChange ?: this.playContainer
        val parent = this.viewGroupOfModeCommon ?: this
        if (this.currentMode == MODE_FULL_SCREEN) {
            contentView?.removeView(container)
        } else {
            parent.removeView(container)
        }
        // 小窗口的宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp。
        val screenWidth = CommonUtil.getScreenWidth(context)
        val params =
                FrameLayout.LayoutParams(
                        (screenWidth * 0.6f).toInt(),
                        (screenWidth * 0.6f * 9 / 16).toInt()
                )
        params.gravity = Gravity.BOTTOM or Gravity.END
        params.rightMargin = CommonUtil.dp2px(context, 8f)
        params.bottomMargin = CommonUtil.dp2px(context, 8f)
        contentView?.addView(this.playContainer, params)

        this.currentMode = MODE_TINY_WINDOW
        this.playController?.onPlayModeChanged(this.currentMode)
        LogUtil.d("mode -> MODE_TINY_WINDOW")
    }

    /**
     * 退回通常模式
     * 切换竖屏时需要在manifest的activity标签下添加
     * android:configChanges="orientation|keyboardHidden|screenSize"配置
     * 避免Activity重新走生命周期, 否则切换全屏模式会导致reset()被调用
     *
     * @return true 退回通常模式.
     */
    override fun returnCommon(): Boolean {
        if (this.currentMode != MODE_FULL_SCREEN &&
                this.currentMode != MODE_TINY_WINDOW) {
            return false
        }
        val activity = CommonUtil.scanForActivity(context)
        if (this.currentMode == MODE_FULL_SCREEN) {
            CommonUtil.showActionBar(context)
            activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_VISIBLE
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val contentView = activity?.findViewById<ViewGroup>(android.R.id.content)
        val container = this.viewOfModeChange ?: this.playContainer
        val parent = this.viewGroupOfModeCommon ?: this
        contentView?.removeView(container)
        val params =
                FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        parent.addView(container, params)

        this.currentMode = MODE_COMMON
        this.playController?.onPlayModeChanged(this.currentMode)
        LogUtil.d("mode -> MODE_COMMON")
        return true
    }

    override fun release() {
        this.audioManager?.abandonAudioFocus(null)
        this.audioManager = null

        this.mediaPlayer?.stop()
        this.mediaPlayer?.release()
        this.mediaPlayer = null

        this.playContainer.removeView(this.playTexture)

        this.surface?.release()
        this.surface = null

        this.surfaceTexture?.release()
        this.surfaceTexture = null

        this.isComponentInit = false
        this.currentState = STATE_IDLE
        this.targetState = STATE_IDLE
    }

    override fun reset() {
        // 保存播放位置
        if (isPlaying() || isBufferingPlaying() || isBufferingPaused() || isPaused()) {
            CommonUtil.savePlayPosition(context, this.url, getCurrentPosition())
        } else if (isCompleted()) {
            CommonUtil.savePlayPosition(context, this.url, 0)
        }

        // 退出全屏或小窗口
        if (isFullScreen() || isTinyWindow()) {
            returnCommon()
        }

        // 释放播放器
        release()

        // 恢复控制器
        this.playController?.reset()

        // gc
        Runtime.getRuntime().gc()
    }

}