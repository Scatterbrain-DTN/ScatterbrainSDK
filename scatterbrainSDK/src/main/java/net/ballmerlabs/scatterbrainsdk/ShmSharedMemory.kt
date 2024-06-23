package net.ballmerlabs.scatterbrainsdk

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.SharedMemory
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
@RequiresApi(Build.VERSION_CODES.O_MR1)
class ShmSharedMemory(private val sharedMemory: SharedMemory) : ShmCompat {
    constructor(parcel: Parcel) : this(parcel.readParcelable(SharedMemory::class.java.classLoader)!!)

    override fun close() {
        sharedMemory.close()
    }

    override fun readOnly(): ByteBuffer {
        return sharedMemory.mapReadOnly()
    }

    override fun readWrite(): ByteBuffer {
        return sharedMemory.mapReadWrite()
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeParcelable(sharedMemory, p1)
    }

    override fun describeContents(): Int {
        return sharedMemory.describeContents()
    }

    companion object CREATOR : Parcelable.Creator<ShmSharedMemory> {
        override fun createFromParcel(parcel: Parcel): ShmSharedMemory {
            return ShmSharedMemory(parcel)
        }

        override fun newArray(size: Int): Array<ShmSharedMemory?> {
            return arrayOfNulls(size)
        }
    }
}