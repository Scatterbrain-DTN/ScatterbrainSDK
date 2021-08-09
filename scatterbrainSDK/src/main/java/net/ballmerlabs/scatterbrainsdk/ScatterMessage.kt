package net.ballmerlabs.scatterbrainsdk

import android.net.Uri
import android.os.*
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.util.*

private fun validateBody(`val`: Int): Int {
    if (`val` > ScatterbrainApi.MAX_BODY_SIZE) {
        throw BadParcelableException("invalid array size")
    }
    return `val`
}


private fun readByteArray(parcel: Parcel): ByteArray {
    val b = ByteArray(validateBody(parcel.readInt()))
    parcel.readByteArray(b)
    return b
}

private fun boolConvert(int: Int): Boolean {
    return int != 0
}

private fun boolConvert(boolean: Boolean): Int {
    return if (boolean) 1 else 0
}

open class ScatterMessage private constructor(
        val body: ByteArray?,
        val fromFingerprint: UUID?,
        val toFingerprint: UUID?,
        open val application: String,
        val extension: String,
        val mime: String,
        val filename: String?,
        val sendDate: Date,
        val receiveDate: Date,
        val fileDescriptor: ParcelFileDescriptor?,
        val toDisk: Boolean,
        val id: ParcelUuid?
): Parcelable {

    private constructor(parcel: Parcel): this(
            body = readByteArray(parcel),
            fromFingerprint = parcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)?.uuid,
            toFingerprint = parcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)?.uuid,
            application = parcel.readString()!!,
            extension = parcel.readString()!!,
            mime =  parcel.readString()!!,
            filename =  parcel.readString(),
            fileDescriptor = parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader),
            toDisk = boolConvert(parcel.readInt()),
            sendDate = Date(parcel.readLong()),
            receiveDate = Date(parcel.readLong()),
            id = parcel.readParcelable(ParcelUuid::class.java.classLoader)
    )

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(body!!.size)
        parcel.writeByteArray(body)
        parcel.writeParcelable(if (fromFingerprint == null) null else ParcelUuid(fromFingerprint), i)
        parcel.writeParcelable(if (toFingerprint == null) null else ParcelUuid(toFingerprint), i)
        parcel.writeString(application)
        parcel.writeString(extension)
        parcel.writeString(mime)
        parcel.writeString(filename)
        parcel.writeParcelable(fileDescriptor, i)
        parcel.writeInt(boolConvert(toDisk))
        parcel.writeLong(sendDate.time)
        parcel.writeLong(receiveDate.time)
        parcel.writeParcelable(id, i)
    }

    data class Builder(
            private var body: ByteArray? = null,
            private var fromFingerprint: UUID? = null,
            private var toFingerprint: UUID? = null,
            private var application: String? = null,
            private var extension: String? = null,
            private var mime: String? = null,
            private var filename: String? = null,
            private var fileDescriptor: ParcelFileDescriptor? = null,
            private var fingerprint: String = "",
            private var fileNotFound: Boolean = false,
            private var todisk:Boolean = fileDescriptor != null,
            private var sendDate: Date = Date(0L),
            private var receiveDate: Date = Date(0L),
            private var id: ParcelUuid? = null
    ) {

        fun setBody(body: ByteArray?) = apply {
            this.body = body
            todisk = false
        }

        fun setTo(to: UUID?) = apply {
            toFingerprint = to
        }

        fun setFrom(from: UUID?) = apply {
            fromFingerprint = from
        }

        fun setApplication(application: String) = apply {
            this.application = application
        }

        fun setSendDate(sendDate: Date) = apply {
            this.sendDate = sendDate
        }

        fun setId(id: UUID) = apply {
            this.id =  ParcelUuid(id)
        }

        fun setFile(file: File?) = apply {
            if (file != null) {
                try {
                    fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    this.extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
                    mime = ScatterbrainApi.getMimeType(file)
                    filename = file.name
                    todisk = true
                } catch (e: FileNotFoundException) {
                    fileDescriptor = null
                    mime = null
                    filename = null
                    this.extension = null
                    fileNotFound = true
                }
            }
        }

        fun setFile(descriptor: ParcelFileDescriptor?, ext: String?, mime: String?, name: String?) = apply {
            fileDescriptor = descriptor
            this.extension = ext
            this.mime = mime
            filename = name
            todisk = true
        }

        private fun verify() {
            require(!(body != null && fileDescriptor != null)) { "must set one of body or file" }
            require(!(body == null && fileDescriptor == null)) { "set either body or file" }
            requireNotNull(application) { "applicaiton must be set" }
            check(!fileNotFound) { "file not found" }
        }

        fun build(): ScatterMessage {
            verify()
            return ScatterMessage(
                    body = body,
                    toFingerprint = toFingerprint,
                    fromFingerprint = fromFingerprint,
                    application = application!!,
                    extension = extension?:"",
                    mime = mime?: ScatterbrainApi.DEFAULT_MIME,
                    filename = filename,
                    fileDescriptor = fileDescriptor,
                    toDisk = todisk,
                    sendDate = sendDate,
                    receiveDate = receiveDate,
                    id = id
            )
        }

        init {
            fingerprint = ""
        }
    }

    companion object {
        const val DISK = 1
        const val NODISK = 0
        @JvmField
        val CREATOR: Parcelable.Creator<ScatterMessage?> = object : Parcelable.Creator<ScatterMessage?> {
            override fun createFromParcel(`in`: Parcel): ScatterMessage? {
                return ScatterMessage(`in`)
            }

            override fun newArray(size: Int): Array<ScatterMessage?> {
                return arrayOfNulls(size)
            }
        }

        fun newBuilder(): Builder {
            return Builder()
        }
    }
}