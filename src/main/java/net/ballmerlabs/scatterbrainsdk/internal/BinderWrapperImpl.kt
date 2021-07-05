package net.ballmerlabs.scatterbrainsdk.internal
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.os.Parcelable
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import net.ballmerlabs.scatterbrainsdk.*
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent)
        } else {
            context.startService(startIntent)
        }
    }

    override suspend fun unbindService() {
        binderProvider.unbindService()
    }


    private suspend fun registerResultUnit(res: Int) = suspendCoroutine<Unit>{ continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, _ ->
            continuation.resumeWith(Result.success(Unit))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
    }

    private suspend inline fun <reified T: Parcelable> registerResultParcelableArray(
            res: Int
    ) = suspendCoroutine<ArrayList<T>> { continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, bundle ->
            continuation.resumeWith(Result.success(
                    bundle.getParcelableArrayList(ScatterbrainApi.EXTRA_ASYNC_RESULT)!!
            ))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
    }

    private suspend fun registerResultByteArray(res: Int) = suspendCoroutine<ByteArray> { continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, bundle ->
            continuation.resumeWith(Result.success(
                    bundle.getByteArray(ScatterbrainApi.EXTRA_ASYNC_RESULT)!!
            ))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
    }

    private suspend fun registerResultStringArrayList(res: Int) = suspendCoroutine<ArrayList<String>> { continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, bundle ->
            continuation.resumeWith(Result.success(
                    bundle.getStringArrayList(ScatterbrainApi.EXTRA_ASYNC_RESULT)!!
            ))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
    }


    override suspend fun sign(identity: Identity, data: ByteArray): ByteArray {
        val res = binderProvider.getAsync().signDataDetachedAsync(data, ParcelUuid(identity.fingerprint))
        return registerResultByteArray(res)
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
        val res = binderProvider.getAsync().getByApplicationAsync(application)
        return registerResultParcelableArray(res)
    }


    override suspend fun getScatterMessages(application: String, since: Date): List<ScatterMessage> {
        return getScatterMessages(application, since, Date())
    }

    override suspend fun getScatterMessages(application: String, start: Date, end: Date): List<ScatterMessage> {
        val res = binderProvider.getAsync().getByApplicationDateAsync(application, start.time, end.time)
        return registerResultParcelableArray(res)
    }

    @ExperimentalCoroutinesApi
    override suspend fun observeMessages(application: String): Flow<List<ScatterMessage>> = callbackFlow  {
        var now = Date()
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.messages > 0) {
                offer(getScatterMessages(application, now))
                now = Date()
            }
        }

        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose { broadcastReceiver.removeOnReceiveCallback(callback) }
    }

    override suspend fun generateIdentity(name: String): Identity {
        return binderProvider.getAsync().generateIdentity(name)
    }

    override suspend fun authorizeIdentity(identity: Identity, packageName: String) {
        binderProvider.getAsync().authorizeApp(ParcelUuid(identity.fingerprint), packageName)
    }

    override suspend fun deauthorizeIdentity(identity: Identity, packageName: String) {
        Log.v(TAG, "deauthorizing $packageName")
        binderProvider.getAsync().deauthorizeApp(ParcelUuid(identity.fingerprint), packageName)
    }

    override suspend fun getPermissions(identity: Identity): Flow<List<NamePackage>> = flow {
        val identities = binderProvider.getAsync().getAppPermissions(ParcelUuid(identity.fingerprint))
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
        return registerResultUnit(res)
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>) {
        val res = binderProvider.getAsync().sendMessagesAsync(messages)
        return registerResultUnit(res)
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: UUID) {
        val res = binderProvider.getAsync().sendAndSignMessageAsync(message, ParcelUuid(identity))
        return registerResultUnit(res)
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: UUID) {
        val res = binderProvider.getAsync().sendAndSignMessagesAsync(messages, ParcelUuid(identity))
        return registerResultUnit(res)
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: Identity) {
        val res = binderProvider.getAsync().sendAndSignMessageAsync(message, ParcelUuid(identity.fingerprint))
        return registerResultUnit(res)
    }

    override suspend fun getPackages(): List<String> {
        val res = binderProvider.getAsync().knownPackagesAsync
        return registerResultStringArrayList(res)
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity) {
        return sendMessage(messages, identity.fingerprint)
    }
    override suspend fun removeIdentity(identity: Identity): Boolean {
        return binderProvider.getAsync().removeIdentity(ParcelUuid(identity.fingerprint))
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