package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.Parcelable
import net.ballmerlabs.scatterbrainsdk.internal.readParcelableMap
import net.ballmerlabs.scatterbrainsdk.internal.writeParcelableMap
import java.util.AbstractMap
import java.util.HashMap

class PermissionStatus(
        private val permissions: Map<String, Boolean>
): Parcelable {
    constructor(parcel: Parcel) : this(
            permissions = readParcelableMap(parcel) { p ->
                val key = parcel.readInt()
                val b = key == 0
                AbstractMap.SimpleEntry(parcel.readString()!!, b)
            }
    ) {
    }

    fun allGranted(): Boolean {
        return ! permissions.values.contains(false)
    }

    fun granted(value: String): Boolean {
        return permissions.getOrDefault(value, false)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeParcelableMap(permissions, parcel, flags) { mapentry, p, _ ->
            p.writeInt(if (mapentry.value) 0 else 1)
            p.writeString(mapentry.key)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PermissionStatus> {
        override fun createFromParcel(parcel: Parcel): PermissionStatus {
            return PermissionStatus(parcel)
        }

        override fun newArray(size: Int): Array<PermissionStatus?> {
            return arrayOfNulls(size)
        }

        const val PERMISSION_BLUETOOTH_ADVERTISE = "BluetoothAdvertise"
        const val PERMISSION_BLUETOOTH_CONNECT = "BluetoothConnect"
        const val PERMISSION_LOCATION = "Location"
    }
}