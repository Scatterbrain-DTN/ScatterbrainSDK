package net.ballmerlabs.scatterbrainsdk.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ScatterbrainBroadcastReceiverImpl @Inject constructor(
        val context: Context,
        @Named(SCOPE_DEFAULT) val coroutineScope: CoroutineScope
) : BroadcastReceiver(), ScatterbrainBroadcastReceiver {
    private val intentFilter = IntentFilter()
            .apply {
                addAction(BROADCAST_EVENT)
                addAction(BROADCAST_RESULT)
                addAction(BROADCAST_ERROR)
            }
    private val eventCallbackSet = mutableSetOf<suspend (HandshakeResult) -> Unit>()
    private val resultCallbackSet = ConcurrentHashMap<Int, suspend (Int, Bundle) -> Unit>()
    private val errorCallbackSet = ConcurrentHashMap<Int, suspend (Int, String) -> Unit>()

    override fun onReceive(ctx: Context, intent: Intent) {
        Log.v(TAG, "onReceive")
        coroutineScope.launch {
            when (intent.action) {
                BROADCAST_EVENT -> {
                    val handshakeResult = intent.getParcelableExtra<HandshakeResult>(ScatterbrainApi.EXTRA_TRANSACTION_RESULT)!!
                    Log.e("debug", "received handshake result")


                    eventCallbackSet.forEach { h -> h(handshakeResult) }
                }
                BROADCAST_RESULT -> {
                    resultCallbackSet.forEach { (key, value) ->
                        yield()
                        if (key == intent.getIntExtra(ScatterbrainApi.EXTRA_ASYNC_HANDLE, -1)) {
                            value(
                                    key,
                                    intent.getBundleExtra(ScatterbrainApi.EXTRA_ASYNC_RESULT)
                                            ?: Bundle.EMPTY
                            )
                            resultCallbackSet.remove(key)
                        }
                    }
                }
                BROADCAST_ERROR -> {
                    errorCallbackSet.forEach { (key, value) ->
                        yield()
                        if (key == intent.getIntExtra(ScatterbrainApi.EXTRA_ASYNC_HANDLE, -1)) {
                            value(
                                    key,
                                    intent.getStringExtra(ScatterbrainApi.EXTRA_ASYNC_RESULT)?: ""
                            )
                            errorCallbackSet.remove(key)
                        }
                    }
                }
                else -> Log.e(TAG, "invalid action ${intent.action}")
            }
        }
    }

    override fun register() {
        context.registerReceiver(this, intentFilter)
    }

    override fun unregister() {
        context.unregisterReceiver(this)
    }

    override fun addOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        eventCallbackSet.add(r)
    }

    override fun removeOnReceiveCallback(r: suspend (HandshakeResult) -> Unit) {
        eventCallbackSet.remove(r)
    }

    override fun addOnResultCallback(handle: Int, r: suspend (Int, Bundle) -> Unit) {
        resultCallbackSet[handle] = r
    }

    override fun addOnErrorCallback(handle: Int, r: suspend (Int, String) -> Unit) {
        errorCallbackSet[handle] = r
    }

    override fun removeOnErrorCallback(handle: Int) {
        errorCallbackSet.remove(handle)
    }

    override fun removeOnResultCallback(handle: Int) {
        resultCallbackSet.remove(handle)
    }

    override fun wipeAsyncCallbacks() {
        resultCallbackSet.clear()
        errorCallbackSet.clear()
    }

    override fun wipeResultCallbacks() {
        resultCallbackSet.clear()
    }

    companion object {
        const val TAG = "BroadcastReceiver"
        const val BROADCAST_EVENT = "net.ballmerlabs.scatterroutingservice.broadcast.NETWORK_EVENT"
        const val BROADCAST_RESULT = "net.ballmerlabs.scatterroutingservice.broadcast.API_RESULT"
        const val BROADCAST_ERROR = "net.ballmerlabs.scatterroutingservice.broadcast.API"
    }

}