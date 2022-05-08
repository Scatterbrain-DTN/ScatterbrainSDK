package net.ballmerlabs.scatterbrainsdk.internal

import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.ballmerlabs.scatterbrainsdk.BinderProvider
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBinderApi

class MockBinderProvider(
        val iBinder: IBinder
) : BinderProvider {
    val binder: ScatterbrainBinderApi = ScatterbrainBinderApi.Stub.asInterface(iBinder)
    private val testLiveData = MutableLiveData<BinderWrapper.Companion.BinderState>()
    override suspend fun getAsync(timeout: Long): ScatterbrainBinderApi {
        return binder
    }

    override suspend fun unbindService(): Boolean {
        //TODO: ignored
        return true
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun getConnectionLivedata(): LiveData<BinderWrapper.Companion.BinderState> {
        return testLiveData
    }

    override fun get(): ScatterbrainBinderApi {
        return binder
    }

}
