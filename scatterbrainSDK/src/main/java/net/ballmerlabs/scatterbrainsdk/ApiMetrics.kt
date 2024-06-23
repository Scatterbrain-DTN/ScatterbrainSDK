package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

open class ApiMetrics(
    open var application: String,
    open var messages: Long = 0,
    open var signed: Long = 0,
    open var lastSeen: Long = Date().time
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"invalid",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p: Parcel, i: Int) {
        p.writeString(application)
        p.writeLong(messages)
        p.writeLong(signed)
        p.writeLong(lastSeen)
    }

    companion object CREATOR : Parcelable.Creator<ApiMetrics> {
        override fun createFromParcel(parcel: Parcel): ApiMetrics {
            return ApiMetrics(parcel)
        }

        override fun newArray(size: Int): Array<ApiMetrics?> {
            return arrayOfNulls(size)
        }
    }
}