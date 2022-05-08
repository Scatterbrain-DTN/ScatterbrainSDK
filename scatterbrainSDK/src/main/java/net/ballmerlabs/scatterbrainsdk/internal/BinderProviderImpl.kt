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
    private val connectionLiveData = MutableLiveData(mapBinderState(false))

    private val callback = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = ScatterbrainBinderApi.Stub.asInterface(service)
            Log.v(BinderWrapper.TAG, "connected to ScatterRoutingService binder")
            connectionLiveData.postValue(mapBinderState(true))
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.v(BinderWrapper.TAG, "onservicedisconnected")
            binder = null
            connectionLiveData.postValue(mapBinderState(false))
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            binder = null
            connectionLiveData.postValue(mapBinderState(false))
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            binder = null
            connectionLiveData.postValue(mapBinderState(false))
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

    private suspend fun bindService(): ScatterbrainBinderApi {
        withTimeout(1000L) {
            bindServiceWithoutTimeout()
        }
        return binder!!
    }

    override fun isConnected(): Boolean {
        val res = binder != null
        connectionLiveData.postValue(mapBinderState(res))
        return res
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


    override fun get(): ScatterbrainBinderApi {
        return runBlocking {
            bindService()
        }
    }

    override suspend fun getAsync(): ScatterbrainBinderApi {
        return bindService()
    }
}