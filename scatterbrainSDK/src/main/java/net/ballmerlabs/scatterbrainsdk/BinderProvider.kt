package net.ballmerlabs.scatterbrainsdk

import androidx.lifecycle.LiveData
import javax.inject.Provider

interface BinderProvider : Provider<ScatterbrainBinderApi> {
    suspend fun getAsync(timeout: Long = 5000L): ScatterbrainBinderApi
    suspend fun unbindService(): Boolean
    fun isConnected(): Boolean
    fun getConnectionLivedata(): LiveData<BinderWrapper.Companion.BinderState>
    companion object {
        fun mapBinderState(boolean: Boolean): BinderWrapper.Companion.BinderState {
            return if (boolean)
                BinderWrapper.Companion.BinderState.STATE_CONNECTED
            else
                BinderWrapper.Companion.BinderState.STATE_DISCONNECTED

        }
    }
}