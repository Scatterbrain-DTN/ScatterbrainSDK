package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import net.ballmerlabs.scatterbrainsdk.internal.b64
import net.ballmerlabs.scatterbrainsdk.internal.readBool
import net.ballmerlabs.scatterbrainsdk.internal.readParcelableMap
import net.ballmerlabs.scatterbrainsdk.internal.writeBool
import net.ballmerlabs.scatterbrainsdk.internal.writeParcelableMap
import java.util.AbstractMap
import java.util.UUID

fun parcelArray(parcel: Parcel): ByteArray {
    val s = ByteArray(parcel.readInt())
    parcel.readByteArray(s)
    return s
}

const val PROTOBUF_PRIVKEY_KEY = "scatterbrain"

/**
 * A handle to a cryptographic identity stored in the Scatterbrain
 * router. This class contains all identity metadata except for the
 * private key (for security reasons). This class has no public constructor
 * and is only returned by Scatterbrain api functions
 *
 * @property publicKey ed25519 public key used by Scatterbrain
 * @property extraKeys additional user defined keys or metadata
 * @property sig ed25519 signature for this identity
 * @property fingerprint unique identifier for this identity
 * @property name user-defined name
 * @property isOwned true if this identity has a private key
 */
@Stable
data class Identity(
    @Stable
    val extraKeys: ImmutableMap<String, ByteArray>,
    @Stable
    val publicKey: ByteArray = extraKeys[PROTOBUF_PRIVKEY_KEY]!!,
    @Stable
    val name: String,
    @Stable
    val sig: ByteArray,
    @Stable
    val fingerprint: UUID,
    @Stable
    val isOwned: Boolean,
    @Stable
    val frozen: Boolean,
) : Parcelable {
    constructor(inParcel: Parcel) : this(
        extraKeys = readParcelableMap(inParcel) { parcel ->
            val len = parcel.readInt()
            val key = ByteArray(len)
            parcel.readByteArray(key)
            AbstractMap.SimpleEntry(parcel.readString()!!, key)
        }.toImmutableMap(),
        name = inParcel.readString()!!,
        sig = parcelArray(inParcel),
        fingerprint = inParcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)!!.uuid!!,
        isOwned = hasKey(inParcel.readByte().toInt()),
        frozen = inParcel.readBool()
    )

    @Stable
    override fun describeContents(): Int {
        return 0
    }

    @Stable
    override fun writeToParcel(parcel: Parcel, i: Int) {
        writeParcelableMap(extraKeys, parcel, i) { mapentry, p, _ ->
            p.writeInt(mapentry.value.size)
            p.writeByteArray(mapentry.value)
            p.writeString(mapentry.key)
        }
        parcel.writeString(name)
        parcel.writeInt(sig.size)
        parcel.writeByteArray(sig)
        parcel.writeParcelable(ParcelUuid(fingerprint), i)
        parcel.writeByte(hasKey(isOwned))
        parcel.writeBool(frozen)
    }

    @Stable
    override fun toString(): String {
        var id = "Identity(" +
                "sig=${this.sig.b64()}\n" +
                "pubkey=${this.publicKey.b64()}\n" +
                "name=${this.name}\n" +
                "isOwned=${this.isOwned}\n" +
                "fingerprint=${this.fingerprint}"

        for ((k, v) in extraKeys) {
            id += "     ($k, ${v.b64()})\n"
        }
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as Identity

        if (extraKeys.keys.any { k ->
                !other.extraKeys.containsKey(k) || !other.extraKeys[k].contentEquals(
                    extraKeys[k]
                )
            }) return false

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (name != other.name) return false
        if (!sig.contentEquals(other.sig)) return false
        if (fingerprint != other.fingerprint) return false
        if (isOwned != other.isOwned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        extraKeys.forEach { (k, v) ->
            result = 31 * result + k.hashCode()
            result = 31 * result + v.contentHashCode()
        }
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + sig.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + isOwned.hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Identity> = object : Parcelable.Creator<Identity> {
            override fun createFromParcel(`in`: Parcel): Identity {
                return Identity(`in`)
            }

            override fun newArray(size: Int): Array<Identity?> {
                return arrayOfNulls(size)
            }
        }

        fun hasKey(`val`: Int): Boolean {
            return `val` == 0
        }

        fun hasKey(`val`: Boolean): Byte {
            return if (`val`) {
                0
            } else {
                1
            }
        }
    }
}