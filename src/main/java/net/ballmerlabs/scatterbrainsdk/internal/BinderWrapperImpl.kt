package net.ballmerlabs.scatterbrainsdk.internal
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
import kotlinx.coroutines.suspendCancellableCoroutine
import net.ballmerlabs.scatterbrainsdk.*
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


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


    private suspend fun registerResultUnit(res: Int) = suspendCancellableCoroutine<Unit>{ continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, _ ->
            continuation.resumeWith(Result.success(Unit))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
        continuation.invokeOnCancellation { broadcastReceiver.removeOnResultCallback(res) }
    }

    private suspend inline fun <reified T: Parcelable> registerResultParcelableArray(
            res: Int
    ) = suspendCancellableCoroutine<ArrayList<T>> { continuation ->
        val result: suspend (Int, Bundle) -> Unit = { _, bundle ->
            continuation.resumeWith(Result.success(
                    bundle.getParcelableArrayList(ScatterbrainApi.EXTRA_ASYNC_RESULT)!!
            ))
        }
        val err: suspend (Int, String) -> Unit = { _, str ->
            continuation.resumeWith(Result.failure(IllegalStateException(str)))
        }
        broadcastReceiver.addOnResultCallback(res, AsyncCallback(result, err))
        continuation.invokeOnCancellation { broadcastReceiver.removeOnResultCallback(res) }
    }

    private suspend fun registerResultByteArray(res: Int) = suspendCancellableCoroutine<ByteArray> { continuation ->
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

    private suspend fun registerResultStringArrayList(res: Int) = suspendCancellableCoroutine<ArrayList<String>> { continuation ->
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
        val binder = binderProvider.getAsync()

        return suspendCancellableCoroutine { c ->
            binder.signDataDetachedAsync(data, ParcelUuid(identity.fingerprint), object: ByteArrayCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onData(data: ByteArray) {
                    c.resume(data)
                }

            })
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
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.getByApplicationAsync(application, object: ScatterMessageCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onScatterMessage(message: MutableList<ScatterMessage>) {
                    c.resume(message)
                }

            })
        }
    }


    override suspend fun getScatterMessages(application: String, since: Date): List<ScatterMessage> {
        return getScatterMessages(application, since, Date())
    }

    override suspend fun getScatterMessages(application: String, start: Date, end: Date): List<ScatterMessage> {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.getByApplicationDateAsync(application, start.time, end.time, object: ScatterMessageCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onScatterMessage(message: MutableList<ScatterMessage>) {
                    c.resume(message)
                }

            })
        }
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
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.generateIdentity(name, object: IdentityCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onIdentity(identity: MutableList<Identity>) {
                    if (identity.size != 1) {
                        c.resumeWithException(IllegalStateException("wrong identity count"))
                    } else {
                        c.resume(identity[0])
                    }
                }

            })
        }
    }

    override suspend fun authorizeIdentity(identity: Identity, packageName: String) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.authorizeApp(ParcelUuid(identity.fingerprint), packageName, object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })

        }
    }

    override suspend fun deauthorizeIdentity(identity: Identity, packageName: String) {
        Log.v(TAG, "deauthorizing $packageName")
        binderProvider.getAsync().deauthorizeApp(ParcelUuid(identity.fingerprint), packageName)
    }

    override suspend fun getPermissions(identity: Identity): List<NamePackage> {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            val identities = binder.getAppPermissions(ParcelUuid(identity.fingerprint), object: StringCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onString(result: MutableList<String>) {
                    val pm = context.packageManager
                    val packageList = result.map { id ->
                        val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
                        NamePackage(pm.getApplicationLabel(r).toString(), r)
                    }
                    c.resume(packageList)
                }

            })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendMessageAsync(message, object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendMessagesAsync(messages, object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: UUID) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessageAsync(message, ParcelUuid(identity), object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: UUID) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessagesAsync(messages, ParcelUuid(identity), object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: Identity) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessageAsync(message, ParcelUuid(identity.fingerprint), object: UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun getPackages(): List<String> {
        val res = binderProvider.getAsync().knownPackagesAsync
        return registerResultStringArrayList(res)
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity) {
        return sendMessage(messages, identity.fingerprint)
    }

    override suspend fun removeIdentity(identity: Identity): Boolean {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c->
            binder.removeIdentity(ParcelUuid(identity.fingerprint), object: BoolCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onResult(result: Boolean) {
                    c.resume(result)
                }

            })
        }
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

    override suspend fun isConnected(): Boolean {
        try {
            val binder = binderProvider.getAsync()
            return suspendCancellableCoroutine { c ->
                binder.ping(object : UnitCallback.Stub() {
                    override fun onError(error: String?) {
                        c.resume(false)
                    }

                    override fun onComplete() {
                        c.resume(true)
                    }

                })
            }
        }
        catch (exception: Exception) {
            return false
        }
    }

    init {
        Log.v(TAG, "init called")
    }
}