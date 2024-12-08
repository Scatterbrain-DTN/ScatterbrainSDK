package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.Parcelable

enum class PairingStage(val code: Int) {
    UNKNOWN(0),
    INITIATE(1),
    ACK(2),
    FAILED(3)
}

data class PairingState(
    val appName: String,
    val stage: PairingStage,
    val identity: ByteArray
) : Parcelable {
    constructor(parcel: Parcel) : this(
        appName = parcel.readString()!!,
        stage = when(parcel.readInt()) {
            1 -> PairingStage.INITIATE
            2 -> PairingStage.ACK
            3 -> PairingStage.FAILED
            else -> PairingStage.UNKNOWN
        },
        identity = parcel.createByteArray()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeInt(stage.code)
        parcel.writeByteArray(identity)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PairingState

        if (appName != other.appName) return false
        if (stage != other.stage) return false
        if (!identity.contentEquals(other.identity)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = appName.hashCode()
        result = 31 * result + stage.hashCode()
        result = 31 * result + identity.contentHashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<PairingState> {
        override fun createFromParcel(parcel: Parcel): PairingState {
            return PairingState(parcel)
        }

        override fun newArray(size: Int): Array<PairingState?> {
            return arrayOfNulls(size)
        }
    }
}