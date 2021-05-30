package net.ballmerlabs.scatterbrainsdk.internal
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import net.ballmerlabs.scatterbrainsdk.*
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@ExperimentalCoroutinesApi
@Singleton
class BinderWrapperImpl @Inject constructor(
        val context: Context,
        private val broadcastReceiver: ScatterbrainBroadcastReceiver,
        val binderProvider: BinderProvider
) : BinderWrapper  {
    
    override suspend fun startService() {
        val startIntent = Intent(BIND_ACTION)
        startIntent.`package` = BIND_PACKAGE
        context.startForegroundService(startIntent)
    }

    override suspend fun unbindService() {
        binderProvider.unbindService()
    }

    override suspend fun sign(identity: Identity, data: ByteArray): ByteArray {
        val res = binderProvider.getAsync().signDataDetachedAsync(data, identity.fingerprint)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, bundle ->
                val d = bundle.getByteArray(ScatterbrainApi.EXTRA_ASYNC_RESULT)
                continuation.resumeWith(Result.success(d!!))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun getIdentities(): List<Identity> {
        return binderProvider.getAsync().identities
    }

    override suspend fun stopService() {
        val stopIntent = Intent(BIND_ACTION)
        stopIntent.`package` = BIND_PACKAGE
        context.stopService(stopIntent)
    }

    @ExperimentalCoroutinesApi
    override suspend fun observeIdentities(): Flow<List<Identity>>  = callbackFlow {
        offer(getIdentities())
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.identities > 0) {
                offer(getIdentities())
            }
        }
        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose {
            broadcastReceiver.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun getScatterMessages(application: String): List<ScatterMessage> {
        return binderProvider.getAsync().getByApplication(application)
    }

    @ExperimentalCoroutinesApi
    override suspend fun observeMessages(application: String): Flow<List<ScatterMessage>> = callbackFlow  {
        offer(getScatterMessages(application))
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.messages > 0) {
                offer(getScatterMessages(application))
            }
        }

        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose {
            broadcastReceiver.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun generateIdentity(name: String): Identity {
        return binderProvider.getAsync().generateIdentity(name)
    }

    override suspend fun authorizeIdentity(identity: Identity, packageName: String) {
        binderProvider.getAsync().authorizeApp(identity.fingerprint, packageName)
    }

    override suspend fun deauthorizeIdentity(identity: Identity, packageName: String) {
        Log.v(TAG, "deauthorizing $packageName")
        binderProvider.getAsync().deauthorizeApp(identity.fingerprint, packageName)
    }

    override suspend fun getPermissions(identity: Identity): Flow<List<NamePackage>> = flow {
        val identities = binderProvider.getAsync().getAppPermissions(identity.fingerprint)
        val result = mutableListOf<NamePackage>()
        val pm = context.packageManager
        for (id in identities) {
            yield()
            val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
            result.add(NamePackage(pm.getApplicationLabel(r).toString(), r))
        }
        emit(result)
    }

    override suspend fun sendMessage(message: ScatterMessage) {
        val res = binderProvider.getAsync().sendMessageAsync(message)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, _ ->
                continuation.resumeWith(Result.success(Unit))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>) {
        val res = binderProvider.getAsync().sendMessagesAsync(messages)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, _ ->
                continuation.resumeWith(Result.success(Unit))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: String) {
        val res = binderProvider.getAsync().sendAndSignMessageAsync(message, identity)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, _ ->
                continuation.resumeWith(Result.success(Unit))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: String) {
        val res = binderProvider.getAsync().sendAndSignMessagesAsync(messages, identity)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, _ ->
                continuation.resumeWith(Result.success(Unit))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: Identity) {
        val res = binderProvider.getAsync().sendAndSignMessageAsync(message, identity.fingerprint)
        return suspendCoroutine { continuation ->
            broadcastReceiver.addOnResultCallback(res) { _, _ ->
                continuation.resumeWith(Result.success(Unit))
            }
            broadcastReceiver.addOnErrorCallback(res) { _, str ->
                continuation.resumeWith(Result.failure(IllegalStateException(str)))
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity) {
        return sendMessage(messages, identity.fingerprint)
    }
    override suspend fun removeIdentity(identity: Identity): Boolean {
        return binderProvider.getAsync().removeIdentity(identity.fingerprint)
    }

    override suspend fun startDiscover() {
        binderProvider.getAsync().startDiscovery()
    }

    override suspend fun startPassive() {
        binderProvider.getAsync().startPassive()
    }

    override suspend fun stopDiscover() {
        binderProvider.getAsync().stopDiscovery()
    }

    override suspend fun stopPassive() {
        binderProvider.getAsync().stopPassive()
    }

    override fun register() {
        broadcastReceiver.register()
    }

    override fun unregister() {
        broadcastReceiver.unregister()
    }

    override fun isConnected(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(1)) {
            if ("net.ballmerlabs.uscatterbrain.ScatterRoutingService" == service.service.className) {
                return true
            }
        }
        return false
    }

    init {
        Log.v(TAG, "init called")
    }
}