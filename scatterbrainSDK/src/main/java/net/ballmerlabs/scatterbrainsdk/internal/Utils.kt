package net.ballmerlabs.scatterbrainsdk.internal

import android.os.Parcel


fun interface ParcelWriter<T> {
    fun writeToParcel(
            value: T,
            parcel: Parcel, flags: Int
    )
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
