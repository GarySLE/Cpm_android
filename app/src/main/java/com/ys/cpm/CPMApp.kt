package com.ys.cpm

import android.app.Application
import com.ys.cpm.Ext.DelegatesExt
import com.ys.cpm.db.VideoDBHelper

/**
 * Created by Ys on 2017/9/17.
 */
class CPMApp : Application() {

    companion object {
        var instance by DelegatesExt.notNullSingleValue<CPMApp>()
    }

    lateinit var dbHelper: VideoDBHelper

    override fun onCreate() {
        super.onCreate()
        instance = this
        dbHelper = VideoDBHelper(this, "db_cpm", 1)
    }

}