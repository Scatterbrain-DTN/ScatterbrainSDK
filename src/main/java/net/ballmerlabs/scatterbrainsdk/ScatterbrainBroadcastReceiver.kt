package net.ballmerlabs.scatterbrainsdk

import android.os.Bundle
import net.ballmerlabs.scatterbrainsdk.internal.AsyncCallback
import net.ballmerlabs.scatterbrainsdk.internal.HandshakeResult

interface ScatterbrainBroadcastReceiver {
    fun register()
    fun unregister()
    fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun addOnResultCallback(handle: Int, r: AsyncCallback)
    fun removeOnResultCallback(handle: Int)
    fun wipeAsyncCallbacks()
    fun wipeResultCallbacks()
}