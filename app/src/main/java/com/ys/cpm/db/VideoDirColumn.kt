package com.ys.cpm.db

import android.provider.BaseColumns

/**
 * Created by Ys on 2017/6/19.
 * VideoDirColumn
 */
class VideoDirColumn : BaseColumns {

    companion object {

        @Column
        const val TITLE = "dir_title"

        @Column(type = Column.Type.INTEGER)
        const val EDITABLE = "editable"

        @Column(type = Column.Type.LONG)
        const val CHANGE_TIME = "change_time"
    }
}