package net.ballmerlabs.scatterbrainsdk.internal

import android.os.Parcel
import java.util.Base64


fun interface ParcelWriter<T> {
    fun writeToParcel(
            value: T,
            parcel: Parcel, flags: Int
    )
}

fun Parcel.readBool(): Boolean {
    return readInt() != 0
}

fun Parcel.writeBool(boolean: Boolean) {
    writeInt(if (boolean) 1 else 0)
}

fun ByteArray.b64(): String {
    return Base64.getUrlEncoder().encodeToString(this)
}

fun interface ParcelReader<T> {
    fun readFromParcel(parcel: Parcel): T
}

fun <K, V> writeParcelableMap(
        map: Map<K, V>,
        parcel: Parcel,
        flags: Int,
        parcelWriter: ParcelWriter<Map.Entry<K, V>>
) {
    parcel.writeInt(map.size)
    for (e in map.entries) {
        parcelWriter.writeToParcel(e, parcel, flags)
    }
}

fun <K, V> readParcelableMap(
        parcel: Parcel,
        parcelReader: ParcelReader<Map.Entry<K, V>>
): Map<K, V> {
    val size = parcel.readInt()
    val map: MutableMap<K, V> = HashMap(size)
    for (i in 0 until size) {
        val value = parcelReader.readFromParcel(parcel)
        map[value.key] = value.value
    }
    return map
}
