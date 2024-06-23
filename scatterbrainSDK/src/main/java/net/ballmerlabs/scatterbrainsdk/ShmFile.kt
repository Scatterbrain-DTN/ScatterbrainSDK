package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class ShmFile(private val fd: ParcelFileDescriptor): ShmCompat {
    constructor(parcel: Parcel) : this(parcel.readParcelable(File::class.java.classLoader)!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        fd.writeToParcel(parcel, flags)
    }

    override fun readOnly(): ByteBuffer {
        val i = FileInputStream(fd.fileDescriptor)
        return i.channel.map(FileChannel.MapMode.READ_ONLY, 0, i.channel.size())
    }

    override fun readWrite(): ByteBuffer {
        val i = FileInputStream(fd.fileDescriptor)
        return i.channel.map(FileChannel.MapMode.READ_WRITE, 0, i.channel.size())
    }

    override fun close() {
        fd.close()
    }

    override fun describeContents(): Int {
        return fd.describeContents()
    }

    companion object CREATOR : Parcelable.Creator<ShmFile> {
        override fun createFromParcel(parcel: Parcel): ShmFile {
            return ShmFile(parcel)
        }

        override fun newArray(size: Int): Array<ShmFile?> {
            return arrayOfNulls(size)
        }
    }
}