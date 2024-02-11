package net.ballmerlabs.scatterbrainsdk

import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.os.SharedMemory
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.nio.ByteBuffer

fun Context.newShm(name: String, data: ByteArray): ShmCompat {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        val shm = SharedMemory.create(name, data.size)
        val buf = shm.mapReadWrite()
        buf.put(data)
        ShmSharedMemory(shm)
    } else {
        val file = File.createTempFile(name, null, this.cacheDir)
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        FileOutputStream(fd.fileDescriptor).apply {
            write(data)
            close()
        }
        ShmFile(fd)
    }
}

interface ShmCompat: Parcelable {
    fun readOnly(): ByteBuffer
    fun readWrite(): ByteBuffer
    fun close()
}