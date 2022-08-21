package net.ballmerlabs.scatterbrainsdk.internal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import net.ballmerlabs.scatterbrainsdk.BinderProvider
import net.ballmerlabs.scatterbrainsdk.BinderProvider.Companion.mapBinderState
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBinderApi
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BinderProviderImpl @Inject constructor(
        val context: Context
): BinderProvider {

    private var binder: ScatterbrainBinderApi? = null
    private val connectionLiveData = MutableLiveData(BinderWrapper.Companion.BinderState.STATE_DISCONNECTED)

    private val callback = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = ScatterbrainBinderApi.Stub.asInterface(service)
            Log.v(BinderWrapper.TAG, "connected to ScatterRoutingService binder")
            startConnected()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.v(BinderWrapper.TAG, "onservicedisconnected")
            binder = null
            startDisconnected()
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            binder = null
            startDisconnected()
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            binder = null
            startDisconnected()
        }
    }

    private fun startConnected() {
        if (connectionLiveData.value != BinderWrapper.Companion.BinderState.STATE_CONNECTED) {
            connectionLiveData.postValue(BinderWrapper.Companion.BinderState.STATE_CONNECTED)
        }
    }

    private fun startDisconnected() {
        if(connectionLiveData.value != BinderWrapper.Companion.BinderState.STATE_DISCONNECTED) {
            connectionLiveData.postValue(BinderWrapper.Companion.BinderState.STATE_DISCONNECTED)
        }
    }

    private suspend fun bindServiceWithoutTimeout(): Unit = suspendCancellableCoroutine { ret ->
        if (binder == null) {
            val bindIntent = Intent(BinderWrapper.BIND_ACTION)
            bindIntent.`package` = BinderWrapper.BIND_PACKAGE
            context.bindService(bindIntent, callback, 0)
        } else {
            ret.resume(Unit)
        }

    }

    override fun getConnectionLivedata(): LiveData<BinderWrapper.Companion.BinderState> {
        return connectionLiveData
    }

    private suspend fun bindService(timeout: Long): ScatterbrainBinderApi {
        withTimeout(timeout) {
            bindServiceWithoutTimeout()
        }
        return binder!!
    }

    override fun isConnected(): Boolean {
        return connectionLiveData.value == BinderWrapper.Companion.BinderState.STATE_CONNECTED
    }

    override suspend fun unbindService(): Boolean = suspendCoroutine { ret ->
        try {
            if (binder != null) {
                context.unbindService(callback)
            }
            ret.resume(true)
        } catch (e: IllegalArgumentException) {
            startDisconnected()
            ret.resume(true) //service already unbound
        }
    }


    override fun get(): ScatterbrainBinderApi {
        return runBlocking {
            bindService(5000L)
        }
    }

    override suspend fun getAsync(timeout: Long): ScatterbrainBinderApi {
        return bindService(timeout)
    }
}