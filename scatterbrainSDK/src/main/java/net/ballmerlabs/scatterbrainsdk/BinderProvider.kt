package net.ballmerlabs.scatterbrainsdk

import javax.inject.Provider

interface BinderProvider : Provider<ScatterbrainBinderApi> {
    suspend fun getAsync(): ScatterbrainBinderApi
    fun unregisterCallback()
    suspend fun unbindService(): Boolean
}