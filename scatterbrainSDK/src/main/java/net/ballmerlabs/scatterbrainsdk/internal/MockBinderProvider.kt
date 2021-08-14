package net.ballmerlabs.scatterbrainsdk.internal

import android.os.IBinder
import net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI

class MockBinderProvider(
        val iBinder: IBinder
) : BinderProvider {
    val binder = ScatterbrainAPI.Stub.asInterface(iBinder)
    override suspend fun getAsync(): ScatterbrainAPI {
        return binder
    }

    override fun unregisterCallback() {
        //TODO: ignored
    }

    override suspend fun unbindService(): Boolean {
        //TODO: ignored
        return true
    }

    override fun get(): ScatterbrainAPI {
        return binder
    }

}
