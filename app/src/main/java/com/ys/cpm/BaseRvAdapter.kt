package com.ys.cpm

import android.content.Context
import android.support.v7.widget.RecyclerView

/**
 * Created by Ys on 2017/6/20.
 * Base RecyclerView Adapter
 */
abstract class BaseRvAdapter<E, VH : RecyclerView.ViewHolder?>(
        protected val mCtx: Context,
        protected val mList: ArrayList<E> = ArrayList()
) : RecyclerView.Adapter<VH>() {

    fun getItem(position: Int): E? {
        return if (position >= 0 && position < mList.size)
            mList[position]
        else
            null
    }

    fun getList(): ArrayList<E> = mList

    fun add(item: E?) {
        if (item == null) return
        mList.add(item)
        notifyDataSetChanged()
    }

    fun removeAt(index: Int): E {
        val dir = mList.removeAt(index)
        notifyDataSetChanged()
        return dir
    }

    fun remove(item: E?) {
        if (item == null) return
        mList.remove(item)
        notifyDataSetChanged()
    }

    fun addAll(list: Collection<E>?) {
        if (list == null || list.isEmpty()) return
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mList.size

    override fun getItemId(position: Int): Long = position.toLong()
}