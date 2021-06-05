package net.ballmerlabs.scatterbrainsdk

import android.net.Uri
import android.os.BadParcelableException
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
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
        val fromFingerprint: String?,
        val toFingerprint: String?,
        open val application: String,
        val extension: String,
        val mime: String,
        val filename: String?,
        val sendDate: Date,
        val receiveDate: Date,
        val fileDescriptor: ParcelFileDescriptor?,
        val toDisk: Boolean,
        val shouldSign: Boolean
): Parcelable {

    private constructor(parcel: Parcel): this(
            body = readByteArray(parcel),
            fromFingerprint = parcel.readString(),
            toFingerprint = parcel.readString(),
            application = parcel.readString()!!,
            extension = parcel.readString()!!,
            mime =  parcel.readString()!!,
            filename =  parcel.readString(),
            fileDescriptor = parcel.readFileDescriptor(),
            toDisk = boolConvert(parcel.readInt()),
            sendDate = Date(parcel.readLong()),
            receiveDate = Date(parcel.readLong()),
            shouldSign = boolConvert(parcel.readInt())
    )

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(body!!.size)
        parcel.writeByteArray(body)
        parcel.writeString(fromFingerprint)
        parcel.writeString(toFingerprint)
        parcel.writeString(application)
        parcel.writeString(extension)
        parcel.writeString(mime)
        parcel.writeString(filename)
        parcel.writeFileDescriptor(fileDescriptor!!.fileDescriptor)
        parcel.writeInt(boolConvert(toDisk))
        parcel.writeLong(sendDate.time)
        parcel.writeLong(receiveDate.time)
        parcel.writeInt(boolConvert(shouldSign))
    }

    data class Builder(
            private var body: ByteArray? = null,
            private var fromFingerprint: String? = null,
            private var toFingerprint: String? = null,
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
            private var shouldSign: Boolean = false
    ) {

        fun setBody(body: ByteArray?) = apply {
            this.body = body
            todisk = false
        }

        fun setTo(to: String?) = apply {
            toFingerprint = to
        }

        fun setFrom(from: String?) = apply {
            fromFingerprint = from
        }

        fun setApplication(application: String) = apply {
            this.application = application
        }

        fun setSendDate(sendDate: Date) = apply {
            this.sendDate = sendDate
        }

        fun enableSigning() = apply {
            this.shouldSign = true
        }

        fun setFile(file: File?, mode: Int) = apply {
            if (file != null) {
                try {
                    fileDescriptor = ParcelFileDescriptor.open(file, mode)
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
                    extension = extension!!,
                    mime = mime!!,
                    filename = filename,
                    fileDescriptor = fileDescriptor,
                    toDisk = todisk,
                    sendDate = sendDate,
                    receiveDate = receiveDate,
                    shouldSign = shouldSign
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