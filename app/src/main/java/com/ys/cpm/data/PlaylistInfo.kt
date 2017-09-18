package com.ys.cpm.data

import android.os.Parcel
import android.os.Parcelable
import com.ys.cpm.Ext.createParcel

/**
 * Created by Ys on 2017/6/15.
 * Item Video Dir
 */
data class PlaylistInfo(
        var title: String? = "",
        var editable: Boolean = true,
        var changeTime: Long = 0,
        val fileList: ArrayList<MediaFile> = ArrayList()
) : Parcelable {

    fun filecount() = fileList.size

    companion object {

        @JvmField
        val CREATOR = createParcel { PlaylistInfo(it) }
    }

    private constructor(p: Parcel) : this() {
        this.title = p.readString()
        this.editable = p.readInt() == 1
        p.readList(this.fileList, javaClass.classLoader)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.title)
        dest.writeInt(if (this.editable) 1 else 0)
        dest.writeList(this.fileList)
    }

    override fun describeContents(): Int = 0
}