package com.ys.cpm.lib

/**
 * Created by Ys on 2017/8/16.
 */
object PlayManager {

    private var playMaster: ChaosPlayMaster? = null

    fun getCurrentPlayMaster(): ChaosPlayMaster? = this.playMaster

    fun setCurrentPlayMaster(videoPlayer: ChaosPlayMaster) {
        if (this.playMaster != videoPlayer) {
            releasePlayMaster()
            this.playMaster = videoPlayer
        }
    }

    fun pausePlayMaster() {
        if (this.playMaster != null &&
                (this.playMaster!!.isPlaying() ||
                        this.playMaster!!.isBufferingPlaying())) {
            this.playMaster!!.pause()
        }
    }

    fun resumePlayMaster() {
        if (this.playMaster != null &&
                (this.playMaster!!.isPaused() ||
                        this.playMaster!!.isBufferingPaused())) {
            this.playMaster!!.replay()
        }
    }

    fun releasePlayMaster() {
        if (this.playMaster != null) {
            this.playMaster!!.reset()
            this.playMaster = null
        }
    }

    fun onBackPressd(): Boolean {
        if (this.playMaster != null) {
            if (this.playMaster!!.isFullScreen() ||
                    this.playMaster!!.isTinyWindow()) {
                return this.playMaster!!.returnCommon()
            }
        }
        return false
    }

}