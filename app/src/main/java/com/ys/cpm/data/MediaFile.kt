package com.ys.cpm.data

import android.os.Parcel
import android.os.Parcelable
import com.ys.cpm.Ext.createParcel

/**
 * Created by Ys on 2017/6/15.
 * Item Video
 */
data class MediaFile(
        var path: String = "",
        var imagePath: String = "",
        var title: String = "",
        var time: Long = 0,
        var dirId: Long = 0
) : Parcelable {

    companion object {

        @JvmField
        val CREATOR = createParcel { MediaFile(it) }
    }

    private constructor(p: Parcel) : this() {
        this.path = p.readString()
        this.imagePath = p.readString()
        this.title = p.readString()
        this.time = p.readLong()
        this.dirId = p.readLong()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.path)
        dest.writeString(this.imagePath)
        dest.writeString(this.title)
        dest.writeLong(this.time)
        dest.writeLong(this.dirId)
    }

    override fun describeContents(): Int = 0

}
