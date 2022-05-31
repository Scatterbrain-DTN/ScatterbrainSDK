package net.ballmerlabs.scatterbrainsdk

import android.content.ComponentName
import android.content.Context
import android.os.Parcelable
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import kotlinx.parcelize.Parcelize
import net.ballmerlabs.scatterbrainsdk.internal.DaggerSdkComponent
import net.ballmerlabs.scatterbrainsdk.internal.SdkComponent
import java.io.File
import java.util.*

@Parcelize
enum class RouterState(val state: String) : Parcelable {
    DISCOVERING("Discovering"),
    OFFLINE("Offline"),
    ERROR("Error")
}

class ScatterbrainApi(applicationContext: Context?) {
    private val sdkComponent: SdkComponent?
    val binderWrapper: BinderWrapper
        get() = sdkComponent!!.sdk()
    val broadcastReceiver: ScatterbrainBroadcastReceiver
        get() = sdkComponent!!.broadcastReceiver()

    @Throws(Throwable::class)
    protected fun finalize() {
        sdkComponent!!.broadcastReceiver().unregister()
    }

    companion object {
        const val MAX_BODY_SIZE = 1024 * 1019 // Binders have 1MB size limit, leave 5kb for metadata
        const val EXTRA_TRANSACTION_RESULT = "transaction_result"
        const val PROTOBUF_PRIVKEY_KEY = "scatterbrain"
        const val EXTRA_ASYNC_RESULT = "async_result"
        const val EXTRA_ASYNC_HANDLE = "async_handle"
        const val PACKAGE_NAME = "net.ballmerlabs.scatterroutingservice"
        const val PERMISSION_ACCESS = "net.ballmerlabs.scatterroutingservice.permission.ACCESS"
        const val PERMISSION_ADMIN = "net.ballmerlabs.scatterroutingservice.permission.ADMIN"
        const val PERMISSION_SUPERUSER = "net.ballmerlabs.scatterroutingservice.permission.SUPERUSER"
        const val DEFAULT_MIME = "application/octet-stream"
        const val BROADCAST_EVENT = "net.ballmerlabs.scatterroutingservice.broadcast.NETWORK_EVENT"
        const val STATE_EVENT = "net.ballmerlabs.scatterroutingservice.broadcast.ROUTER_STATE"
        const val IMPORT_IDENTITY_ACTION = "net.ballmerlabs.scatterroutingservice.IMPORT_IDENTITY_ACTION"
        const val EXTRA_IDENTITY_RESULT = "net.ballmerlabs.scatterroutingservice.EXTRA_IDENTITY_RESULT"
        const val EXTRA_ROUTER_STATE = "net.ballmerlabs.scatterroutingservice.EXTRA_ROUTER_STATE"
        const val EXTRA_NUM_IDENTITIES = "net.ballmerlabs.scatterroutingservice.EXTRA_NUM_IDENTITIES"
        val IMPORT_IDENTITY_COMPONENT = ComponentName(PACKAGE_NAME, PACKAGE_NAME + ".ui.identity.IdentityImportActivity")
        fun getMimeType(file: File): String {
            return if (file.isDirectory) {
                DocumentsContract.Document.MIME_TYPE_DIR
            } else {
                val name = file.name
                val lastDot = name.lastIndexOf('.')
                if (lastDot >= 0) {
                    val extension = name.substring(lastDot + 1).lowercase(Locale.getDefault())
                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    if (mime != null) return mime
                }
                "application/octet-stream"
            }
        }
    }

    init {
        sdkComponent = DaggerSdkComponent.builder().applicationContext(applicationContext!!)!!.build()
    }
}