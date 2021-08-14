package net.ballmerlabs.scatterbrainsdk

import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import java.util.*

open class Identity : Parcelable {
    val extraKeys: Map<String, ByteArray>
    val publicKey: ByteArray
    val name: String
    val sig: ByteArray
    val fingerprint: UUID
    var hasPrivateKey: Boolean

    protected constructor(
            map: Map<String, ByteArray>,
            pub: ByteArray,
            name: String,
            sig: ByteArray,
            fingerprint: UUID,
            hasPrivateKey: Boolean
    ) {
        extraKeys = map
        publicKey = pub
        this.name = name
        this.sig = sig
        this.fingerprint = fingerprint
        this.hasPrivateKey = hasPrivateKey
    }

    private fun interface ParcelWriter<T> {
        fun writeToParcel(
                value: T,
                parcel: Parcel, flags: Int
        )
    }

    private fun interface ParcelReader<T> {
        fun readFromParcel(parcel: Parcel): T
    }

    protected constructor(inParcel: Parcel) {
        extraKeys = readParcelableMap(inParcel) { parcel ->
            val len = parcel.readInt()
            val key = ByteArray(len)
            parcel.readByteArray(key)
            AbstractMap.SimpleEntry(parcel.readString()!!, key)
        }
        publicKey = extraKeys[ScatterbrainApi.PROTOBUF_PRIVKEY_KEY]!!
        name = inParcel.readString()!!
        sig = ByteArray(inParcel.readInt())
        inParcel.readByteArray(sig)
        val uuid = inParcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)
        fingerprint = uuid!!.uuid
        hasPrivateKey = hasKey(inParcel.readByte().toInt())
    }

    override fun describeContents(): Int {
        return 0
    }

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
        parcel.writeByte(hasKey(hasPrivateKey))
    }

    companion object {
        private fun <K, V> writeParcelableMap(
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

        private fun <K, V> readParcelableMap(
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

        @JvmField
        val CREATOR: Parcelable.Creator<Identity> = object : Parcelable.Creator<Identity> {
            override fun createFromParcel(`in`: Parcel): Identity {
                return Identity(`in`)
            }

            override fun newArray(size: Int): Array<Identity?> {
                return arrayOfNulls(size)
            }
        }

        private fun hasKey(`val`: Int): Boolean {
            return `val` == 0
        }

        private fun hasKey(`val`: Boolean): Byte {
            return if (`val`) {
                0
            } else {
                1
            }
        }
    }
}