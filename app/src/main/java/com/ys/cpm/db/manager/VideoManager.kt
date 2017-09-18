package com.ys.cpm.db.manager

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import android.text.TextUtils
import com.ys.cpm.data.MediaFile
import com.ys.cpm.data.PlaylistInfo
import com.ys.cpm.db.VideoDirColumn
import com.ys.cpm.db.VideoFileColumn
import com.ys.cpm.db.SQLiteOperator

/**
 * Created by Ys on 2017/6/19.
 * VideoManager
 */
class VideoManager {

    companion object {

        @JvmStatic
        fun updateVideoDirChangeTime(operator: SQLiteOperator, title: String, changeTime: Long) {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(title)
            val values = ContentValues()
            values.put(VideoDirColumn.CHANGE_TIME, changeTime)
            try {
                operator.update(VideoDirColumn::class.java, values, where, whereArgs)
            } finally {
                operator.close()
            }
        }

        @JvmStatic
        fun updateVideoDirName(operator: SQLiteOperator, title: String, oldTitle: String) {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(oldTitle)
            val values = ContentValues()
            values.put(VideoDirColumn.TITLE, title)
            try {
                operator.update(VideoDirColumn::class.java, values, where, whereArgs)
            } finally {
                operator.close()
            }
        }

        @JvmStatic
        fun deleteVideoDir(operator: SQLiteOperator, title: String) {
            val dirWhere = "${VideoDirColumn.TITLE} = ?"
            val dirWhereArgs = arrayOf(title)
            val columns = arrayOf(BaseColumns._ID)
            var cursor: Cursor? = null
            try {
                cursor = operator.query(VideoDirColumn::class.java, columns, dirWhere, dirWhereArgs)
                if (cursor.count <= 0) {
                    return
                }
                cursor.moveToFirst()
                val dirId = -cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val fileWhere = "${VideoFileColumn.VIDEO_DIR_ID} = ?"
                val fileWhereArgs = arrayOf("$dirId")

                operator.delete(VideoDirColumn::class.java, dirWhere, dirWhereArgs)
                operator.delete(VideoFileColumn::class.java, fileWhere, fileWhereArgs)
            } finally {
                cursor?.close()
                operator.close()
            }
        }

        @JvmStatic
        fun addVideoDir(operator: SQLiteOperator, title: String, editable: Boolean = false, changeTime: Long = System.currentTimeMillis()) {
            val dirValues = makeVideoDirValues(title, if (editable) 1 else 0, changeTime)
            try {
                operator.insert(VideoDirColumn::class.java, dirValues)
            } finally {
                operator.close()
            }
        }

        private fun makeVideoDirValues(title: String, editable: Int, changeTime: Long): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(VideoDirColumn.TITLE, title)
            contentValues.put(VideoDirColumn.EDITABLE, editable)
            contentValues.put(VideoDirColumn.CHANGE_TIME, changeTime)
            return contentValues
        }

        @JvmStatic
        fun getVideoDirs(operator: SQLiteOperator): ArrayList<PlaylistInfo>? {
            var cursor: Cursor? = null
            try {
                cursor = operator.query(VideoDirColumn::class.java, null, null)
                if (cursor.count <= 0) {
                    return null
                }

                val dirs = ArrayList<PlaylistInfo>()
                cursor.moveToFirst()
                do {
                    val dir = makeVideoDir(operator, cursor)
                    dirs.add(dir)
                } while (cursor.moveToNext())
                return dirs
            } finally {
                cursor?.close()
                operator.close()
            }
        }

        private fun makeVideoDir(operator: SQLiteOperator, cursor: Cursor?): PlaylistInfo {
            val title = cursor?.getString(cursor.getColumnIndex(VideoDirColumn.TITLE)) ?: ""
            val editable = cursor?.getInt(cursor.getColumnIndex(VideoDirColumn.EDITABLE)) == 1
            val changeTime = cursor?.getLong(cursor.getColumnIndex(VideoDirColumn.CHANGE_TIME)) ?: 0
            val fileList = getVideoFiles(operator, title)
            return PlaylistInfo(title, editable, changeTime, fileList ?: ArrayList())
        }

        @JvmStatic
        fun updateVideoFile(operator: SQLiteOperator, oldTitle: String, dirTitle: String,
                            title: String = "", subtitle: String = "", imagePath: String = "") {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(dirTitle)
            val columns = arrayOf(BaseColumns._ID)
            var cursor: Cursor? = null
            try {
                cursor = operator.query(VideoDirColumn::class.java, columns, where, whereArgs)
                if (cursor.count <= 0) {
                    return
                }
                cursor.moveToFirst()
                val dirId = -cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val fileWhere = "${VideoFileColumn.VIDEO_DIR_ID} = ? AND ${VideoFileColumn.VIDEO_TITLE} = ?"
                val fileWhereArgs = arrayOf("$dirId", oldTitle)
                val values = ContentValues()
                if (!TextUtils.isEmpty(title)) {
                    values.put(VideoFileColumn.VIDEO_TITLE, title)
                }
                if (!TextUtils.isEmpty(subtitle)) {
                    values.put(VideoFileColumn.VIDEO_SUBTITLE, subtitle)
                }
                if (!TextUtils.isEmpty(imagePath)) {
                    values.put(VideoFileColumn.VIDEO_IMAGE, imagePath)
                }
                operator.update(VideoFileColumn::class.java, values, fileWhere, fileWhereArgs)
            } finally {
                cursor?.close()
                operator.close()
            }
        }

        @JvmStatic
        fun deleteVideoFile(operator: SQLiteOperator, dirTitle: String, fileTitle: String = "") {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(dirTitle)
            val columns = arrayOf(BaseColumns._ID)
            var cursor: Cursor? = null
            try {
                cursor = operator.query(VideoDirColumn::class.java, columns, where, whereArgs)
                if (cursor.count <= 0) {
                    return
                }
                cursor.moveToFirst()
                val dirId = -cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val isAll = TextUtils.isEmpty(fileTitle)
                val fileWhere = StringBuilder("${VideoFileColumn.VIDEO_DIR_ID} = ?")
                if (!isAll) {
                    fileWhere.append(" AND ${VideoFileColumn.VIDEO_TITLE} = ?")
                }
                val fileWhereArgs = if (isAll) arrayOf("$dirId") else arrayOf("$dirId", fileTitle)
                operator.delete(VideoFileColumn::class.java, fileWhere.toString(), fileWhereArgs)
            } finally {
                cursor?.close()
                operator.close()
            }
        }

        @JvmStatic
        fun addVideoFile(operator: SQLiteOperator, path: String, title: String, time: Long,
                         dir: String = "", adTagUrl: String = "", image: String = "",
                         subtitle: String = ""): MediaFile? {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(dir)
            val columns = arrayOf(BaseColumns._ID)
            var cursor: Cursor? = null
            try {
                cursor = operator.query(VideoDirColumn::class.java, columns, where, whereArgs)
                if (cursor.count <= 0) {
                    addVideoDir(operator, dir)
                    cursor = operator.query(VideoDirColumn::class.java, columns, where, whereArgs)
                }
                cursor.moveToFirst()
                val dirId = -cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val fileValues = makeVideoFileValues(dirId, path, image, title, subtitle, time, adTagUrl)
                operator.insert(VideoFileColumn::class.java, fileValues)
                return MediaFile(path, image, title, time, dirId)
            } finally {
                cursor?.close()
                operator.close()
            }
        }

        private fun makeVideoFileValues(id: Long, path: String, image: String, title: String,
                                        subtitle: String, time: Long, adTagUrl: String): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(VideoFileColumn.VIDEO_PATH, path)
            contentValues.put(VideoFileColumn.VIDEO_IMAGE, image)
            contentValues.put(VideoFileColumn.VIDEO_TITLE, title)
            contentValues.put(VideoFileColumn.VIDEO_SUBTITLE, subtitle)
            contentValues.put(VideoFileColumn.VIDEO_TIME, time)
            contentValues.put(VideoFileColumn.VIDEO_DIR_ID, id)
            contentValues.put(VideoFileColumn.AD_TAG_URL, adTagUrl)
            return contentValues
        }

        @JvmStatic
        fun getVideoFiles(operator: SQLiteOperator, dirTitle: String): ArrayList<MediaFile>? {
            val where = "${VideoDirColumn.TITLE} = ?"
            val whereArgs = arrayOf(dirTitle)
            val columns = arrayOf(BaseColumns._ID)
            var dirCursor: Cursor? = null
            var fileCursor: Cursor? = null
            try {
                dirCursor = operator.query(VideoDirColumn::class.java, columns, where, whereArgs)
                if (dirCursor.count <= 0) {
                    return null
                }
                dirCursor.moveToFirst()
                val dirId = -dirCursor.getLong(dirCursor.getColumnIndex(BaseColumns._ID))

                val fileWhere = "${VideoFileColumn.VIDEO_DIR_ID} = ?"
                val fileWhereArgs = arrayOf("$dirId")
                fileCursor = operator.query(VideoFileColumn::class.java, fileWhere, fileWhereArgs)
                if (fileCursor.count <= 0) {
                    return null
                }
                val files = ArrayList<MediaFile>()
                fileCursor.moveToFirst()
                do {
                    val file = makeVideoFile(fileCursor)
                    file.dirId = dirId
                    files.add(file)
                } while (fileCursor.moveToNext())
                return files
            } finally {
                dirCursor?.close()
                fileCursor?.close()
                operator.close()
            }
        }

        private fun makeVideoFile(cursor: Cursor?): MediaFile {
            val path = cursor?.getString(cursor.getColumnIndex(VideoFileColumn.VIDEO_PATH)) ?: ""
            val imagePath = cursor?.getString(cursor.getColumnIndex(VideoFileColumn.VIDEO_IMAGE)) ?: ""
            val title = cursor?.getString(cursor.getColumnIndex(VideoFileColumn.VIDEO_TITLE)) ?: ""
            val time = cursor?.getLong(cursor.getColumnIndex(VideoFileColumn.VIDEO_TIME)) ?: 0
            return MediaFile(path, imagePath, title, time)
        }
    }
}