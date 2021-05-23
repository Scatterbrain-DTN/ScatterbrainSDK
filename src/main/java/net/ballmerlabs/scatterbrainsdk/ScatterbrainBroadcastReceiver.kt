package net.ballmerlabs.scatterbrainsdk

import net.ballmerlabs.scatterbrainsdk.internal.HandshakeResult

interface ScatterbrainBroadcastReceiver {
    fun register()
    fun unregister()
    fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
    fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit)
}