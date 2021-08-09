package net.ballmerlabs.scatterbrainsdk.internal

import net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI
import javax.inject.Provider

interface BinderProvider : Provider<ScatterbrainAPI> {
    suspend fun getAsync(): ScatterbrainAPI
    fun unregisterCallback()
    suspend fun unbindService(): Boolean
}