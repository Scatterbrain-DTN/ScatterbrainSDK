package net.ballmerlabs.scatterbrainsdk.internal

import android.os.IBinder
import net.ballmerlabs.scatterbrainsdk.BinderProvider
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBinderApi

class MockBinderProvider(
        val iBinder: IBinder
) : BinderProvider {
    val binder = ScatterbrainBinderApi.Stub.asInterface(iBinder)
    override suspend fun getAsync(): ScatterbrainBinderApi {
        return binder
    }

    override fun unregisterCallback() {
        //TODO: ignored
    }

    override suspend fun unbindService(): Boolean {
        //TODO: ignored
        return true
    }

    override fun get(): ScatterbrainBinderApi {
        return binder
    }

}
