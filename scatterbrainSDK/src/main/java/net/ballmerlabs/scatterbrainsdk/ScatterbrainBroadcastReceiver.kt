package net.ballmerlabs.scatterbrainsdk

import androidx.lifecycle.LiveData
import net.ballmerlabs.scatterbrainsdk.internal.AsyncCallback

interface ScatterbrainBroadcastReceiver {
    fun observeRouterState(): LiveData<RouterState>
    fun register()
    fun unregister()
    fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun addOnResultCallback(handle: Int, r: AsyncCallback)
    fun removeOnResultCallback(handle: Int)
    fun wipeAsyncCallbacks()
    fun wipeResultCallbacks()
    fun postRouterState(routerState: RouterState)
}