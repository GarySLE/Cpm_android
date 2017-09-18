package com.ys.cpm.vm

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by Ys on 2017/9/17.
 */
open class RecyclerViewModel(
        activity: Activity,
        manager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
) {

    val context = activity
    var layoutManager = manager

}