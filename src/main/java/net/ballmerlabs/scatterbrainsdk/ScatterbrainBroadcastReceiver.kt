package net.ballmerlabs.scatterbrainsdk

import android.os.Bundle
import net.ballmerlabs.scatterbrainsdk.internal.HandshakeResult

interface ScatterbrainBroadcastReceiver {
    fun register()
    fun unregister()
    fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun addOnResultCallback(handle: Int, r: suspend (Int, Bundle) -> Unit)
    fun addOnErrorCallback(handle: Int, r: suspend (Int, String) -> Unit)
    fun removeOnResultCallback(handle: Int)
    fun removeOnErrorCallback(handle: Int)
    fun wipeAsyncCallbacks()
    fun wipeResultCallbacks()
}