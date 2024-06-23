package net.ballmerlabs.scatterbrainsdk.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ballmerlabs.scatterbrainsdk.HandshakeResult
import net.ballmerlabs.scatterbrainsdk.PairingState
import net.ballmerlabs.scatterbrainsdk.RouterState
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.BROADCAST_EVENT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.EXTRA_LUID
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.EXTRA_ROUTER_STATE
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.EXTRA_TRANSACTION_RESULT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.PAIRING_EVENT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainApi.Companion.STATE_EVENT
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver.Companion.EXTRA_PAIRING_STATE
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class Handlers(
    val handshakeResult: MutableLiveData<HandshakeResult> = MutableLiveData(),
    val luidState: MutableLiveData<ParcelUuid> = MutableLiveData(),
    val desktopPairing: MutableLiveData<PairingState> = MutableLiveData(),
    val routerState: MutableLiveData<RouterState> = MutableLiveData(),
    val callbacks: ConcurrentHashMap<suspend (HandshakeResult) -> Unit, Boolean> = ConcurrentHashMap(),
) {
    fun addOnReceiveCallback(func: suspend (HandshakeResult) -> Unit) {
        callbacks[func] = true
    }

    fun removeOnReceiveCallback(func: suspend (HandshakeResult) -> Unit) {
        callbacks.remove(func)
    }
}

val handlers = ConcurrentHashMap<Handlers, Boolean>()


@Singleton
class ScatterbrainBroadcastReceiverImpl @Inject constructor() : BroadcastReceiver(),
    ScatterbrainBroadcastReceiver {
    private val intentFilter = IntentFilter()
        .apply {
            addAction(BROADCAST_EVENT)
            addAction(STATE_EVENT)
            addAction(PAIRING_EVENT)
        }
    @Inject
    lateinit var context: Context
    @Named(SCOPE_DEFAULT)
    @Inject
    lateinit var coroutineScope: CoroutineScope

    override fun onReceive(ctx: Context, intent: Intent) {
        Log.v(TAG, "onReceive")
        try {
            handlers.forEach { (handler, _) ->
                when (intent.action) {
                    BROADCAST_EVENT -> {
                        val handshakeResult =
                            intent.getParcelableExtra<HandshakeResult>(
                                EXTRA_TRANSACTION_RESULT
                            )
                        val luid = intent.getParcelableExtra<ParcelUuid>(EXTRA_LUID)
                        if (handshakeResult != null) {
                            Log.v(
                                "debug",
                                "handshakeResult ${handshakeResult.metrics.size}"
                            )
                            handler.handshakeResult.postValue(handshakeResult!!)
                        }
                        if (luid != null) {
                            handler.luidState.postValue(luid!!)
                        }
                    }

                    STATE_EVENT -> {
                        val state =
                            intent.getParcelableExtra<RouterState>(EXTRA_ROUTER_STATE)
                        if (state != null) {
                            handler.routerState.postValue(state)
                        }
                    }

                    PAIRING_EVENT -> {
                        val state =
                            intent.getParcelableExtra<PairingState>(EXTRA_PAIRING_STATE)
                        if (state != null) {
                            handler.desktopPairing.postValue(state)
                        }
                    }

                    else -> Log.e(TAG, "invalid action ${intent.action}")
                }
            }
        } catch (exc: Exception) {
            Log.w(TAG, "exception in ScatterbrainBroadcastReceiver onReceive: $exc")
        }
    }

    override fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, intentFilter, RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(this, intentFilter)
        }
    }

    override fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "failed to unregister receiver")
        }
    }

    companion object {
        const val TAG = "BroadcastReceiver"
    }

}