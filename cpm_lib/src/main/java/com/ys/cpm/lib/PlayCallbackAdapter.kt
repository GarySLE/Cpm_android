package com.ys.cpm.lib

import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText

/**
 * Created by Ys on 2017/8/3.
 */
open class PlayCallbackAdapter : IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnTimedTextListener {

    override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean = false

    override fun onPrepared(mp: IMediaPlayer?) {}

    override fun onVideoSizeChanged(mp: IMediaPlayer?,
                                    width: Int,
                                    height: Int,
                                    sar_num: Int,
                                    sar_den: Int
    ) {}

    override fun onCompletion(mp: IMediaPlayer?) {}

    override fun onError(mp: IMediaPlayer?, what: Int, extra: Int): Boolean = false

    override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {}

    override fun onSeekComplete(mp: IMediaPlayer?) {}

    override fun onTimedText(mp: IMediaPlayer?, timedText: IjkTimedText?) {}

}