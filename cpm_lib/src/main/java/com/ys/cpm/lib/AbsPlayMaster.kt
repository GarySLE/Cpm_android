package com.ys.cpm.lib

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by Ys on 2017/8/16.
 */
abstract class AbsPlayMaster : FrameLayout {

    companion object {

        /**
         * 播放错误
         */
        const val STATE_ERROR = -1
        /**
         * 播放未开始
         */
        const val STATE_IDLE = 0
        /**
         * 播放准备中
         */
        const val STATE_PREPARING = 1
        /**
         * 播放准备就绪
         */
        const val STATE_PREPARED = 2
        /**
         * 正在播放
         */
        const val STATE_PLAYING = 3
        /**
         * 暂停播放
         */
        const val STATE_PAUSED = 4
        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
         */
        const val STATE_BUFFERING_PLAYING = 5
        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
         */
        const val STATE_BUFFERING_PAUSED = 6
        /**
         * 播放完成
         */
        const val STATE_COMPLETED = 7
        /**
         * 跳转中
         */
        const val STATE_SEEKING = 8

        /**
         * 普通模式
         */
        const val MODE_COMMON = 10
        /**
         * 全屏模式
         */
        const val MODE_FULL_SCREEN = 11
        /**
         * 小窗口模式
         */
        const val MODE_TINY_WINDOW = 12

    }

    /**
     * 播放器当前状态
     */
    open var currentState = STATE_IDLE
    /**
     * 播放器当前模式
     */
    open var currentMode = MODE_COMMON
    /**
     * 画面显示模式
     */
    open var currentDisplayMode: Int = 0

    open var url: String? = null
    open var headers: Map<String, String>? = null

    /**
     * 是否从上一次中断的位置继续播放
     * true 上次中断的位置继续播放，false 从头开始播放
     */
    open var isContinueFromLastPlay = false

    /**
     * 缓冲百分比
     */
    open var bufferPercent = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    /**
     * 设置Url、headers, 以及用来设置其他播放参数
     *
     * @param url     视频地址，可以是本地，也可以是网络视频
     * @param headers 请求header.
     */
    open fun setup(url: String, headers: Map<String, String>?) {
        this.url = url
        this.headers = headers
    }

    /**
     * 设置显示画面比例
     *
     * @param mode 显示模式标签
     * [PlayTexture.DISPLAY_MODE_SMART_PARENT]
     * [PlayTexture.DISPLAY_MODE_ORIGIN_SIZE]
     * [PlayTexture.DISPLAY_MODE_16_9]
     * [PlayTexture.DISPLAY_MODE_4_3]
     */
    fun setDisplayMode(mode: Int): Boolean {
        return if (this.currentDisplayMode == mode) {
            false
        } else {
            onDisplayModeChange(mode)
        }
    }

    // 播放器在当前的播放状态
    open fun isIdle(): Boolean = this.currentState == STATE_IDLE
    open fun isPreparing(): Boolean = this.currentState == STATE_PREPARING
    open fun isPrepared(): Boolean = this.currentState == STATE_PREPARED
    open fun isBufferingPlaying(): Boolean = this.currentState == STATE_BUFFERING_PLAYING
    open fun isBufferingPaused(): Boolean = this.currentState == STATE_BUFFERING_PAUSED
    open fun isPlaying(): Boolean = this.currentState == STATE_PLAYING
    open fun isPaused(): Boolean = this.currentState == STATE_PAUSED
    open fun isSeeking(): Boolean = this.currentState == STATE_SEEKING
    open fun isError(): Boolean = this.currentState == STATE_ERROR
    open fun isCompleted(): Boolean = this.currentState == STATE_COMPLETED
    // ========================

    // 播放器在当前的播放模式
    open fun isFullScreen(): Boolean = this.currentMode == MODE_FULL_SCREEN
    open fun isTinyWindow(): Boolean = this.currentMode == MODE_TINY_WINDOW
    open fun isCommon(): Boolean = this.currentMode == MODE_COMMON
    // =========================

    /**
     * 开始播放
     */
    abstract fun play()

    /**
     * 从指定的位置开始播放
     *
     * @param position 播放位置
     */
    abstract fun play(position: Long)

    /**
     * 重新播放，播放器被暂停、播放错误、播放完成后，需要调用此方法重新播放
     */
    abstract fun replay()

    /**
     * 暂停播放
     */
    abstract fun pause()

    /**
     * 跳转到指定的位置继续播放
     *
     * @param position 播放位置
     */
    abstract fun seekTo(position: Long)

    /**
     * 显示模式切换
     *
     * @param mode 显示模式标签
     */
    abstract fun onDisplayModeChange(mode: Int): Boolean

    /**
     * 设置音量
     *
     * @param volume 音量值
     */
    abstract fun setVolume(volume: Int)

    /**
     * 获取最大音量
     *
     * @return 最大音量值
     */
    abstract fun getMaxVolume(): Int

    /**
     * 获取当前音量
     *
     * @return 当前音量值
     */
    abstract fun getVolume(): Int

    /**
     * 获取当前播放帧
     */
    abstract fun getCurrentFrameBitmap(): Bitmap?

    /**
     * 获取办法给总时长，毫秒
     *
     * @return 视频总时长ms
     */
    abstract fun getDuration(): Long

    /**
     * 获取当前播放的位置，毫秒
     *
     * @return 当前播放位置，ms
     */
    abstract fun getCurrentPosition(): Long

    /**
     * 进入全屏模式
     */
    abstract fun enterFullScreen()

    /**
     * 进入小窗口模式
     */
    abstract fun enterTinyWindow()

    /**
     * 退回通常模式
     *
     * @return true 退回通常
     */
    abstract fun returnCommon(): Boolean

    /**
     * 此处只释放播放器（如果要释放播放器并恢复控制器状态需要调用reset()方法）
     * 不管是全屏、小窗口还是Common状态下控制器的UI都不恢复初始状态
     * 这样以便在当前播放器状态下可以方便的切换不同的清晰度
     */
    abstract fun release()

    /**
     * 释放全部资源，内部的播放器被释放掉，同时退出全屏、小窗口模式
     * 并且控制器的UI也应该恢复到最初始的状态.
     */
    abstract fun reset()

}