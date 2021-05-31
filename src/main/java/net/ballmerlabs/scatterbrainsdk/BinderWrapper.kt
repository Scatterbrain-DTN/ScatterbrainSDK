package net.ballmerlabs.scatterbrainsdk

import android.content.pm.ApplicationInfo
import kotlinx.coroutines.flow.Flow

interface BinderWrapper {
    suspend fun startService()
    suspend fun stopService()
    suspend fun unbindService()
    suspend fun getIdentities(): List<Identity>
    suspend fun getScatterMessages(application: String): List<ScatterMessage>
    suspend fun observeIdentities(): Flow<List<Identity>>
    suspend fun observeMessages(application: String): Flow<List<ScatterMessage>>
    suspend fun generateIdentity(name: String): Identity
    suspend fun getPermissions(identity: Identity): Flow<List<NamePackage>>
    suspend fun authorizeIdentity(identity: Identity, packageName: String)
    suspend fun deauthorizeIdentity(identity: Identity, packageName: String)
    suspend fun removeIdentity(identity: Identity): Boolean
    suspend fun sign(identity: Identity, data: ByteArray): ByteArray
    suspend fun startDiscover()
    suspend fun stopDiscover()
    suspend fun startPassive()
    suspend fun stopPassive()
    suspend fun sendMessage(message: ScatterMessage)
    suspend fun sendMessage(message: ScatterMessage, identity: Identity)
    suspend fun sendMessage(message: ScatterMessage, identity: String)
    suspend fun sendMessage(messages: List<ScatterMessage>, identity: String)
    suspend fun sendMessage(messages: List<ScatterMessage>)
    suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity)
    suspend fun getPackages(): List<String>
    fun register()
    fun unregister()
    fun isConnected(): Boolean
    companion object {
        val TAG = "ServiceConnectionRepository"
        const val BIND_ACTION = "net.ballmerlabs.uscatterbrain.ScatterRoutingService.BIND"
        const val BIND_PACKAGE = "net.ballmerlabs.scatterroutingservice"
    }
}


class NamePackage(
        val name: String,
        val info: ApplicationInfo
) {
    override fun toString(): String {
        return name
    }
}