package com.ys.cpm.db

import android.content.Context

/**
 * Created by Ys on 2017/6/19.
 * VideoDBHelper
 */
class VideoDBHelper(
        ctx: Context,
        name: String,
        version: Int
) : DBHelper(ctx, name, version) {

    init {
        mClazz = arrayOf(VideoDirColumn::class.java, VideoFileColumn::class.java)
    }
}