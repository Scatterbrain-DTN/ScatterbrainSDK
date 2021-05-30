package net.ballmerlabs.scatterbrainsdk.internal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BinderProviderImpl @Inject constructor(
        val context: Context
): BinderProvider {

    private val bindCallbackSet: MutableSet<(Boolean?) -> Unit> = mutableSetOf()
    private var binder: ScatterbrainAPI? = null


    private val callback = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = ScatterbrainAPI.Stub.asInterface(service)
            Log.v(BinderWrapper.TAG, "connected to ScatterRoutingService binder")
            try {
                bindCallbackSet.forEach { c ->  c(true)}
            } catch (e: RemoteException) {
                Log.e(BinderWrapper.TAG, "RemoteException: $e")
                bindCallbackSet.forEach { c -> c(null) }
            } finally {
                bindCallbackSet.clear()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.v(BinderWrapper.TAG, "onservicedisconnected")
            binder = null
            bindCallbackSet.forEach { c -> c(false) }
            bindCallbackSet.clear()
        }
    }

    fun registerCallback() {
        bindCallbackSet.forEach { registerCallback(it) }
    }

    override fun unregisterCallback() {
        bindCallbackSet.forEach { unregisterCallback(it) }
    }

    private fun registerCallback(c: (Boolean?) -> Unit) {
        bindCallbackSet.add(c)
    }

    private fun unregisterCallback(c: (Boolean?) -> Unit) {
        bindCallbackSet.remove(c)
    }

    private suspend fun bindServiceWithoutTimeout(): Unit = suspendCoroutine { ret ->
        if (binder == null) {
            registerCallback { b ->
                if (b == null || b == false) throw IllegalStateException("failed to bind service")
                ret.resume(Unit)
            }
            val bindIntent = Intent(BinderWrapper.BIND_ACTION)
            bindIntent.`package` = BinderWrapper.BIND_PACKAGE
            context.bindService(bindIntent, callback, 0)
        } else {
            ret.resume(Unit)
        }
    }


    private suspend fun bindService(): ScatterbrainAPI {
        withTimeout(5000L) {
            bindServiceWithoutTimeout()
        }
        return binder!!
    }


    override suspend fun unbindService(): Boolean = suspendCoroutine { ret ->
        try {
            if (binder != null) {
                context.unbindService(callback)
            }
            ret.resume(true)
        } catch (e: IllegalArgumentException) {
            ret.resume(true) //service already unbound
        }
    }


    override fun get(): ScatterbrainAPI {
        return runBlocking {
            bindService()
        }
    }

    override suspend fun getAsync(): ScatterbrainAPI {
        return bindService()
    }
}