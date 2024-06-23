package net.ballmerlabs.scatterbrainsdk.internal

import android.os.Parcel
import android.os.Parcelable
data class SbApp(
    val name: String,
    val desktop: Boolean,
    val id: String? = null,
    ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readBool(),
        parcel.readString(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeBool(desktop)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SbApp> {
        override fun createFromParcel(parcel: Parcel): SbApp {
            return SbApp(parcel)
        }

        override fun newArray(size: Int): Array<SbApp?> {
            return arrayOfNulls(size)
        }
    }
}