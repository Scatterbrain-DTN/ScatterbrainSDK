package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class DesktopApp (
    val remotekey: ByteArray,
    var pubkey: ByteArray,
    var session: UUID,
    var name: String,
    val remoteFingerprint: ByteArray,
    var paired: Boolean = false,
    var admin: Boolean = false,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createByteArray()!!,
        parcel.createByteArray()!!,
        UUID(parcel.readLong(), parcel.readLong()),
        parcel.readString()!!,
        parcel.createByteArray()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DesktopApp

        if (!remotekey.contentEquals(other.remotekey)) return false
        if (!pubkey.contentEquals(other.pubkey)) return false
        if (session != other.session) return false
        if (name != other.name) return false
        if (!remoteFingerprint.contentEquals(other.remoteFingerprint)) return false
        if (paired != other.paired) return false
        if (admin != other.admin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remotekey.contentHashCode()
        result = 31 * result + pubkey.contentHashCode()
        result = 31 * result + session.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + remoteFingerprint.contentHashCode()
        result = 31 * result + paired.hashCode()
        result = 31 * result + admin.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(remotekey)
        parcel.writeByteArray(pubkey)
        parcel.writeLong(session.mostSignificantBits)
        parcel.writeLong(session.leastSignificantBits)
        parcel.writeString(name)
        parcel.writeByteArray(remoteFingerprint)
        parcel.writeByte(if (paired) 1 else 0)
        parcel.writeByte(if (admin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DesktopApp> {
        override fun createFromParcel(parcel: Parcel): DesktopApp {
            return DesktopApp(parcel)
        }

        override fun newArray(size: Int): Array<DesktopApp?> {
            return arrayOfNulls(size)
        }
    }
}