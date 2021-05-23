package net.ballmerlabs.scatterbrainsdk.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.runBlocking
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScatterbrainBroadcastReceiverImpl @Inject constructor(
        val context: Context
) : BroadcastReceiver(), ScatterbrainBroadcastReceiver {
    private val intentFilter = IntentFilter(BROADCASST_MESSAGE)
    private val callbackSet = mutableSetOf<suspend (HandshakeResult) -> Unit>()

    override fun onReceive(ctx: Context, intent: Intent) {
        val handshakeResult = intent.getParcelableExtra<HandshakeResult>(ScatterbrainApi.EXTRA_TRANSACTION_RESULT)!!
        Log.e("debug", "received handshake result")
        callbackSet.forEach { h -> runBlocking {  h(handshakeResult) } }
    }

    override fun register() {
        context.registerReceiver(this, intentFilter)
    }

    override fun unregister() {
        context.unregisterReceiver(this)
    }

    override fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        callbackSet.add(r)
    }

    override fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        callbackSet.remove(r)
    }

    companion object {
        const val BROADCASST_MESSAGE = "net.ballmerlabs.scatterroutingservice.broadcast.NETWORK_EVENT"
    }

}