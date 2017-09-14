package com.ys.cpm.lib

import android.util.Log

/**
 * Created by Ys on 2017/8/16.
 */
class LogUtil {

    companion object {
        private val TAG = "ChaosPlayMaster"

        @JvmStatic
        fun d(message: String) {
            Log.d(TAG, message)
        }

        @JvmStatic
        fun i(message: String) {
            Log.i(TAG, message)
        }

        @JvmStatic
        fun e(message: String, throwable: Throwable) {
            Log.e(TAG, message, throwable)
        }
    }

}