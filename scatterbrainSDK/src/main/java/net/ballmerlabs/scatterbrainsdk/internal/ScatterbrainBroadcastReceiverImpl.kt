package net.ballmerlabs.scatterbrainsdk.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.ballmerlabs.scatterbrainsdk.HandshakeResult
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.BROADCAST_EVENT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.EXTRA_TRANSACTION_RESULT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class AsyncCallback(
        val result: suspend (Int, Bundle) -> Unit,
        val err: suspend (Int, String) -> Unit
)

@Singleton
class ScatterbrainBroadcastReceiverImpl @Inject constructor(): BroadcastReceiver(), ScatterbrainBroadcastReceiver {
    private val intentFilter = IntentFilter()
            .apply {
                addAction(BROADCAST_EVENT)
            }
    private val eventCallbackSet = mutableSetOf<suspend (HandshakeResult) -> Unit>()
    private val resultCallbackSet = ConcurrentHashMap<Int, AsyncCallback>()
    @Inject lateinit var context: Context
    @Named(SCOPE_DEFAULT) @Inject lateinit var coroutineScope: CoroutineScope

    override fun onReceive(ctx: Context, intent: Intent) {
        Log.v(TAG, "onReceive")
        coroutineScope.launch {
            when (intent.action) {
                BROADCAST_EVENT -> {
                    val handshakeResult = intent.getParcelableExtra<HandshakeResult>(EXTRA_TRANSACTION_RESULT)!!
                    Log.e("debug", "received handshake result")


                    eventCallbackSet.forEach { h -> h(handshakeResult) }
                }

                else -> Log.e(TAG, "invalid action ${intent.action}")
            }
        }
    }

    override fun register() {
        context.registerReceiver(this, intentFilter)
    }

    override fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "failed to unregister receiver")
        }
    }

    override fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        eventCallbackSet.add(r)
    }

    override fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        eventCallbackSet.remove(r)
    }

    override fun addOnResultCallback(handle: Int, r: AsyncCallback) {
        resultCallbackSet[handle] = r
    }



    override fun removeOnResultCallback(handle: Int) {
        resultCallbackSet.remove(handle)
    }

    override fun wipeAsyncCallbacks() {
        resultCallbackSet.clear()
    }

    override fun wipeResultCallbacks() {
        resultCallbackSet.clear()
    }

    companion object {
        const val TAG = "BroadcastReceiver"
    }

}