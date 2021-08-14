package net.ballmerlabs.scatterbrainsdk.internal

import android.os.Parcel
import android.os.Parcelable

/**
 * Transaction statistics from a handshake with a Scatterbrain peer.
 *
 * @property identities number of identities transferred
 * @property messages number of messages transferred
 * @property stackTrace error status of the transaction
 * @property success true if the transaction completed successfully
 */
open class HandshakeResult : Parcelable {
    enum class TransactionStatus {
        STATUS_SUCCESS, STATUS_FAIL
    }

    protected constructor(parcel: Parcel) {
        identities = parcel.readInt()
        messages = parcel.readInt()
        status = TransactionStatus.values()[parcel.readInt()]
    }

    val success: Boolean
        get() = status == TransactionStatus.STATUS_SUCCESS
    val identities: Int
    val messages: Int
    val status: TransactionStatus

    constructor(
            identities: Int,
            messages: Int,
            status: TransactionStatus
    ) {
        this.status = status
        this.messages = messages
        this.identities = identities
    }

    fun from(stats: HandshakeResult): HandshakeResult {
        val status: TransactionStatus = if (stats.status == TransactionStatus.STATUS_FAIL ||
                this.status == TransactionStatus.STATUS_FAIL) {
            TransactionStatus.STATUS_FAIL
        } else {
            stats.status
        }
        return HandshakeResult(
                stats.identities + identities,
                stats.messages + messages,
                status
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(identities)
        parcel.writeInt(messages)
        parcel.writeInt(status.ordinal)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<HandshakeResult> = object : Parcelable.Creator<HandshakeResult> {
            override fun createFromParcel(parcel: Parcel): HandshakeResult {
                return HandshakeResult(parcel)
            }

            override fun newArray(size: Int): Array<HandshakeResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}