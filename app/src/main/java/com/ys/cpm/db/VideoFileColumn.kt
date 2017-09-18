package com.ys.cpm.db

import android.provider.BaseColumns

/**
 * Created by Ys on 2017/6/19.
 * VideoFileColumn
 */
class VideoFileColumn : BaseColumns {

    companion object {

        @Column
        const val VIDEO_DIR_ID: String = "video_dir_id"

        @Column
        const val VIDEO_TITLE: String = "video_title"

        @Column
        const val VIDEO_SUBTITLE: String = "video_subtitle"

        @Column
        const val VIDEO_IMAGE: String = "video_image"

        @Column
        const val VIDEO_PATH: String = "video_path"

        @Column
        const val AD_TAG_URL: String = "ad_tag_url"

        @Column(type = Column.Type.LONG)
        const val VIDEO_TIME = "video_time"
    }
}