package net.ballmerlabs.scatterbrainsdk

import android.net.Uri
import android.os.*
import android.webkit.MimeTypeMap
import net.ballmerlabs.scatterbrainsdk.ScatterMessage.Builder
import java.io.File
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.util.*

private fun validateBody(size: Int): Int {
    if (size > ScatterbrainApi.MAX_BODY_SIZE) {
        throw BadParcelableException("invalid array size")
    }
    return size
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

/**
 * Represents a messages sent or received via Scatterbrain.
 * @property Builder builder class to construct an instance of ScatterMessage
 * @property shm contents of message, null if message is a file
 * @property fromFingerprint identity fingerprint of sender, null if anonymous
 * @property toFingerprint identity fingerprint of recipient. Currently unused
 * @property application name of application this message belongs to
 * @property extension file extension of this message. Autogenerated if message is not a file
 * @property mime mime type for this message
 * @property filename name of file on disk, null if not a file message
 * @property sendDate timestamp when this message was created
 * @property receiveDate timestamp when this message was received by Scatterbrain
 * @property isFile if this message contains a file descriptor, if false message contains inline bytes
 * @property id a unique id referring to this message, valid within the local router only
 */
class ScatterMessage private constructor(
        val shm: SharedMemory?,
        val fromFingerprint: UUID?,
        val toFingerprint: UUID?,
        val application: String,
        val extension: String,
        val mime: String,
        val filename: String,
        val sendDate: Date,
        val receiveDate: Date,
        val fileDescriptor: ParcelFileDescriptor?,
        val isFile: Boolean,
        val id: ParcelUuid
): Parcelable {

    val body by lazy {
        if (shm == null) {
            null
        } else {
            val buf = shm.mapReadOnly()
            val bytes = ByteArray(buf.remaining())
            buf.get(bytes)
            shm.close()
            buf
        }
    }

    private constructor(parcel: Parcel): this(
            shm = parcel.readParcelable(SharedMemory::class.java.classLoader),
            fromFingerprint = parcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)?.uuid,
            toFingerprint = parcel.readParcelable<ParcelUuid>(ParcelUuid::class.java.classLoader)?.uuid,
            application = parcel.readString()!!,
            extension = parcel.readString()!!,
            mime =  parcel.readString()!!,
            filename =  parcel.readString()!!,
            fileDescriptor = parcel.readParcelable(ParcelFileDescriptor::class.java.classLoader),
            isFile = boolConvert(parcel.readInt()),
            sendDate = Date(parcel.readLong()),
            receiveDate = Date(parcel.readLong()),
            id = parcel.readParcelable(ParcelUuid::class.java.classLoader)!!
    )

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeParcelable(shm, i)
        parcel.writeParcelable(if (fromFingerprint == null) null else ParcelUuid(fromFingerprint), i)
        parcel.writeParcelable(if (toFingerprint == null) null else ParcelUuid(toFingerprint), i)
        parcel.writeString(application)
        parcel.writeString(extension)
        parcel.writeString(mime)
        parcel.writeString(filename)
        parcel.writeParcelable(fileDescriptor, i)
        parcel.writeInt(boolConvert(isFile))
        parcel.writeLong(sendDate.time)
        parcel.writeLong(receiveDate.time)
        parcel.writeParcelable(id, i)
        shm?.close()
    }


    protected fun finalize() {
        shm?.close()
    }

    /**
     * Builder class used to construct a ScatterMessage
     *
     */
    open class Builder protected constructor(
            private var shm: SharedMemory? = null,
            protected var fromFingerprint: UUID? = null,
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
        protected fun setShm(body: SharedMemory?) = apply {
            this.shm = body
            todisk = false
        }

        /**
         * sets the recipient fingerprint
         * @param to identity fingerprint for recipient
         */
        fun setTo(to: UUID?) = apply {
            toFingerprint = to
        }

        /**
         * sets the application identifier for this message. This is used for filtering
         * messages
         * @param application the application identifier
         */
        fun setApplication(application: String) = apply {
            this.application = application
        }

        protected fun setFile(file: File) = apply {
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

        protected fun setFile(descriptor: ParcelFileDescriptor, ext: String, mime: String, name: String) = apply {
            fileDescriptor = descriptor
            this.extension = ext
            this.mime = mime
            filename = name
            todisk = true
        }

        private fun verify() {
            require(!(shm != null && fileDescriptor != null)) { "must set one of body or file" }
            require(!(shm == null && fileDescriptor == null)) { "set either body or file" }
            requireNotNull(application) { "applicaiton must be set" }
            check(!fileNotFound) { "file not found" }
        }

        /**
         * Builds a ScatterMessage instance
         * @return ScatterMessage object
         * @throws IllegalArgumentException if configuration is invalid
         * @throws NullPointerException if values are skipped
         */
        fun build(): ScatterMessage {
            verify()
            return ScatterMessage(
                    shm = shm,
                    toFingerprint = toFingerprint,
                    fromFingerprint = fromFingerprint,
                    application = application!!,
                    extension = extension?:"",
                    mime = mime?: ScatterbrainApi.DEFAULT_MIME,
                    filename = filename?:UUID.randomUUID().toString(),
                    fileDescriptor = fileDescriptor,
                    isFile = todisk,
                    sendDate = sendDate,
                    receiveDate = receiveDate,
                    id = id?: ParcelUuid(UUID.randomUUID())
            )
        }

        init {
            fingerprint = ""
        }

        companion object {
            /**
             * creates a new Builder instance using an inline byte array without a file descriptor
             * @param data payload for this message
             * @return builder class
             */
            fun newInstance(data: ByteArray): Builder {
                val shared = SharedMemory.create("scatterMessage", data.size)
                val buf = shared.mapReadWrite()
                buf.put(data)
                return Builder().setShm(shared)
            }

            /**
             * creates a new Builder instance using a file. Files are copied into the
             * Scatterbrain datastore when this messsage is inserted
             * @param file file for this message
             * @return builder class
             */
            fun newInstance(file: File): Builder {
                return Builder().setFile(file)
            }

            /**
             * creates a new Builder instance using a file. Files are copied into the
             * Scatterbrain datastore when this messsage is inserted
             * @param descriptor file for this message
             * @param ext file extension
             * @param mime mime type
             * @param name filename for file
             * @return builder class
             */
            fun newInstance(descriptor: FileDescriptor, ext: String, mime: String, name: String): Builder {
                return Builder().setFile(ParcelFileDescriptor.dup(descriptor), ext, mime, name)
            }


        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ScatterMessage?> = object : Parcelable.Creator<ScatterMessage?> {
            override fun createFromParcel(`in`: Parcel): ScatterMessage {
                return ScatterMessage(`in`)
            }

            override fun newArray(size: Int): Array<ScatterMessage?> {
                return arrayOfNulls(size)
            }
        }
    }
}