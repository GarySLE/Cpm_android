package com.ys.cpm.lib

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ContextThemeWrapper
import android.text.TextUtils
import android.util.TypedValue
import android.view.WindowManager
import java.util.*
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import android.os.Environment.MEDIA_MOUNTED
import android.provider.MediaStore
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by Ys on 2017/8/16.
 */
class CommonUtil {

    companion object {
        /**
         * Get activity from context object
         *
         * @param context something
         * @return object of Activity or null if it is not Activity
         */
        @JvmStatic
        fun scanForActivity(context: Context?): Activity? {
            if (context == null) return null
            if (context is Activity) {
                return context
            } else if (context is ContextWrapper) {
                return scanForActivity(context.baseContext)
            }
            return null
        }

        /**
         * Get AppCompatActivity from context
         *
         * @param context
         * @return AppCompatActivity if it's not null
         */
        @JvmStatic
        fun getAppCompatActivity(context: Context?): AppCompatActivity? {
            if (context == null) return null
            if (context is AppCompatActivity) {
                return context
            } else if (context is ContextThemeWrapper) {
                return getAppCompatActivity(context.baseContext)
            }
            return null
        }

        @JvmStatic
        fun showActionBar(context: Context) {
            val ab = getAppCompatActivity(context)!!.supportActionBar
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false)
                ab.show()
            }
            scanForActivity(context)!!
                    .window
                    .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        @JvmStatic
        fun hideActionBar(context: Context) {
            val ab = getAppCompatActivity(context)!!.supportActionBar
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false)
                ab.hide()
            }
            scanForActivity(context)!!
                    .window
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        /**
         * 获取屏幕宽度
         *
         * @param context
         * @return width of the screen.
         */
        @JvmStatic
        fun getScreenWidth(context: Context): Int =
                context.resources.displayMetrics.widthPixels

        /**
         * 获取屏幕高度
         *
         * @param context
         * @return heiht of the screen.
         */
        @JvmStatic
        fun getScreenHeight(context: Context): Int =
                context.resources.displayMetrics.heightPixels

        /**
         * dp转px
         *
         * @param context
         * @param dpVal   dp value
         * @return px value
         */
        @JvmStatic
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dpVal,
                    context.resources.displayMetrics
            ).toInt()
        }

        /**
         * 将毫秒数格式化为"##:##"的时间
         *
         * @param milliseconds 毫秒数
         * @return ##:##
         */
        @JvmStatic
        fun formatTime(milliseconds: Long): String {
            if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
                return "00:00"
            }
            val totalSeconds = milliseconds / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600
            val stringBuilder = StringBuilder()
            val mFormatter = Formatter(stringBuilder, Locale.getDefault())
            return if (hours > 0) {
                mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            } else {
                mFormatter.format("%02d:%02d", minutes, seconds).toString()
            }
        }

        /**
         * 保存播放位置，以便下次播放时接着上次的位置继续播放.
         *
         * @param context
         * @param url     视频链接url
         * @param position 保存的播放位置
         */
        @JvmStatic
        fun savePlayPosition(context: Context, url: String?, position: Long) {
            if (TextUtils.isEmpty(url)) {
                LogUtil.d("save position url is empty")
                return
            }
            context.
                    getSharedPreferences(
                            "NICE_VIDEO_PALYER_PLAY_POSITION",
                            Context.MODE_PRIVATE
                    )
                    .edit()
                    .putLong(url, position)
                    .apply()
        }

        /**
         * 取出上次保存的播放位置
         *
         * @param context
         * @param url     视频链接url
         * @return 上次保存的播放位置
         */
        @JvmStatic
        fun getSavedPlayPosition(context: Context, url: String?): Long {
            if (TextUtils.isEmpty(url)) {
                LogUtil.d("get save position url is empty")
                return 0
            }
            return context.getSharedPreferences("NICE_VIDEO_PALYER_PLAY_POSITION",
                    Context.MODE_PRIVATE)
                    .getLong(url, 0)
        }

        /* Checks if external storage is available for read and write */
        @JvmStatic
        fun isExternalStorageWritable(): Boolean =
                Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        /* Checks if external storage is available to at least read */
        @JvmStatic
        fun isExternalStorageReadable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state ||
                    Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        /**
         * 广播通知系统相册更新单个文件
         */
        @JvmStatic
        fun updateSystemGallery(context: Context, file: File) {
            val action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            val uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(action, uri))
        }

        /**
         * 保存截图
         * 需要适配动态权限申请
         */
        @JvmStatic
        fun saveBitmapInExternalStorage(context: Context, bitmap: Bitmap?, fileName: String) {
            if (bitmap == null) return

            val path =
                    if (isExternalStorageWritable())
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                        )
                    else
                        context.getExternalFilesDir(null)
            val finalFileName =
                    if (fileName.endsWith(".jpg", true)) fileName else fileName + ".jpg"
            val file = File(path, finalFileName)
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                updateSystemGallery(context, file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}