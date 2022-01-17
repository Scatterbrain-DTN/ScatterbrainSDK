package net.ballmerlabs.scatterbrainsdk

import androidx.lifecycle.LiveData
import javax.inject.Provider

interface BinderProvider : Provider<ScatterbrainBinderApi> {
    suspend fun getAsync(): ScatterbrainBinderApi
    fun unregisterCallback()
    suspend fun unbindService(): Boolean
    fun getConnectionLivedata(): LiveData<BinderWrapper.Companion.BinderState>
}