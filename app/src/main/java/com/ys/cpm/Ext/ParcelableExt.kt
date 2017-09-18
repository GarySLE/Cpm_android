package com.ys.cpm.Ext

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Ys on 2017/6/29.
 * ParcelableExt
 */
inline fun <reified T : Parcelable> createParcel(crossinline createFromParcel: (Parcel) -> T?)
        = object : Parcelable.Creator<T> {

    override fun createFromParcel(source: Parcel): T? = createFromParcel(source)

    override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
}